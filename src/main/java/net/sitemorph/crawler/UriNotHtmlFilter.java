/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.net.URL;
import java.util.regex.Pattern;

/**
 * Skip URIs that are not associated with html by file extension.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public class UriNotHtmlFilter implements PreFetchFilter {

  final static Pattern FILTERS = Pattern.compile(
      ".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4"
          + "|wav|avi|mov|mpeg|ram|m4v|pdf" +
          "|rm|smil|wmv|swf|wma|zip|rar|gz|ai|idml|pdf|qxd|indt|" +
          "ico))$");

  @Override
  public boolean shouldVisit(URL url) {
    return !FILTERS.matcher(url.toExternalForm()).matches();
  }
}
