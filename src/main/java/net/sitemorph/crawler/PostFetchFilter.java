/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

/**
 * Filter a page after it has been fetched to decide if it should be stored.
 * As well as filtering the individual pages this filter can also be uesd
 * to stop the crawl task from proceeding in cases like having successfully
 * downloaded the required number of pages.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public interface PostFetchFilter {

  public boolean storeResponse(Spider spider, Response response);
}
