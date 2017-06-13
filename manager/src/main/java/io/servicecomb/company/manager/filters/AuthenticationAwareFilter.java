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
package io.servicecomb.company.manager.filters;

import static io.servicecomb.company.manager.filters.FilterConstants.TOKEN_PREFIX;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.servicecomb.company.manager.AuthenticationService;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
class AuthenticationAwareFilter extends ZuulFilter {

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationAwareFilter.class);

  private static final String LOGIN_PATH = "/login";

  private final AuthenticationService authenticationService;

  @Autowired
  AuthenticationAwareFilter(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @Override
  public String filterType() {
    return "pre";
  }

  @Override
  public int filterOrder() {
    return 1;
  }

  @Override
  public boolean shouldFilter() {
    return !path().endsWith(LOGIN_PATH);
  }

  @Override
  public Object run() {
    try {
      filter();
    } catch (IOException e) {
      logger.error("Failed to filter user request", e);
      throw new IllegalStateException(e);
    }
    return null;
  }

  private void filter() throws IOException {
    RequestContext context = RequestContext.getCurrentContext();
    HttpServletResponse response = context.getResponse();

    if (doesNotContainToken(context)) {
      logger.warn("No token found in request header");
      response.sendError(SC_FORBIDDEN);
    } else {
      ResponseEntity<String> responseEntity = authenticationService.validate(token(context));
      if (!responseEntity.getStatusCode().is2xxSuccessful()) {
        response.sendError(SC_FORBIDDEN);
      }
    }
  }

  private boolean doesNotContainToken(RequestContext context) {
    return authorizationHeader(context) == null
        || !authorizationHeader(context).startsWith(TOKEN_PREFIX);
  }

  private String token(RequestContext context) {
    return authorizationHeader(context).replace(TOKEN_PREFIX, "");
  }

  private String authorizationHeader(RequestContext context) {
    return context.getRequest().getHeader(AUTHORIZATION);
  }

  private String path() {
    RequestContext context = RequestContext.getCurrentContext();
    HttpServletRequest request = context.getRequest();

    String path = request.getContextPath() + request.getServletPath();
    if (request.getPathInfo() != null) {
      path = path + request.getPathInfo();
    }

    logger.debug("Get the request path {}", path);
    return path;
  }
}
