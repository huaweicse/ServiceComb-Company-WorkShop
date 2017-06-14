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

import static com.netflix.zuul.constants.ZuulConstants.ZUUL_INITIAL_STREAM_BUFFER_SIZE;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.servicecomb.company.manager.archive.ProjectArchive;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class CacheUpdateFilter extends ZuulFilter {

  private static final Logger logger = LoggerFactory.getLogger(CacheUpdateFilter.class);
  private static final DynamicIntProperty STREAM_BUFFER_SIZE = DynamicPropertyFactory
      .getInstance()
      .getIntProperty(ZUUL_INITIAL_STREAM_BUFFER_SIZE, 8192);

  private final ProjectArchive<Integer, Long> archive;
  private final PathExtractor pathExtractor;

  @Autowired
  CacheUpdateFilter(ProjectArchive<Integer, Long> archive, PathExtractor pathExtractor) {
    this.archive = archive;
    this.pathExtractor = pathExtractor;
  }

  @Override
  public String filterType() {
    return "post";
  }

  @Override
  public int filterOrder() {
    return 0;
  }

  @Override
  public boolean shouldFilter() {
    RequestContext context = RequestContext.getCurrentContext();
    String path = pathExtractor.path(context);
    logger.info("Received request with query path: {}", path);

    return isSuccessfulFibonacciResponse(context, path);
  }

  @Override
  public Object run() {
    RequestContext context = RequestContext.getCurrentContext();
    Integer fibonacciTerm = Integer.valueOf(context.getRequestQueryParams().get("n").get(0));
    Long fibonacciValue = fibonacciInResponse(context);

    logger.info("Updating cache with fibonacci term {} and value {}", fibonacciTerm, fibonacciValue);
    archive.archive(fibonacciTerm, fibonacciValue);
    return null;
  }

  private boolean isSuccessfulFibonacciResponse(RequestContext context, String path) {
    return path.endsWith("/fibonacci/term")
        && context.getResponseStatusCode() == SC_OK
        && context.sendZuulResponse()
        && (context.getResponseBody() != null || context.getResponseDataStream() != null);
  }

  private Long fibonacciInResponse(RequestContext context) {
    Long value;

    if (context.getResponseBody() != null) {
      value = Long.valueOf(context.getResponseBody());
    } else {
      value = valueFromResponse(context);
    }

    return value;
  }

  private Long valueFromResponse(RequestContext context) {
    Long value;

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(STREAM_BUFFER_SIZE.get())) {
      IOUtils.copy(context.getResponseDataStream(), outputStream);
      value = Long.valueOf(outputStream.toString());
      context.setResponseBody(outputStream.toString());
    } catch (IOException e) {
      logger.error("Failed to read response body", e);
      context.setResponseStatusCode(SC_INTERNAL_SERVER_ERROR);
      throw new IllegalStateException("Failed to read response body", e);
    }
    return value;
  }
}
