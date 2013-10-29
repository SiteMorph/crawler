/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Use Jsoup to parse a document and rewrite the links relative to the URL
 * and return them.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public class JsoupLinkExtractor extends AbstractLinkExtractor {

  private static final String HREF = "href";

  @Override
  protected Set<URL> extractLinkStatusOk(Response content) {
    Document doc = Jsoup.parse(content.getBody(),
        content.getUrl().toExternalForm());
    Elements links = doc.select("link, a");
    Set<URL> result = new HashSet<URL>();
    for (Element element : links) {
      if (!element.hasAttr(HREF)) {
        continue;
      }
      try {
        // Add trailing slash (semantically same) for
        URL url = new URL(element.absUrl(HREF));
        if (url.getProtocol().startsWith("http") &&
            "".equals(url.getPath())) {
          url = new URL(url.toExternalForm() + "/");
        }
        result.add(url);
      } catch (MalformedURLException e) {
        log.debug("Error creating URL from href {}", element.attr(HREF));
      }
    }
    return result;
  }
}
