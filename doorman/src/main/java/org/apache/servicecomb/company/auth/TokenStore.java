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
/**
 * {@link TokenStore} is a general interface responsible for token generation and parsing.
 */
public interface TokenStore {

  /**
   * Generates a token embedded with the username provided.
   * @param username the username of requested user.
   * @return the generated token.
   */
  String generate(String username);

  /**
   * Parses a token if valid.
   * Throws {@link TokenException} if the provided is not genuine.
   * @param token the token.
   * @return the username embedded in the token.
   */
  String parse(String token);
}
