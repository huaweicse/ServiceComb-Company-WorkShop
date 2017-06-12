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

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class TokenWrappingFilter extends ZuulFilter {

  private static final Logger logger = LoggerFactory.getLogger(TokenWrappingFilter.class);

  @Override
  public String filterType() {
    return "post";
  }

  @Override
  public int filterOrder() {
    return 1;
  }

  @Override
  public boolean shouldFilter() {
    RequestContext context = RequestContext.getCurrentContext();
    return path(context).endsWith("/login")
        && context.getResponse().getStatus() == SC_OK;
  }

  @Override
  public Object run() {
    try {
      filter();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    return null;
  }

  private void filter() throws IOException {
    RequestContext context = RequestContext.getCurrentContext();
    HttpServletResponse response = context.getResponse();

    if (context.getResponseBody() != null) {
      response.addHeader(AUTHORIZATION, "Bearer " + context.getResponseBody());
    } else {
      try {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8192);
        writeResponse(context.getResponseDataStream(), outputStream);
        response.addHeader(AUTHORIZATION, "Bearer " + outputStream.toString());
      } catch (IOException e) {
        logger.error("Failed to read response body", e);
        response.sendError(SC_INTERNAL_SERVER_ERROR);
      }
    }
  }

  private String path(RequestContext context) {
    HttpServletRequest request = context.getRequest();

    String path = request.getContextPath() + request.getServletPath();
    if (request.getPathInfo() != null) {
      path = path + request.getPathInfo();
    }

    return path;
  }

  private void writeResponse(InputStream in, OutputStream out) throws IOException {
    byte[] bytes = new byte[8192];
    int bytesRead;
    while ((bytesRead = in.read(bytes)) != -1) {
      out.write(bytes, 0, bytesRead);
    }
  }
}
