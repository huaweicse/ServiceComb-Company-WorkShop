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
package org.apache.servicecomb.company.worker;

/**
 * {@link FibonacciService} provides the interface of actual fibonacci sequence calculation.
 */
interface FibonacciService {

  /**
   * @see FibonacciEndpoint#term(int)
   * @param n the index of fibonacci sequence
   * @return the nth term of fibonacci sequence
   */
  long term(int n);
}
