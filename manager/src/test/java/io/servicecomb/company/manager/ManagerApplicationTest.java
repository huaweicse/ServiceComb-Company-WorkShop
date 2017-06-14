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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static io.servicecomb.company.manager.filters.FilterConstants.TOKEN_PREFIX;
import static java.util.Collections.singletonList;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.servicecomb.company.manager.archive.Archive;
import io.servicecomb.company.manager.archive.ProjectArchive;
import java.net.URI;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "zuul.routes.doorman.url=http://localhost:8082",
        "zuul.routes.worker.url=http://localhost:8082"
    }
)
public class ManagerApplicationTest {

  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(8082);

  private static final String validUsername = uniquify("validUsername");

  private static final String password = uniquify("password");
  private static final String token = uniquify("token");
  private static final String unknownToken = uniquify("unknownToken");
  private static final String authorization = TOKEN_PREFIX + token;
  private static final String doormanAddress = "http://localhost:8082";

  private final ServiceInstance serviceInstance = mock(ServiceInstance.class);

  @Autowired
  private TestRestTemplate restTemplate;

  @MockBean
  private LoadBalancerClient loadBalancer;

  @Autowired
  private ProjectArchive<Integer, Long> archive;

  @BeforeClass
  public static void setUp() throws Exception {
    stubFor(post(urlEqualTo("/login"))
        .withRequestBody(containing("username=" + validUsername))
        .willReturn(
            aResponse()
                .withHeader(AUTHORIZATION, authorization)
                .withStatus(SC_OK)
                .withBody(token)));

    stubFor(post(urlEqualTo("/validate"))
        .withRequestBody(containing("token=" + token))
        .willReturn(
            aResponse()
                .withStatus(SC_OK)
                .withBody(validUsername)));

    stubFor(post(urlEqualTo("/validate"))
        .withRequestBody(containing("token=" + unknownToken))
        .willReturn(
            aResponse()
                .withStatus(SC_FORBIDDEN)));

    stubFor(get(urlEqualTo("/fibonacci/term?n=1"))
        .willReturn(
            aResponse()
                .withStatus(SC_OK)
                .withBody("1")));
  }

  @Test
  public void returnsTokenToAuthenticatedUser() throws Exception {
    ResponseEntity<String> responseEntity = restTemplate.postForEntity(
        "/doorman/login",
        loginRequest(validUsername),
        String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    assertThat(responseEntity.getHeaders())
        .containsEntry(AUTHORIZATION, singletonList(authorization));
  }

  @Test
  public void validatesTokenAndCachesResult() {
    when(loadBalancer.choose("doorman")).thenReturn(serviceInstance);
    when(serviceInstance.getUri()).thenReturn(URI.create(doormanAddress));

    ResponseEntity<String> responseEntity = restTemplate.exchange(
        "/worker/fibonacci/term?n=1",
        GET,
        validationRequest(token),
        String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    assertThat(responseEntity.getBody()).isEqualTo("1");

    Archive<Long> result = archive.search(1);

    assertThat(result.exists()).isTrue();
    assertThat(result.get()).isEqualTo(1L);
  }

  @Test
  public void forbidsRequestsWithoutToken() {
    when(loadBalancer.choose("doorman")).thenReturn(serviceInstance);
    when(serviceInstance.getUri()).thenReturn(URI.create(doormanAddress));

    ResponseEntity<String> responseEntity = restTemplate.getForEntity(
        "/worker/fibonacci/term?n=1",
        String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
  }

  @Test
  public void forbidsRequestsWithUnknownToken() {
    when(loadBalancer.choose("doorman")).thenReturn(serviceInstance);
    when(serviceInstance.getUri()).thenReturn(URI.create(doormanAddress));

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
}
