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
package io.servicecomb.company;

import static io.jsonwebtoken.SignatureAlgorithm.HS512;

import io.jsonwebtoken.Jwts;

public class TokenStore {

  private final String secretKey;

  public TokenStore(String secretKey) {
    this.secretKey = secretKey;
  }

  public String generate(String username) {
    return Jwts.builder()
        .setSubject(username)
        .signWith(HS512, secretKey)
        .compact();
  }

  public String parse(String token) {
    return Jwts.parser()
        .setSigningKey(secretKey)
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }
}
