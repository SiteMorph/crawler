/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.net.URL;

/**
 * Visit pages from the same domain only.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public class SameDomainFilter implements PreFetchFilter {

  private URL domain;

  public SameDomainFilter(URL domain) {
    this.domain = domain;
  }

  @Override
  public boolean shouldVisit(URL url) {
    return domain.getHost().equals(url.getHost()) &&
        domain.getPort() == url.getPort();
  }
}
