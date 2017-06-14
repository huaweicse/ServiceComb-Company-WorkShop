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

import static io.servicecomb.company.manager.filters.FilterConstants.FIBONACCI_PATH;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.servicecomb.company.manager.archive.Archive;
import io.servicecomb.company.manager.archive.ProjectArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class CacheFetchFilter extends ZuulFilter {

  private static final Logger logger = LoggerFactory.getLogger(CacheFetchFilter.class);

  private final ProjectArchive<Integer, Long> archive;
  private final PathExtractor pathExtractor;

  @Autowired
  CacheFetchFilter(ProjectArchive<Integer, Long> archive, PathExtractor pathExtractor) {
    this.archive = archive;
    this.pathExtractor = pathExtractor;
  }

  @Override
  public String filterType() {
    return "pre";
  }

  @Override
  public int filterOrder() {
    return 2;
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

    Integer term = Integer.valueOf(context.getRequestQueryParams().get("n").get(0));
    logger.info("Received request to calculate fibonacci term {}", term);

    Archive<Long> result = archive.search(term);

    if (result.exists()) {
      logger.info("Found existing project archive with key {} and value {}", term, result.get());
      returnResultWithoutForwardingToZuul(context, result.get());
    }

    return null;
  }

  private boolean isGenuineFibonacciRequest(RequestContext context, String path) {
    return path.endsWith(FIBONACCI_PATH) && context.sendZuulResponse();
  }

  private void returnResultWithoutForwardingToZuul(RequestContext context, Long value) {
    context.setResponseStatusCode(SC_OK);
    context.setResponseBody(value.toString());
    context.setSendZuulResponse(false);
  }
}
