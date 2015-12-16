package net.sitemorph.crawler;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;

import org.testng.annotations.Test;

/**
 * Test basic operational aspects of the http client fetcher.
 *
 * @author
 */
public class HttpClientFetcherTest {

  @Test
  public void testRedirect() throws MalformedURLException {
    LinkFetcher fetcher = new HttpClientFetcher("SiteMorph Crawler");
    Spider.UrlTarget target = new Spider.UrlTarget(null,
        new URL("http://www.sitemorph.co.uk/"));
    Response response = fetcher.fetchUrl(target);
    assertEquals(response.getStatusCode(), 301, "Expected redirect");
    assertNotNull(response.getHeaderValues("Location"));
  }


}
