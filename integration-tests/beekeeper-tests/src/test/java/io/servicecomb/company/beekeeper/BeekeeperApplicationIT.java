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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = BeekeeperApplication.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
    })
public class BeekeeperApplicationIT {

  private final RestTemplate restTemplate = new RestTemplate();

  private final String serviceAddress = "http://localhost:8090";

  @Test
  public void getsAncestorsOfDroneAtGenerationN() {
    ResponseEntity<Ancestor> responseEntity = restTemplate.getForEntity(
        serviceAddress + "/rest/drone/ancestors/{generation}",
        Ancestor.class,
        30);

    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    assertThat(responseEntity.getBody().getAncestors()).isEqualTo(832040L);
  }

  @Test
  public void getsAncestorsOfQueenAtGenerationN() {
    ResponseEntity<Ancestor> responseEntity = restTemplate.getForEntity(
        serviceAddress + "/rest/queen/ancestors/{generation}",
        Ancestor.class,
        30);

    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    assertThat(responseEntity.getBody().getAncestors()).isEqualTo(2178309L);
  }
}

