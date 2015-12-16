/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract link extractor handles 301 and 301 location redirect link generation
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public abstract class AbstractLinkExtractor implements LinkExtractor {

  public static final String LOCATION = "Location";

  protected final Logger log = LoggerFactory.getLogger(getClass());
  @Override
  public Set<URL> extractLinks(Response content) {
    switch (content.getStatusCode()) {
      case OK : return extractLinkStatusOk(content);
      case MOVED_PERMANENTLY :
      case FOUND_REDIRECT :
      case SEE_OTHER_REDIRECT :
        return location(content);
      default:
        log.error("Could not determine status for response. " +
            "Returning no links");
        return Collections.emptySet();
    }
  }

  private Set<URL> location(Response content) {
    Set<URL> result = new HashSet<URL>(1);
    List<String> locations = content.getHeaderValues(LOCATION);
    if (!locations.isEmpty()) {
      try {
        result.add(qualifyUrl(content.getUrl(), locations.get(0)));
      } catch (MalformedURLException e) {
        log.error("Could not parse URL from Location redirect value: " +
            locations.get(0), e);
      }
    }
    return result;
  }

  static URL qualifyUrl(URL url, String location) throws MalformedURLException {
    // handle absolute url case
    try {
      URI base = url.toURI();
      URI update = base.resolve(location);
      return update.toURL();
    } catch (URISyntaxException e) {
      throw new MalformedURLException("Could not create URI from url " + url);
    } catch (MalformedURLException e) {
      throw e;
    }
  }

  protected abstract Set<URL> extractLinkStatusOk(Response content);
}
