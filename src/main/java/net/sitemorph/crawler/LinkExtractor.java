/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.net.URL;
import java.util.Set;

/**
 * Interface for a processor that extracts the collection of link urls
 * from a document.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public interface LinkExtractor {

  /**
   * Processes page content to extract the links from it.
   *
   *
   * @param content to process
   * @return Set of Urls contained in the page content
   */
  public Set<URL> extractLinks(Response content);
}
