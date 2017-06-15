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

package io.servicecomb.company.beekeeper;

import static com.seanyinx.github.unit.scaffolding.Randomness.nextInt;
import static com.seanyinx.github.unit.scaffolding.Randomness.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class BeekeeperServiceImplTest {

  private final int generation = nextInt(90) + 1;
  private final long fibonacciValue = nextLong();

  private final FibonacciCalculator fibonacciCalculator = mock(FibonacciCalculator.class);
  private final BeekeeperService beekeeperService = new BeekeeperServiceImpl(fibonacciCalculator);

  @Test
  public void calculatesAncestorsOfDroneAtGenerationN() {
    when(fibonacciCalculator.term(generation + 1)).thenReturn(fibonacciValue);

    long ancestors = beekeeperService.ancestorsOfDroneAt(generation);

    assertThat(ancestors).isEqualTo(fibonacciValue);
  }

  @Test
  public void calculatesAncestorsOfQueenAtGenerationN() {
    when(fibonacciCalculator.term(generation + 2)).thenReturn(fibonacciValue);

    long ancestors = beekeeperService.ancestorsOfQueenAt(generation);

    assertThat(ancestors).isEqualTo(fibonacciValue);
  }

  @Test
  public void ancestorsAtGeneration0Is0() {
    when(fibonacciCalculator.term(anyInt())).thenReturn(1L);

    assertThat(beekeeperService.ancestorsOfDroneAt(0)).isZero();
    assertThat(beekeeperService.ancestorsOfQueenAt(0)).isZero();
  }
}
