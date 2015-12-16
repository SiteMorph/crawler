/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Decide whether to save a fetched URI based on it's mime type. Only html
 * type mime types are accepted.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public class HtmlMimeTypeFilter implements PostFetchFilter {

  private static final Set<String> HTML_MIMES = new HashSet<String>();
  static {
    HTML_MIMES.add("text/html");
    HTML_MIMES.add("application/xhtml+xml");
    HTML_MIMES.add("text/x-server-parsed-html");
  }

  @Override
  public boolean storeResponse(Spider spider, Response response) {
    // check for the header 'Content-Type'
    List<String> contentTypes = response.getHeaderValues("Content-Type");
    if (null == contentTypes) {
      return false;
    }
    for (String content : contentTypes) {
      for (String mime : HTML_MIMES) {
        if (content.contains(mime)) {
          return true;
        }
      }
    }
    return false;
  }
}
