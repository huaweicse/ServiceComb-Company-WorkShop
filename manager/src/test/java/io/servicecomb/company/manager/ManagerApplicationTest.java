/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.company.manager;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static io.servicecomb.company.manager.filters.FilterConstants.TOKEN_PREFIX;
import static java.util.Collections.singletonList;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.servicecomb.company.manager.archive.Archive;
import io.servicecomb.company.manager.archive.ProjectArchive;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "zuul.routes.doorman.url=http://localhost:8082",
        "zuul.routes.beekeeper.url=http://localhost:8082",
        "zuul.routes.worker.url=http://localhost:8082"
    }
)
@ActiveProfiles("dev")
public class ManagerApplicationTest {

  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(8082);

  private static final String validUsername = uniquify("validUsername");

  private static final String password = uniquify("password");
  private static final String token = uniquify("token");
  private static final String unknownToken = uniquify("unknownToken");
  private static final String authorization = TOKEN_PREFIX + token;
  private static String ancestorJson = "{\"ancestors\": 2}";

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private ProjectArchive<String, String> archive;

  @BeforeClass
  public static void setUp() throws Exception {
    stubFor(post(urlEqualTo("/rest/login"))
        .withRequestBody(containing("username=" + validUsername))
        .willReturn(
            aResponse()
                .withHeader(AUTHORIZATION, authorization)
                .withStatus(SC_OK)
                .withBody(token)));

    stubFor(post(urlEqualTo("/rest/validate"))
        .withRequestBody(matchingJsonPath("$.[?($.token == '" + token + "')]"))
        .willReturn(
            aResponse()
                .withStatus(SC_OK)
                .withBody(validUsername)));

    stubFor(post(urlEqualTo("/rest/validate"))
        .withRequestBody(matchingJsonPath("$.[?($.token == '" + unknownToken + "')]"))
        .willReturn(
            aResponse()
                .withStatus(SC_FORBIDDEN)));

    stubFor(get(urlEqualTo("/fibonacci/term?n=1"))
        .willReturn(
            aResponse()
                .withStatus(SC_OK)
                .withHeader(CONTENT_TYPE, TEXT_PLAIN_VALUE)
                .withBody("1")));

    stubFor(get(urlEqualTo("/rest/drone/ancestors/2"))
        .willReturn(
            aResponse()
                .withStatus(SC_OK)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(ancestorJson)));
  }

  @Test
  public void returnsTokenToAuthenticatedUser() throws Exception {
    ResponseEntity<String> responseEntity = restTemplate.postForEntity(
        "/doorman/rest/login",
        loginRequest(validUsername),
        String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    assertThat(responseEntity.getHeaders())
        .containsEntry(AUTHORIZATION, singletonList(authorization));
  }

  @Test
  public void validatesTokenAndCachesWorkerResult() {
    ResponseEntity<String> responseEntity = restTemplate.exchange(
        "/worker/fibonacci/term?n=1",
        GET,
        validationRequest(token),
        String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    assertThat(responseEntity.getBody()).isEqualTo("1");

    responseEntity = restTemplate.exchange(
        "/worker/fibonacci/term?n=1",
        GET,
        validationRequest(token),
        String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    assertThat(responseEntity.getBody()).isEqualTo("1");

    verify(exactly(1), getRequestedFor(urlEqualTo("/fibonacci/term?n=1")));

    Archive<String> result = archive.search("/worker/fibonacci/term?n=1");

    assertThat(result.exists()).isTrue();
    assertThat(result.get()).isEqualTo("1");
  }

  @Test
  public void validatesTokenAndCachesBeekeeperResult() {
    ResponseEntity<Ancestor> responseEntity = restTemplate.exchange(
        "/beekeeper/rest/drone/ancestors/{generation}",
        GET,
        validationRequest(token),
        Ancestor.class,
        2);

    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    assertThat(responseEntity.getBody().getAncestors()).isEqualTo(2L);

    responseEntity = restTemplate.exchange(
        "/beekeeper/rest/drone/ancestors/{generation}",
        GET,
        validationRequest(token),
        Ancestor.class,
        2);

    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    assertThat(responseEntity.getBody().getAncestors()).isEqualTo(2L);

    verify(exactly(1), getRequestedFor(urlEqualTo("/rest/drone/ancestors/2")));

    Archive<String> result = archive.search("/beekeeper/rest/drone/ancestors/2");

    assertThat(result.exists()).isTrue();
    assertThat(result.get()).isEqualTo(ancestorJson);
  }

  @Test
  public void forbidsRequestsWithoutToken() {
    ResponseEntity<String> responseEntity = restTemplate.getForEntity(
        "/worker/fibonacci/term?n=1",
        String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
  }

  @Test
  public void forbidsRequestsWithUnknownToken() {
    ResponseEntity<String> responseEntity = restTemplate.exchange(
        "/worker/fibonacci/term?n=1",
        GET,
        validationRequest(unknownToken),
        String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
  }

  private HttpEntity<MultiValueMap<String, String>> loginRequest(String username) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("username", username);
    map.add("password", password);

    return new HttpEntity<>(map, headers);
  }

  private HttpEntity<Object> validationRequest(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.add(AUTHORIZATION, TOKEN_PREFIX + token);

    return new HttpEntity<>(headers);
  }

  private static class Ancestor {

    private long ancestors;

    /**
     * Default constructor for Json deserialization
     */
    Ancestor() {
    }

    Ancestor(long ancestors) {
      this.ancestors = ancestors;
    }

    public long getAncestors() {
      return ancestors;
    }
  }
}
