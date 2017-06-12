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

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class AuthenticationServiceHappyTest {
  @Rule
  public final PactProviderRule providerRule = new PactProviderRule("Doorman", this);

  private final String token = uniquify("token");
  private final String username = uniquify("username");

  private final ServiceInstance serviceInstance = mock(ServiceInstance.class);
  private final LoadBalancerClient loadBalancer = mock(LoadBalancerClient.class);

  private final AuthenticationService authenticationService = new AuthenticationService(loadBalancer);

  @Pact(consumer = "Manager")
  public PactFragment createFragment(PactDslWithProvider pactDslWithProvider) throws JsonProcessingException {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);

    return pactDslWithProvider
        .given("User Sean is authorized")
        .uponReceiving("a request to access from Sean")
        .path("/validate")
        .query("token=" + token)
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
    when(loadBalancer.choose("doorman")).thenReturn(serviceInstance);
    when(serviceInstance.getUri()).thenReturn(URI.create(providerRule.getConfig().url()));

    ResponseEntity<String> responseEntity = authenticationService.validate(token);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).isEqualTo(username);
  }
}
