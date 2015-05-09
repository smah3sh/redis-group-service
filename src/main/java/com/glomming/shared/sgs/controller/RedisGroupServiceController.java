package com.glomming.shared.sgs.controller;

import com.glomming.shared.sgs.exception.BaseException;
import com.glomming.shared.sgs.service.SimpleGroupService;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.glomming.shared.sgs.ServiceName.SERVICE_PATH;

@ManagedResource(description = "Dynamo REST service REST Controller")
@RestController
@EnableAutoConfiguration
@RequestMapping(SERVICE_PATH)
public class RedisGroupServiceController {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RedisGroupServiceController.class);

  private SimpleGroupService simpleGroupService;

  public static final String GROUP = "group";
  public static final String USER = "user";

  /**
   * Helper method to print out complete request details
   *
   * @param path
   * @param id
   * @param gameId
   * @param nonce
   * @param requestBody
   * @return
   */
  public static String requestToString(String path, String id, String gameId, String nonce, String requestParams, String requestBody) {
    StringBuilder sb = new StringBuilder();
    sb.append("path: " + path + ",\n");
    sb.append("id: " + id + ",\n");
    sb.append("gameId: " + gameId + ",\n");
    sb.append("nonce: " + nonce + ",\n");
    if (!StringUtils.isEmpty(requestParams))
      sb.append("requestParams: " + requestParams + ",\n");
    if (!StringUtils.isEmpty(requestBody))
      sb.append("requestBody: " + requestBody);
    return sb.toString();
  }

  @Autowired
  public void setStringAttributeService(SimpleGroupService service) {
    this.simpleGroupService= service;
  }

  @ApiOperation(value = "create", notes = "Create a new  item by id and namespace. Will overwrite existing item.")
  @RequestMapping(value = "/" + GROUP + "/create", method = RequestMethod.POST, produces = "application/json")
  @ResponseStatus(value = HttpStatus.OK)
  public
  @ResponseBody
  ResponseEntity<String> create(HttpServletRequest request,
                                   @ApiParam("Unique id within a game like a playerId") @RequestParam(value = "id", required = true) String id,
                                   @ApiParam("GameId") @RequestParam(value = "gameId", required = true) String gameId,
                                   @RequestBody String jsonAttributes)
      throws BaseException {

    long startTime = System.currentTimeMillis(), endTime = 0;
    ResponseEntity<String> responseEntity = null;
    String description = null;
    HttpStatus httpStatus = HttpStatus.OK;
    String nonce = null;

    try {
      logger.info("REQUEST::", requestToString(request.getContextPath(), id, gameId, nonce, null, jsonAttributes));
      httpStatus = HttpStatus.OK;
    } catch (Exception e) {
      logger.error("", e);
      description = e.getMessage();
      httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    } finally {
      endTime = System.currentTimeMillis();
      MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
      headers.add(null, nonce);
      responseEntity = new ResponseEntity<>(null, headers, httpStatus);
    }

    return responseEntity;
  }

}
 