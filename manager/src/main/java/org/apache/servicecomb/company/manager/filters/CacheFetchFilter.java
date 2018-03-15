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

package org.apache.servicecomb.company.manager.filters;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.servicecomb.company.manager.archive.Archive;
import org.apache.servicecomb.company.manager.archive.ProjectArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pre {@link ZuulFilter} to search cache for corresponding entry of provided key. If a
 * matching cache entry is found, return the cache value immediately without forwarding requests
 * further to remote service.
 */
abstract class CacheFetchFilter extends ZuulFilter {

  private static final Logger logger = LoggerFactory.getLogger(CacheFetchFilter.class);
  private final ProjectArchive<String, String> archive;
  private final PathExtractor pathExtractor;

  CacheFetchFilter(ProjectArchive<String, String> archive, PathExtractor pathExtractor) {
    this.archive = archive;
    this.pathExtractor = pathExtractor;
  }

  @Override
  public String filterType() {
    return "pre";
  }

  @Override
  public boolean shouldFilter() {
    RequestContext context = RequestContext.getCurrentContext();
    String path = pathExtractor.path(context);
    logger.info("Received request with query path: {}", path);
    return isGenuineFibonacciRequest(context, path);
  }

  @Override
  public Object run() {
    RequestContext context = RequestContext.getCurrentContext();
    String path = pathExtractor.path(context);
    logger.info("Received request to " + requestDescription() + " at {}", path);

    Archive<String> result = archive.search(path);

    if (result.exists()) {
      logger.info("Found existing project archive with key {} and value {}", path, result.get());
      returnResultWithoutForwardingToZuul(context, result.get(), responseContentType());
    }

    return null;
  }

  protected abstract String requestDescription();

  protected abstract String responseContentType();

  protected abstract String pathInRequest();

  private boolean isGenuineFibonacciRequest(RequestContext context, String path) {
    return path.contains(pathInRequest()) && context.sendZuulResponse();
  }

  private void returnResultWithoutForwardingToZuul(
      RequestContext context,
      String body,
      String contentType) {
    context.setResponseStatusCode(SC_OK);
    context.getResponse().setHeader(CONTENT_TYPE, contentType);
    context.setResponseBody(body);
    context.setSendZuulResponse(false);
  }
}
