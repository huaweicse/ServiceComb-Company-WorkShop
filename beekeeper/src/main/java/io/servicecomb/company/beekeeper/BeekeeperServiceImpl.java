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

/**
 * Implements {@link BeekeeperService} to get fibonacci term from remote worker.
 */
class BeekeeperServiceImpl implements BeekeeperService {

  private final FibonacciCalculator fibonacciCalculator;

  BeekeeperServiceImpl(FibonacciCalculator fibonacciCalculator) {
    this.fibonacciCalculator = fibonacciCalculator;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long ancestorsOfDroneAt(int generation) {
    return fibonacciCalculator.term(generation);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long ancestorsOfQueenAt(int generation) {
    // please refer to the link in javadoc for the reason of + 2
    return fibonacciCalculator.term(generation + 2);
  }
}
