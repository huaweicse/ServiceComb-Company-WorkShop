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

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.servicecomb.company.auth.AuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(AuthenticationController.class)
public class AuthenticationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AuthenticationService authenticationService;

  @MockBean
  private AuthorizationHeaderGenerator headerGenerator;

  private final String password = uniquify("password");
  private final String username = uniquify("username");
  private final String token = uniquify("token");
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void setUp() throws Exception {
    when(headerGenerator.generate(token)).thenReturn(new HttpHeaders());
  }

  @Test
  public void returnsTokenOfAuthenticatedUser() throws Exception {
    when(authenticationService.authenticate(username, password)).thenReturn(token);

    mockMvc.perform(
        post("/rest/login")
            .contentType(APPLICATION_FORM_URLENCODED)
            .param(AuthenticationController.USERNAME, username)
            .param(AuthenticationController.PASSWORD, password))
        .andExpect(status().isOk())
        .andExpect(content().string("Welcome, " + username));
  }

  @Test
  public void validatesTokenAgainstStoredUserSessions() throws Exception {
    when(authenticationService.validate(token)).thenReturn(username);

    mockMvc.perform(
        post("/rest/validate")
            .contentType(APPLICATION_JSON_UTF8_VALUE)
            .content(objectMapper.writeValueAsBytes(new Token(token))))
        .andExpect(status().isOk())
        .andExpect(content().string(username));
  }
}
