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

import static org.springframework.http.HttpStatus.FORBIDDEN;

import io.servicecomb.company.auth.UnauthorizedAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * {@link AuthenticationExceptionHandler} intercepts response with {@link
 * UnauthorizedAccessException} and wraps the exception message in a forbidden response.
 */
@ControllerAdvice
class AuthenticationExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationExceptionHandler.class);

  @ExceptionHandler(UnauthorizedAccessException.class)
  ResponseEntity<String> handleException(UnauthorizedAccessException e) {
    logger.warn("Authentication failure", e);
    return new ResponseEntity<>(e.getMessage(), FORBIDDEN);
  }
}
