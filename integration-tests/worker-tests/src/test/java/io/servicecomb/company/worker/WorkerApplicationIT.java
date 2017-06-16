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
package io.servicecomb.company.worker;

import static org.assertj.core.api.Assertions.assertThat;

import io.servicecomb.provider.pojo.RpcReference;
import io.servicecomb.springboot.starter.provider.EnableServiceComb;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkerApplicationIT {

  @Autowired
  private FibonacciProvider fibonacciProvider;

  @Value("${service.address}")
  private String serviceAddress;

  private final long fibonacciValue = 12586269025L;
  private final RestTemplate restTemplate = new RestTemplate();

  @Test
  public void getsNthTermOfFibonacci() {
    long fibo = fibonacciProvider.term(50);

    assertThat(fibo).isEqualTo(fibonacciValue);
  }

  @Test
  public void getsNthTermOfFibonacciByRest() {
    ResponseEntity<Long> responseEntity = restTemplate.getForEntity(
        serviceAddress + "/fibonacci/term?n=50",
        long.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).isEqualTo(fibonacciValue);
  }

  @SpringBootApplication
  @EnableServiceComb
  static class WorkerTestApplication {
    // this annotation does not take effect in spring test
    @RpcReference(microserviceName = "worker", schemaId = "fibonacciRpcEndpoint")
    private FibonacciProvider fibonacciProvider;

    public static void main(String[] args) {
      SpringApplication.run(WorkerTestApplication.class, args);
    }

    @Bean
    FibonacciProvider fibonacciProvider() {
      return fibonacciProvider;
    }
  }
}

