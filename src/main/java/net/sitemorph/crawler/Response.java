/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sitemorph.crawler.Spider.UrlTarget;

/**
 * Response from the server which may include a body as well as headers
 * and a status code.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public class Response {

  public static final String CONTENT_TYPE_HEADER = "Content-Type";
  private static Response error;
  static {
    error = new Builder().setStatusCode(StatusCode.SERVER_ERROR)
        .setBody("")
        .build();
  }

  private StatusCode statusCode;
  private Map<String, List<String>> headers;
  private String body;
  private URL url, referrer;

  private Response() {}

  public static Response serverError() {
    return error;
  }

  public static Response ioException(final StatusCode statusCode,
      final Map<String, List<String>> headers, final UrlTarget url,
      final IOException e) {
    return new Response() {
      @Override
      public StatusCode getStatusCode() {
        return statusCode;
      }

      @Override
      public Set<String> getHeaderKeys() {
        return headers.keySet();
      }

      @Override
      public List<String> getHeaderValues(String key) {
        return headers.get(key);
      }

      @Override
      public boolean hasHeader(String key) {
        return headers.containsKey(key);
      }

      @Override
      public String getBody() {
        return e.getMessage();
      }

      @Override
      public URL getUrl() {
        return url.getTarget();
      }

      @Override
      public URL getReferrer() {
        return url.getReferrer();
      }
    };
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("response : {")
        .append("url : '")
        .append(null == url ? null : url.toExternalForm())
        .append("' referrer : '")
        .append(null == referrer ? null : referrer.toExternalForm())
        .append("' statusCode : '")
        .append(null == statusCode ? null : statusCode.toString())
        .append("}");
    return builder.toString();
  }

  public StatusCode getStatusCode() {
    return statusCode;
  }

  public Set<String> getHeaderKeys() {
    return headers.keySet();
  }

  public List<String> getHeaderValues(String key) {
    return headers.get(key);
  }

  public boolean hasBody() {
    return null != body;
  }

  public String getBody() {
    return body;
  }

  public URL getReferrer() {
    return referrer;
  }

  public URL getUrl() {
    return url;
  }

  public boolean hasHeader(String header) {
    return headers.containsKey(header);
  }

  public static class Builder {
    private Response response;

    public Builder() {
      response = new Response();
      response.headers = new HashMap<String, List<String>>();
    }

    public Response build() {
      return response;
    }

    public Builder setStatusCode(StatusCode code) {
      response.statusCode = code;
      return this;
    }

    public Builder addHeader(String header, String value) {
      if (!response.headers.containsKey(header)) {
        response.headers.put(header, new ArrayList<String>(1));
      }
      response.headers.get(header).add(value);
      return this;
    }

    public Builder setBody(String body) {
      response.body = body;
      return this;
    }

    public Builder setHeaders(Map<String, List<String>> headerFields) {
      response.headers = headerFields;
      return this;
    }

    public Builder setUrl(URL url) {
      response.url = url;
      return this;
    }

    public Builder setReferrer(URL referrer) {
      response.referrer = referrer;
      return this;
    }
  }
}
