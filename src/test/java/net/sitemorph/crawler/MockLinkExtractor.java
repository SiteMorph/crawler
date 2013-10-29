/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Mock link extractor always returns just one link to the same page.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public class MockLinkExtractor implements LinkExtractor {

  public static Set<URL> result = new HashSet<URL>();
  static {
    try {
      result.add(new URL("http://www.sitemorph.net/"));
      result.add(new URL("http://www.google.com/"));
    } catch (MalformedURLException e) {
      throw new Error("Mock url invalid");
    }
  }

  @Override
  public Set<URL> extractLinks(Response content) {
    return result;
  }
}
