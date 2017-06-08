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
package io.servicecomb.company.auth;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static io.servicecomb.company.auth.AuthenticationController.PASSWORD;
import static io.servicecomb.company.auth.AuthenticationController.USERNAME;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@WebMvcTest(AuthenticationController.class)
public class AuthenticationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AuthenticationService authenticationService;

  private final String password = uniquify("password");
  private final String username = uniquify("username");
  private final String token = uniquify("token");

  @Test
  public void returnsTokenOfAuthenticatedUser() throws Exception {
    when(authenticationService.authenticate(username, password)).thenReturn(token);

    mockMvc.perform(
        MockMvcRequestBuilders.post("/login")
            .contentType(APPLICATION_FORM_URLENCODED)
            .param(USERNAME, username)
            .param(PASSWORD, password))
        .andExpect(status().isOk())
        .andExpect(content().string(token));
  }

  @Test
  public void validatesTokenAgainstStoredUserSessions() throws Exception {
    when(authenticationService.validate(token)).thenReturn(username);

    mockMvc.perform(
        MockMvcRequestBuilders.post("/validate")
            .contentType(APPLICATION_FORM_URLENCODED)
            .param("token", token))
        .andExpect(status().isOk())
        .andExpect(content().string(username));
  }
}
