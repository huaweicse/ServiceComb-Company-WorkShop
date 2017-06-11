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

import io.servicecomb.provider.pojo.RpcSchema;

@RpcSchema(schemaId = "fibonacciService")
// class modifier has to be public, or producer invoker will fail to access it
public class FibonacciServiceImpl implements FibonacciService {

  @Override
  public long term(int n) {
    if (n < 0) {
      throw new IllegalArgumentException("Fibonacci term must not be negative: " + n);
    }

    return nthTerm(n, new long[]{-1, -1});
  }

  private long nthTerm(int n, long[] lastTwoFibos) {
    if (n == 0) {
      return 0;
    } else if (n == 1) {
      return 1;
    }

    if (!isCached(lastTwoFibos)) {
      cacheCalculatedFibo(nthTerm(n - 2, lastTwoFibos), lastTwoFibos);
      cacheCalculatedFibo(nthTerm(n - 1, lastTwoFibos), lastTwoFibos);
    }

    return lastTwoFibos[0] + lastTwoFibos[1];
  }

  private boolean isCached(long[] lastTwoFibos) {
    return lastTwoFibos[0] >= 0 && lastTwoFibos[1] >= 0;
  }

  private void cacheCalculatedFibo(long fibo, long[] lastTwoFibos) {
    lastTwoFibos[1] = lastTwoFibos[0];
    lastTwoFibos[0] = fibo;
  }
}
