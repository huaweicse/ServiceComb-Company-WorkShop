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

/**
 * Post {@link ZuulFilter} to update cache entry with response body from remote service.
 */
abstract class CacheUpdateFilter extends ZuulFilter {

  private static final Logger logger = LoggerFactory.getLogger(BeekeeperCacheUpdateFilter.class);
  private static final DynamicIntProperty STREAM_BUFFER_SIZE = DynamicPropertyFactory
      .getInstance()
      .getIntProperty(ZUUL_INITIAL_STREAM_BUFFER_SIZE, 8192);
  private final ProjectArchive<String, String> archive;
  private final PathExtractor pathExtractor;

  CacheUpdateFilter(
      ProjectArchive<String, String> archive, PathExtractor pathExtractor) {
    this.archive = archive;
    this.pathExtractor = pathExtractor;
  }

  @Override
  public String filterType() {
    return "post";
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

    archive.archive(pathExtractor.path(context), responseBodyOf(context));
    return null;
  }

  private boolean isSuccessfulFibonacciResponse(RequestContext context, String path) {
    return path.contains(pathInRequest())
        && context.getResponseStatusCode() == SC_OK
        && context.sendZuulResponse()
        && (context.getResponseBody() != null || context.getResponseDataStream() != null);
  }

  protected abstract String pathInRequest();

  private String responseBodyOf(RequestContext context) {
    try {
      if (context.getResponseBody() != null) {
        return context.getResponseBody();
      } else {
        return responseBody(context);
      }
    } catch (IOException e) {
      logger.error("Failed to read response body", e);
      context.setResponseStatusCode(SC_INTERNAL_SERVER_ERROR);
      throw new IllegalStateException("Failed to read response body", e);
    }
  }

  private String responseBody(RequestContext context) throws IOException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(STREAM_BUFFER_SIZE.get())) {
      IOUtils.copy(context.getResponseDataStream(), outputStream);
      context.setResponseBody(outputStream.toString());
      return outputStream.toString();
    }
  }
}
