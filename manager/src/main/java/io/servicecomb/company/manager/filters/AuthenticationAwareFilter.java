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

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class AuthenticationAwareFilter extends ZuulFilter {

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationAwareFilter.class);

  private static final String LOGIN_PATH = "/login";

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
    return true;
  }

  @Override
  public Object run() {
    RequestContext context = RequestContext.getCurrentContext();
    filter(context);
    return null;
  }

  private void filter(RequestContext context) {
    HttpServletRequest request = context.getRequest();
    HttpServletResponse response = context.getResponse();

    String path = request.getContextPath() + request.getServletPath();
    if (request.getPathInfo() != null) {
      path = path + request.getPathInfo();
    }
    logger.info("Get the request path {}", path);

    if (path.endsWith(LOGIN_PATH)) {
      // if logging in, let the request flow
      return;
    }
  }
}
