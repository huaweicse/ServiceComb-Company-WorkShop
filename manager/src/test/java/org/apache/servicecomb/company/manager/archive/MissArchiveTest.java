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
package org.apache.servicecomb.company.manager.archive;

import static com.seanyinx.github.unit.scaffolding.AssertUtils.expectFailing;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.NoSuchElementException;
import org.junit.Test;

public class MissArchiveTest {

  private final String key = uniquify("key");
  private final Archive<String> archive = new MissArchive<>(key);

  @Test
  public void blowsUpWhenGet() {
    assertThat(archive.exists()).isFalse();

    try {
      archive.get();
      expectFailing(NoSuchElementException.class);
    } catch (NoSuchElementException e) {
      assertThat(e.getMessage()).isEqualTo("No result found for search term " + key);
    }
  }
}