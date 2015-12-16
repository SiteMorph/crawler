/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.net.URL;

/**
 * A filter which can be used to block crawling of a page before the fetch
 * is executed. The purpose of this filter is to restrict which URLs are
 * added to the crawl queue. As such they only filter on URL.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public interface PreFetchFilter {

  public boolean shouldVisit(URL url);
}
