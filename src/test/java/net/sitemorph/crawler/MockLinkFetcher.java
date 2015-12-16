/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import net.sitemorph.crawler.Spider.UrlTarget;

/**
 * Mock link fetcher returns the same content for all urls.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public class MockLinkFetcher implements LinkFetcher {

  private static Response response;
  static {
    Response.Builder builder = new Response.Builder();
    builder.setStatusCode(StatusCode.OK)
        .addHeader("Mime-Type", "text/html")
        .setBody("<html><a href=\"http://www.sitemorph.net/\">Site Morph</a>" +
            "</html>");
    response =  builder.build();
  }

  public static Response getResponse() {
    return response;
  }

  @Override
  public Response fetchUrl(UrlTarget url) {
    return response;
  }

  @Override
  public String getAgentString() {
    return "testagent";
  }
}
