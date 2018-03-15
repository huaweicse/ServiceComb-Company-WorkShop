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
package org.apache.servicecomb.company.auth;

import static com.seanyinx.github.unit.scaffolding.AssertUtils.expectFailing;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class JwtTokenStoreTest {

  private final String secretKey = uniquify("SecretKey");
  private final String someUser = uniquify("User");
  private TokenStore tokenStore;

  @Test
  public void generatesTokenOfSomeUser() {
    tokenStore = new JwtTokenStore(secretKey, 10);

    String token = tokenStore.generate(someUser);
    assertThat(token).isNotEmpty();

    String user = tokenStore.parse(token);
    assertThat(user).isEqualTo(someUser);
  }

  @Test
  public void blowsUpWhenTokenExpired() throws InterruptedException {
    tokenStore = new JwtTokenStore(secretKey, 1);

    String token = tokenStore.generate(someUser);
    TimeUnit.MILLISECONDS.sleep(1002);

    try {
      tokenStore.parse(token);
      expectFailing(TokenException.class);
    } catch (TokenException ignored) {
    }
  }
}
