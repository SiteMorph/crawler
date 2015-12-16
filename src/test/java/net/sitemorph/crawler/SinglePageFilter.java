/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

/**
 * A post fetch filter that stops after storing just one page.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public class SinglePageFilter implements PostFetchFilter {

  boolean isFirst = true;
  int calledCount = 0;

  @Override
  public boolean storeResponse(Spider spider, Response response) {
    calledCount++;
    if (isFirst) {
      isFirst = false;
      return true;
    } else {
      spider.clearQueue();
      return false;
    }
  }

  int calledCount() {
    return calledCount;
  }
}
