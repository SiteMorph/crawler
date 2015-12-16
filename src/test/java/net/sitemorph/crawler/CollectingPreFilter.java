/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple pre filter that returns the collection of urls tested.
 *
 * @author dak
 */
public class CollectingPreFilter implements PreFetchFilter {

  public Set<URL> called = new HashSet<URL>();

  @Override
  public boolean shouldVisit(URL url) {
    called.add(url);
    return true;
  }

  public Set<URL> shouldVisitCalled() {
    return called;
  }
}
