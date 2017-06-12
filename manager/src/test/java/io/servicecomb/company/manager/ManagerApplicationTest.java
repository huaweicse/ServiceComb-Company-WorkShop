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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.Collections;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ManagerApplicationTest {

  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(8082/*wireMockConfig().dynamicPort()*/);

  private static final String validUsername = uniquify("validUsername");
  private static final String unknownUsername = uniquify("unknownUsername");

  private static final String password = uniquify("password");
  private static final String token = uniquify("token");

  @Autowired
  private TestRestTemplate restTemplate;

  @BeforeClass
  public static void setUp() throws Exception {
    stubFor(post(urlEqualTo("/login"))
        .withRequestBody(containing("username=" + validUsername))
        .willReturn(
            aResponse()
                .withStatus(SC_OK)
                .withBody(token)));

    stubFor(WireMock.post(urlEqualTo("/login"))
        .withRequestBody(containing("username=" + unknownUsername))
        .willReturn(
            aResponse()
                .withStatus(SC_FORBIDDEN)));
  }

  @Test
  public void returnsTokenToAuthenticatedUser() throws Exception {
    ResponseEntity<String> responseEntity = restTemplate.postForEntity(
        "/doorman/login",
        loginRequest(validUsername),
        String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    assertThat(responseEntity.getHeaders()).containsEntry(AUTHORIZATION, Collections.singletonList("Bearer " + token));
  }

  @Test
  public void forbidsUnauthenticatedUser() throws Exception {
    ResponseEntity<String> responseEntity = restTemplate.postForEntity(
        "/doorman/login",
        loginRequest(unknownUsername),
        String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
    assertThat(responseEntity.getHeaders()).doesNotContainKey(AUTHORIZATION);
  }

  private HttpEntity<MultiValueMap<String, String>> loginRequest(String username) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("username", username);
    map.add("password", password);

    return new HttpEntity<>(map, headers);
  }
}
