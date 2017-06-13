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

import static org.springframework.http.HttpStatus.REQUEST_TIMEOUT;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * {@link AuthenticationService} validates user requests by sending the token in request header to
 * remote doorman for authentication.
 */
@Service
public class AuthenticationService {

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

  private final RestTemplate restTemplate;
  private final LoadBalancerClient loadBalancer;

  @Autowired
  AuthenticationService(LoadBalancerClient loadBalancer) {
    this.loadBalancer = loadBalancer;
    restTemplate = new RestTemplate();

    restTemplate.setErrorHandler(new ResponseErrorHandler() {
      @Override
      public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        return false;
      }

      @Override
      public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
      }
    });
  }

  @HystrixCommand(fallbackMethod = "timeout")
  public ResponseEntity<String> validate(String token) {
    ResponseEntity<String> responseEntity = restTemplate.postForEntity(
        doormanAddress() + "/validate",
        validationRequest(token),
        String.class
    );

    if (!responseEntity.getStatusCode().is2xxSuccessful()) {
      logger.warn("No such user found with token {}", token);
    }
    logger.info("Validated request of token {} to be user {}", token, responseEntity.getBody());
    return responseEntity;
  }

  private ResponseEntity<String> timeout(String token) {
    logger.warn("Request to validate token {} timed out", token);
    return new ResponseEntity<>(REQUEST_TIMEOUT);
  }

  private String doormanAddress() {
    return loadBalancer.choose("doorman").getUri().toString();
  }

  private HttpEntity<MultiValueMap<String, String>> validationRequest(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("token", token);

    return new HttpEntity<>(map, headers);
  }
}
