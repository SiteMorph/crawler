/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import static net.sitemorph.crawler.AbstractLinkExtractor.qualifyUrl;
import static org.testng.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import org.testng.annotations.Test;

/**
 * Tests for the link extractor to cover 301 / 301 relative urls
 */
public class AbstractLinkExtractorTest {

  private URL getBase() {
    try {
      return new URL("http://www.sitemorph.net/app-home");
    } catch (MalformedURLException e) {
      throw new Error("bad url");
    }
  }

  private URL url(String url) {
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw new Error("bad url " + url);
    }
  }

  @Test
  public void testDifferentHost() throws MalformedURLException {
    assertEquals(qualifyUrl(getBase(), "http://www.google.com/"),
        url("http://www.google.com/"));
  }

  @Test
  public void testSameHostPath() throws MalformedURLException {
    assertEquals(qualifyUrl(getBase(), "http://www.sitemorph.net/blog"),
        url("http://www.sitemorph.net/blog"));
  }

  @Test
  public void testSameHostParameter() throws MalformedURLException {
    assertEquals(qualifyUrl(getBase(), "http://www.sitemorph.net/blog?a=1"),
        url("http://www.sitemorph.net/blog?a=1"));
  }

  @Test
  public void testRelativeAbsolutePath() throws MalformedURLException {
    assertEquals(qualifyUrl(getBase(), "/faq"),
        url("http://www.sitemorph.net/faq"));
  }

  @Test
  public void testRelativeSameHomeRelativePath() throws MalformedURLException {
    assertEquals(qualifyUrl(getBase(), "../about"),
        url("http://www.sitemorph.net/../about"));
    // this behaviour is strange but want to know if it ever changes.
  }

}
