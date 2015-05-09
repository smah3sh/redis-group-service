package com.glomming.shared.sgs.test;

import com.glomming.shared.sgs.controller.RedisGroupServiceController;
import com.glomming.shared.sgs.service.SimpleGroupService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {SimpleGroupServiceTestConfiguration.class, MockServletContext.class})
@WebAppConfiguration
@IntegrationTest
public class SimpleGroupServiceRestTest extends RestHelper {

  private static final Logger logger = LoggerFactory.getLogger(RedisGroupServiceController.class);

  @Autowired
  SimpleGroupService simpleGroupService;

  @Autowired
  protected RedisGroupServiceController dynamoRestServiceController;

  @Override
  public Object getController() {
    return dynamoRestServiceController;
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @After
  public void tearDown() throws Exception {
    cleanUp();
  }

  private void cleanUp() throws Exception {
  }

  @Test
  public void testCreate() throws Exception {
    String id = UUID.randomUUID().toString();
    String nonce = UUID.randomUUID().toString();
    String requestAttributes = SimpleGroupServiceTestHelper.createAttributes(null);
    MockHttpServletRequestBuilder createRequest = prepareCreateRequest(id, requestAttributes);
    createRequest.header(null, nonce);
    ResultActions resultActions = mvc.perform(createRequest).andExpect(status().isOk());
    String response = getResponseString(resultActions);
    Assert.assertEquals(getResponseHeader(resultActions, null), nonce);


    // Now read it all back
    nonce = UUID.randomUUID().toString();
    MockHttpServletRequestBuilder readAllRequest = prepareReadAllRequest(id);
    readAllRequest.header(null, nonce);
    resultActions = mvc.perform(readAllRequest).andExpect(status().isOk());
    response = getResponseString(resultActions);
    Assert.assertEquals(getResponseHeader(resultActions, null), nonce);
    String responseAttributes = null;
    Assert.assertTrue(SimpleGroupServiceTestHelper.checkIfAttributesAreEqual(requestAttributes, responseAttributes));

  }


}
