/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.net.URL;

/**
 * Robots filter using an underlying robots spec from a response.
 *
 * @author dak
 */
public class RobotsFilter implements PreFetchFilter {

  private RobotsTxt robots;
  private String agent;

  public RobotsFilter(Response response, String agent) {
    robots = RobotsTxt.fromResponse(response);
    this.agent = agent;
  }

  @Override
  public boolean shouldVisit(URL url) {
    StringBuilder path = new StringBuilder();
    path.append(url.getPath());
    if (null != url.getQuery()) {
      path.append("?")
          .append(url.getQuery());
    }
    return robots.pathIsAllowed(agent, path.toString());
  }
}
