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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = ManagerApplication.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
    })
@ActiveProfiles("sit")
public class ManagerApplicationIT {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  public void loginAndValidateWithRemoteDoorman() {
    ResponseEntity<String> responseEntity = restTemplate.exchange(
        "/doorman/rest/login",
        HttpMethod.POST,
        loginRequest(),
        String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    assertThat(responseEntity.getBody()).isEqualTo("Welcome, jordan");

    responseEntity = restTemplate.exchange(
        "/doorman/path/not/exist",
        HttpMethod.GET,
        validationRequest(authorization(responseEntity)),
        String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
  }

  private String authorization(ResponseEntity<String> responseEntity) {
    return responseEntity.getHeaders().get(AUTHORIZATION).get(0);
  }

  private HttpEntity<MultiValueMap<String, String>> loginRequest() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("username", "jordan");
    map.add("password", "password");

    return new HttpEntity<>(map, headers);
  }

  private HttpEntity<Object> validationRequest(String authorization) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.add(AUTHORIZATION, authorization);

    return new HttpEntity<>(headers);
  }
}
