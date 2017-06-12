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

package io.servicecomb.company.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.seanyinx.github.unit.scaffolding.Randomness;
import org.junit.Test;
import org.mockito.Mockito;

public class FibonacciRpcEndpointTest {

  private final FibonacciService fibonacciService = Mockito.mock(FibonacciService.class);

  private final long expected = Randomness.nextLong();
  private final int term = Randomness.nextInt();

  private final FibonacciRpcEndpoint fibonacciRpcApi = new FibonacciRpcEndpoint(fibonacciService);

  @Test
  public void providesFibonacciTermWithUnderlyingService() {
    when(fibonacciService.term(term)).thenReturn(expected);

    long fibo = fibonacciRpcApi.term(term);

    assertThat(fibo).isEqualTo(expected);
  }
}