// Copyright 2012 Google Inc. All Rights Reserved.
package com.google.jstestdriver.server.gateway;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

/**
 * Represents a mock HTTP response sent by the JsTD Gateway.
 * @author rdionne@google.com (Robert Dionne)
 */
public class MockResponse {

  /**
   * JSON key identifying the HTTP status.
   */
  public static final String STATUS = "status";

  /**
   * JSON key identifying the HTTP response headers.
   */
  public static final String RESPONSE_HEADERS = "responseHeaders";

  /**
   * JSON key identifying the HTTP response content type.
   */
  public static final String CONTENT_TYPE = "contentType";

  /**
   * JSON key identifying the HTTP response text.
   */
  public static final String RESPONSE_TEXT = "responseText";

  final Integer status;
  final ImmutableMap<String, String> responseHeaders;
  final String contentType;
  final String responseText;

  /**
   * Determines whether an entry of the gateway configuration represents a mock
   * response or not.
   * @param entry A {@link JsonObject} representing a gateway configuration entry.
   * @return True iff the entry represents a mock response.
   */
  public static boolean entryDescribesMockResponse(final JsonObject entry) {
    return entry.has(STATUS)
        || entry.has(RESPONSE_HEADERS)
        || entry.has(CONTENT_TYPE)
        || entry.has(RESPONSE_TEXT);
  }

  /**
   * Builds a {@link MockResponse} from a gateway configuration entry.
   * @param entry A {@link JsonObject} representing a gateway configuration entry.
   * @return The {@link MockResponse}.
   */
  public static MockResponse buildFromEntry(final JsonObject entry) {
    Integer status = null;
    ImmutableMap<String, String> responseHeaders = null;
    String contentType = null;
    String responseText = null;

    if (entry.has(STATUS)) {
      status = entry.get(STATUS).getAsInt();
    }

    if (entry.has(RESPONSE_HEADERS)) {
      JsonObject jsonHeaders = entry.get(RESPONSE_HEADERS).getAsJsonObject();
      ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
      for (Entry<String, JsonElement> jsonEntry : jsonHeaders.entrySet()) {
        builder.put(jsonEntry.getKey(), jsonEntry.getValue().getAsString());
      }
      responseHeaders = builder.build();
    }

    if (entry.has(CONTENT_TYPE)) {
      contentType = entry.get(CONTENT_TYPE).getAsString();
    }

    if (entry.has(RESPONSE_TEXT)) {
      responseText = entry.get(RESPONSE_TEXT).getAsString();
    }

    return new MockResponse(status, responseHeaders, contentType, responseText);
  }

  public MockResponse(
      final Integer status,
      final ImmutableMap<String, String> responseHeaders,
      final String contentType,
      final String responseText) {
    this.status = status;
    this.responseHeaders = responseHeaders;
    this.contentType = contentType;
    this.responseText = responseText;
  }

  /**
   * Write the {@link MockResponse} to the {@link HttpServletResponse}.
   * @param response An {@link HttpServletResponse}.
   * @throws IOException if we are unable to write to the {@link HttpServletResponse}.
   */
  public void writeTo(final HttpServletResponse response) throws IOException {
    if (status != null) {
      response.setStatus(status);
    }
    if (responseHeaders != null) {
      for (Entry<String, String> entry : responseHeaders.entrySet()) {
        response.setHeader(entry.getKey(), entry.getValue());
      }
    }
    if (contentType != null) {
      response.setContentType(contentType);
    }
    if (responseText != null) {
      response.getWriter().write(responseText);
    }
  }
}
