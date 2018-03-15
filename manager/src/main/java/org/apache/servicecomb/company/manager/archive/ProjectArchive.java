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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

/**
 * A generic project archive to cache user requests.
 *
 * @param <K> key of cache entry
 * @param <V> value of cache entry
 */
@CacheConfig(cacheNames = "projectArchive")
public class ProjectArchive<K, V> {
  private static final Logger logger = LoggerFactory.getLogger(ProjectArchive.class);

  /**
   * Search the cache for matching result of provided key. This method will not be executed unless
   * there is no such key in the cache. In this case, {@link MissArchive} will always be returned to
   * indicate a cache miss. Otherwise, {@link HitArchive} will be retrieved from the cache using
   * {@link Cacheable}
   *
   * @param key the key of the cache entry to be found
   * @return {@link Archive} of cache entry
   * @see Cacheable
   */
  @Cacheable
  public Archive<V> search(K key) {
    logger.info("Cache miss with key: {}", key);
    return new MissArchive<>(key.toString());
  }

  /**
   * Update the cache with provided key and value. This method will always be executed.
   *
   * @param key the key of the cache entry to be updated
   * @param value the value of the cache entry to be updated
   * @return {@link HitArchive} of the cache entry
   * @see CachePut
   */
  @CachePut(key = "#key")
  public Archive<V> archive(K key, V value) {
    logger.info("Updated cache with key {} and value {}", key, value);
    return new HitArchive<>(value);
  }
}
