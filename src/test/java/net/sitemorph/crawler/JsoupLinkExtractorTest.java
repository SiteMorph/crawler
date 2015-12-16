/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import static org.testng.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import net.sitemorph.crawler.Response.Builder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.testng.annotations.Test;

/**
 * Test the jsoup link extractor.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public class JsoupLinkExtractorTest {

  private static final String HTML =
      "<html>" +
        "<head>" +
          "<link href=\"/main.css\" />" +
        "</head>" +
        "<body>" +
          "<a href=\"/index.jsp?a=v#ref\">Some Link</a>" +
          "<a href=\"./secondary.php\">Secondary Link</a>" +
          "<a href=\"#somePlace\">Bookmark reference</a>" +
        "</body>" +
      "</html>",
      BASE = "http://localhost/somepath/";



  @Test
  public void testLinkAnchorBothReturned() {
    Document doc = Jsoup.parse(HTML);
    Elements links = doc.select("link, a");
    assertEquals(links.size(), 4, "Expected 2 elements");
  }

  @Test
  public void testLinkHrefRewritten() {
    Response.Builder builder = new Builder();
    try {
      builder.setBody(HTML)
          .setStatusCode(StatusCode.OK)
          .setUrl(new URL(BASE));
    } catch (MalformedURLException e) {
      throw new Error("Base is invalid", e);
    }

    LinkExtractor toTest = new JsoupLinkExtractor();

    Set<URL> expected = new HashSet<URL>();
    try {
      expected.add(new URL("http://localhost/main.css"));
      expected.add(new URL("http://localhost/index.jsp?a=v#ref"));
      expected.add(new URL("http://localhost/somepath/secondary.php"));
      expected.add(new URL("http://localhost/somepath/#somePlace"));
    } catch (MalformedURLException e) {
      throw new Error("Could not parse test url", e);
    }
    assertEquals(toTest.extractLinks(builder.build()), expected);
  }
}
