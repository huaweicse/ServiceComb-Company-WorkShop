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
package org.apache.servicecomb.company.auth.endpoint.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactFolder;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import org.apache.servicecomb.company.auth.AuthenticationService;
import org.apache.servicecomb.company.auth.UnauthorizedAccessException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@RunWith(PactRunner.class)
@PactFolder("../target/pacts")
@Provider("Doorman")
public class AuthenticationContractTest {

  private static ConfigurableApplicationContext doormanContext;
  private static AuthenticationService authenticationService;

  @TestTarget
  public final Target target = new HttpTarget(8081);

  @BeforeClass
  public static void startCustomerService() {
    doormanContext = SpringApplication
        .run(DoormanRestApplication.class, "--server.port=8081", "--spring.profiles.active=dev", "--spring.main.web-environment=true");

    authenticationService = doormanContext.getBean(AuthenticationService.class);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    doormanContext.close();
  }

  @State("User Sean is authorized")
  public void acceptAuthenticatedUser() {
    when(authenticationService.validate("sean-token")).thenReturn("Sean");
  }

  @State("User Jack is unauthorized")
  public void rejectUnknownUser() {
    when(authenticationService.validate("unknown-token"))
        .thenThrow(new UnauthorizedAccessException("No user matches such a token unknown-token"));
  }

  @SpringBootApplication
  static class DoormanRestApplication {

    public static void main(String[] args) {
      SpringApplication.run(DoormanRestApplication.class, args);
    }

    @Bean
    AuthenticationService authenticationService() {
      return mock(AuthenticationService.class);
    }
  }
}
