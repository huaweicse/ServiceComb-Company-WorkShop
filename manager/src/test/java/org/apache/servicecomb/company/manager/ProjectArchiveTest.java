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
package org.apache.servicecomb.company.manager;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.servicecomb.company.manager.archive.Archive;
import org.apache.servicecomb.company.manager.archive.ProjectArchive;
import org.junit.Test;

public class ProjectArchiveTest {

  private final ProjectArchive<Integer, Long> archive = new ProjectArchive<>();

  @Test
  public void alwaysMissOnSearch() {
    Archive<Long> result = archive.search(1);

    assertThat(result.exists()).isFalse();
  }

  @Test
  public void alwaysHitOnArchive() {
    Archive<Long> result = archive.archive(1, 1L);

    assertThat(result.exists()).isTrue();
    assertThat(result.get()).isEqualTo(1L);
  }
}
