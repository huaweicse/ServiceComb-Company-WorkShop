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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/rest")
@Controller
public class BeekeeperController {

  private static final Logger logger = LoggerFactory.getLogger(BeekeeperController.class);

  private final BeekeeperService beekeeperService;

  @Autowired
  BeekeeperController(BeekeeperService beekeeperService) {
    this.beekeeperService = beekeeperService;
  }

  /**
   * calculates the number of ancestors of a drone (male bee) at specified generation.
   *
   * @param generation the generation of bee ancestors at query
   * @return the number of ancestors
   */
  @RequestMapping(value = "/drone/ancestors/{generation}", method = GET, produces = APPLICATION_JSON_VALUE)
  @ResponseBody
  public Ancestor ancestorsOfDrone(@PathVariable int generation) {
    logger.info(
        "Received request to find the number of ancestors of drone at generation {}",
        generation);

    return new Ancestor(beekeeperService.ancestorsOfDroneAt(generation));
  }

  /**
   * calculates the number of ancestors of a queen (female bee) at specified generation.
   *
   * @param generation the generation of bee ancestors at query
   * @return the number of ancestors
   */
  @RequestMapping(value = "/queen/ancestors/{generation}", method = GET, produces = APPLICATION_JSON_VALUE)
  @ResponseBody
  public Ancestor ancestorsOfQueen(@PathVariable int generation) {
    logger.info(
        "Received request to find the number of ancestors of queen at generation {}",
        generation);

    return new Ancestor(beekeeperService.ancestorsOfQueenAt(generation));
  }
}
