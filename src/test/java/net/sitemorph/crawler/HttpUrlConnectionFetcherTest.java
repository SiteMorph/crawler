/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import net.sitemorph.crawler.Spider.UrlTarget;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.testng.Assert.assertEquals;

/**
 * Check status codes.
 *
 * @author dak
 */
public class HttpUrlConnectionFetcherTest {

  @Test
  public void testNotFound() throws MalformedURLException {
    URL toFetch = new URL("http://localhost:8080/somethingnotthere"),
        referrer = new URL("http://localhost:8080/");
    UrlTarget target = new UrlTarget(referrer, toFetch);
    HttpUrlConnectionFetcher fetcher = new HttpUrlConnectionFetcher();
    Response response = fetcher.fetchUrl(target);
    assertEquals(response.getUrl(), toFetch);
    assertEquals(response.getReferrer(), referrer);
    assertEquals(response.getStatusCode(), StatusCode.NOT_FOUND);
  }

  @Test
  public void testUrlPathNull() throws MalformedURLException {
    URL test = new URL("http://localhost:8080");
    assertEquals(test.getPath(), "");
  }
}
