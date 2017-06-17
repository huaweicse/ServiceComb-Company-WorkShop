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

import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class AuthenticationServiceHappyTest {
  @Rule
  public final PactProviderRule providerRule = new PactProviderRule("Doorman", this);

  private final String token = "sean-token";
  private final String username = "Sean";

  private final ServiceInstance serviceInstance = mock(ServiceInstance.class);
  private final RestTemplate restTemplate = new FixedUrlRestTemplate(providerRule.getConfig().url() + "/rest/validate");

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final AuthenticationService authenticationService = new AuthenticationService(restTemplate);

  @Pact(consumer = "Manager")
  public PactFragment createFragment(PactDslWithProvider pactDslWithProvider) throws JsonProcessingException {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", MediaType.TEXT_PLAIN_VALUE);

    return pactDslWithProvider
        .given("User Sean is authorized")
        .uponReceiving("a request to access from Sean")
        .path("/rest/validate")
        .body(objectMapper.writeValueAsString(new Token(token)), APPLICATION_JSON)
        .method("POST")
        .willRespondWith()
        .headers(headers)
        .status(HttpStatus.OK.value())
        .body(username)
        .toFragment();
  }

  @PactVerification
  @Test
  public void validatesUserToken() {
    when(serviceInstance.getUri()).thenReturn(URI.create(providerRule.getConfig().url()));

    ResponseEntity<String> responseEntity = authenticationService.validate(token);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).isEqualTo(username);
  }
}
