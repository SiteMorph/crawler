/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import net.sitemorph.crawler.Spider.UrlTarget;

/**
 * Fetch the content of a url.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public interface LinkFetcher {

  public Response fetchUrl(UrlTarget url);

  public String getAgentString();
}
