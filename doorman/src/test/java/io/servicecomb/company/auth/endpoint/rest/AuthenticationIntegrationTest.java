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
package io.servicecomb.company.auth.endpoint.rest;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static io.servicecomb.company.auth.endpoint.rest.AuthenticationController.PASSWORD;
import static io.servicecomb.company.auth.endpoint.rest.AuthenticationController.TOKEN;
import static io.servicecomb.company.auth.endpoint.rest.AuthenticationController.USERNAME;
import static io.servicecomb.company.auth.endpoint.rest.AuthorizationHeaderGenerator.TOKEN_PREFIX;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.servicecomb.company.DoormanApplication;
import java.time.ZonedDateTime;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"company.auth.secret=someSecretKey"}, classes = DoormanApplication.class)
@AutoConfigureMockMvc
public class AuthenticationIntegrationTest {

  private static final String password = "password";
  private static final String username = "jordan";

  @Autowired
  private MockMvc mockMvc;

  @Value("${company.auth.secret}")
  private String secretKey;

  @Test
  public void returnsTokenOfAuthenticatedUser() throws Exception {
    ZonedDateTime loginTime = ZonedDateTime.now().truncatedTo(SECONDS);

    MvcResult result = mockMvc.perform(
        MockMvcRequestBuilders.post("/login")
            .contentType(APPLICATION_FORM_URLENCODED)
            .param(USERNAME, username)
            .param(PASSWORD, password))
        .andExpect(status().isOk()).andReturn();

    String tokenInHeader = result.getResponse().getHeader(AUTHORIZATION).replace(TOKEN_PREFIX, "");
    Claims token = Jwts.parser()
        .setSigningKey(secretKey)
        .parseClaimsJws(tokenInHeader)
        .getBody();

    assertThat(token.getSubject()).isEqualTo(username);
    assertThat(token.getExpiration())
        .isAfterOrEqualsTo(Date.from(loginTime.plusDays(1).toInstant()))
        .isBeforeOrEqualsTo(Date.from(ZonedDateTime.now().plusDays(1).toInstant()));

    mockMvc.perform(
        MockMvcRequestBuilders.post("/validate")
            .contentType(APPLICATION_FORM_URLENCODED)
            .param(TOKEN, tokenInHeader))
        .andExpect(status().isOk());
  }

  @Test
  public void forbidsAccessOfInvalidUser() throws Exception {
    mockMvc.perform(
        MockMvcRequestBuilders.post("/login")
            .contentType(APPLICATION_FORM_URLENCODED)
            .param(USERNAME, uniquify("invalid-user"))
            .param(PASSWORD, uniquify("password")))
        .andExpect(status().isForbidden());
  }
}
