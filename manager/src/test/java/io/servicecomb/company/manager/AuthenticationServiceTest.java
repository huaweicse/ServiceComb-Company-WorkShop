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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.REQUEST_TIMEOUT;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.seanyinx.github.unit.scaffolding.Randomness;
import java.net.URI;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationServiceTest {
  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

  private final String username = Randomness.uniquify("username");
  private final String token = Randomness.uniquify("token");

  private final ServiceInstance serviceInstance = mock(ServiceInstance.class);

  private final String doormanAddress = "http://localhost:" + wireMockRule.port();

  @MockBean
  private LoadBalancerClient loadBalancer;

  @Autowired
  private AuthenticationService authenticationService;

  @Before
  public void setUp() throws Exception {
    stubFor(post(urlEqualTo("/validate"))
        .willReturn(
            aResponse()
                .withFixedDelay(2000)
                .withStatus(SC_OK)
                .withBody(username)));
  }

  @Test
  public void timesOutWhenDoormanIsUnresponsive() {
    when(loadBalancer.choose("doorman")).thenReturn(serviceInstance);
    when(serviceInstance.getUri()).thenReturn(URI.create(doormanAddress));

    ResponseEntity<String> responseEntity = authenticationService.validate(token);

    assertThat(responseEntity.getStatusCode()).isEqualTo(REQUEST_TIMEOUT);
  }
}