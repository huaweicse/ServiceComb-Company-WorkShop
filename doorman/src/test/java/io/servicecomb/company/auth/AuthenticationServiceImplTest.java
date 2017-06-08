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

import static com.seanyinx.github.unit.scaffolding.AssertUtils.expectFailing;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class AuthenticationServiceImplTest {

  private final String username = uniquify("username");
  private final String password = uniquify("password");
  private final User user = new User(username);

  private final TokenStore tokenStore = mock(TokenStore.class);
  private final UserRepository userRepository = mock(UserRepository.class);

  private final AuthenticationService authenticationService = new AuthenticationServiceImpl(
      tokenStore, userRepository);
  private String token = uniquify("token");

  @Test
  public void authenticateUserWithUsernameAndPassword() {
    when(userRepository.findByUsernameAndPassword(username, password)).thenReturn(user);
    when(tokenStore.generate(username)).thenReturn(token);

    String token = authenticationService.authenticate(username, password);

    assertThat(token).isEqualTo(this.token);
  }

  @Test
  public void blowsUpWhenUserIsInvalid() {
    try {
      authenticationService.authenticate(username, password);
      expectFailing(UnauthorizedAccessException.class);
    } catch (UnauthorizedAccessException e) {
      assertThat(e.getMessage()).isEqualTo("No user matches username " + username + " and password");
    }
  }

  @Test
  public void validatesUserToken() {
    when(tokenStore.parse(token)).thenReturn(username);

    String user = authenticationService.validate(token);

    assertThat(user).isEqualTo(username);
  }

  @Test
  public void blowsUpWhenTokenMatchesNoUser() {
    when(tokenStore.parse(token)).thenThrow(new TokenException());

    try {
      authenticationService.validate(token);
      expectFailing(UnauthorizedAccessException.class);
    } catch (UnauthorizedAccessException e) {
      assertThat(e.getMessage()).isEqualTo("No user matches such a token " + token);
    }
  }
}
