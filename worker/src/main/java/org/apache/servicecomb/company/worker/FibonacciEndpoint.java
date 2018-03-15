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
 * {@link FibonacciEndpoint} provides the common interface for different endpoint implementations,
 * such as {@link FibonacciRestEndpoint} and {@link FibonacciRpcEndpoint}.
 *
 * It supports sequence up to roughly 90, because the sequence value will exceed the limit of Java
 * type long.
 *
 * The endpoints are supposed to be very thin. They parse requests from different protocols and
 * delegate them to the same calculation logic provided by {@link FibonacciService}.
 */
public interface FibonacciEndpoint {

  /**
   * Calculates the nth term of fibonacci sequence.
   * for example, the following statement outputs fibonacci sequence [0, 1, 1, 2, 3, 5]
   * <pre>
   *   {@code long[] sequence = {
   *      endpoint.term(0),
   *      endpoint.term(1),
   *      endpoint.term(2),
   *      endpoint.term(3),
   *      endpoint.term(4),
   *      endpoint.term(5)};
   *   System.out.println(Arrays.toString(sequence));
   *   }
   * </pre>
   *
   * @param n the index of fibonacci sequence
   * @return the nth term of fibonacci
   */
  long term(int n);
}
