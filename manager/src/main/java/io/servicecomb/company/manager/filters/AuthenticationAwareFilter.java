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
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * {@link AuthenticationAwareFilter} is a pre-filter for netflix Zuul. It validates all user
 * requests except those ending with /login to doorman to verify if the JWT token provided is
 * genuine.
 */
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
    filter();
    return null;
  }

  private void filter() {
    RequestContext context = RequestContext.getCurrentContext();

    if (doesNotContainToken(context)) {
      logger.warn("No token found in request header");
      rejectRequest(context);
    } else {
      ResponseEntity<String> responseEntity = authenticationService.validate(token(context));
      if (!responseEntity.getStatusCode().is2xxSuccessful()) {
        rejectRequest(context);
      }
    }
  }

  private void rejectRequest(RequestContext context) {
    context.setResponseStatusCode(SC_FORBIDDEN);
    context.setSendZuulResponse(false);
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
