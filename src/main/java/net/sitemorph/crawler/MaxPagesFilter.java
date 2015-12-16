/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

/**
 * Wait until the required number of pages have been fetched and exit. Note that
 * this filter should be applied after all other post fetch filters immediately
 * before saving to count the number of saved pages. If used first it will
 * limit the number of fetched uris which will also include non page and
 * redirect instances.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public class MaxPagesFilter implements PostFetchFilter {

  private int maxPages;
  private int seenPages = 0;

  public MaxPagesFilter(int maxPages) {
    this.maxPages = maxPages;
  }

  @Override
  public boolean storeResponse(Spider spider, Response response) {
    seenPages++;
    if (seenPages <= maxPages) {
      return true;
    }
    spider.clearQueue();
    return false;
  }
}
