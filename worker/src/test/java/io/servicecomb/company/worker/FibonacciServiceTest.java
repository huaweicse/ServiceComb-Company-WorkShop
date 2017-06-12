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

import static com.seanyinx.github.unit.scaffolding.AssertUtils.expectFailing;
import static com.seanyinx.github.unit.scaffolding.Randomness.nextInt;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class FibonacciServiceTest {

  private final int[] expected = {
      0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181, 6765,
      10946, 17711, 28657, 46368, 75025, 121393, 196418, 317811
  };

  private final FibonacciService calculator = new FibonacciServiceImpl();

  @Test
  public void calculatesFibonacciOfTermN() {
    for (int i = 0, length = expected.length; i < length; i++) {
      long fib = calculator.term(i);

      assertThat(fib).isEqualTo(expected[i]);
    }
  }

  @Test
  public void blowsUpWhenInputIsNegative() {
    int term = -nextInt(128) - 1;
    try {
      calculator.term(term);
      expectFailing(IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Fibonacci term must not be negative: " + term);
    }
  }

  @Test
  public void calculatesFibonacciOfTerm90() {
    long fibo = calculator.term(90);

    assertThat(fibo).isEqualTo(2880067194370816120L);
  }
}
