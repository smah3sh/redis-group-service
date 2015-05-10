package com.glomming.shared.sgs.test;

import com.amazonaws.util.json.JSONException;
import com.glomming.shared.sgs.controller.RedisGroupServiceController;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;


/**
 * REST helper class
 */
public class RestHelper extends AbstractIntegrationTest {

  protected MockHttpServletRequestBuilder prepareCreateRequest(String id, String attributes) throws Exception {
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/sgs/v1/" + RedisGroupServiceController.GROUP + "/create");
    request.param("gameId", APPID);
    request.param("id", id);
    setJsonString(request, attributes);
    return request;
  }

  protected MockHttpServletRequestBuilder prepareUpdateRequest(String id, String attributes) throws Exception {
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/sgs/v1/" + RedisGroupServiceController.GROUP + "/update");
    request.param("gameId", APPID);
    request.param("id", id);
    setJsonString(request, attributes);
    return request;
  }

  protected MockHttpServletRequestBuilder prepareReadRequest(String id, List<String> attributes) throws Exception {
    StringBuilder sb = new StringBuilder();
    for (String attribute : attributes) {
      sb.append(attribute + ",");
    }
    if (sb.length() > 2)
      sb.deleteCharAt(sb.length() - 1);
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/sgs/v1/" + RedisGroupServiceController.GROUP + "/read");
    request.param("gameId", APPID);
    request.param("id", id);
    request.param(null, sb.toString());
    return request;
  }

  protected MockHttpServletRequestBuilder prepareReadAllRequest(String id) throws JSONException {
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/sgs/v1/" + RedisGroupServiceController.GROUP + "/read/all");
    request.param("gameId", APPID);
    request.param("id", id);
    return request;
  }


  protected MockHttpServletRequestBuilder prepareDeleteRequest(String id, List<String> attributes) throws Exception {
    StringBuilder sb = new StringBuilder();
    for (String attribute : attributes) {
      sb.append(attribute + ",");
    }
    if (sb.length() > 2)
      sb.deleteCharAt(sb.length() - 1);
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/sgs/v1/" + RedisGroupServiceController.GROUP + "/delete");
    request.param("gameId", APPID);
    request.param("id", id);
    request.param(null, sb.toString());
    return request;
  }

  protected MockHttpServletRequestBuilder prepareDeleteAllRequest(String id) throws JSONException {
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/sgs/v1/" + RedisGroupServiceController.GROUP + "/delete/all");
    request.param("gameId", APPID);
    request.param("id", id);
    return request;
  }

}
