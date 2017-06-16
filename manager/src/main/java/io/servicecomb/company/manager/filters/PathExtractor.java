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

import com.netflix.zuul.context.RequestContext;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
class PathExtractor {

  String path(RequestContext context) {
    HttpServletRequest request = context.getRequest();
    StringBuilder builder = new StringBuilder();

    builder.append(request.getContextPath()).append(request.getServletPath());
    if (request.getPathInfo() != null) {
      builder.append(request.getPathInfo());
    }

    if (context.getRequestQueryParams() != null) {
      appendQueryParams(context, builder);
    }

    return builder.toString();
  }

  private void appendQueryParams(RequestContext context, StringBuilder builder) {
    List<String> queryParams = new LinkedList<>();

    context.getRequestQueryParams()
        .forEach((key, values) -> values
            .forEach(value -> queryParams.add(key + "=" + value)));

    builder.append("?").append(String.join("&", queryParams));
  }
}
