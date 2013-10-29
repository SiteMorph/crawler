/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import net.sitemorph.crawler.Spider.Builder;
import org.testng.annotations.Test;

/**
 * Crawler system tests.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public class CrawlerTest {

  @Test(expectedExceptions = {IllegalStateException.class})
  public void testSpiderBuilderError() {
    Spider.Builder builder = new Builder();
    builder.build();
  }

  private Spider getTestSpider(PostFetchFilter filter)
      throws MalformedURLException {

    Spider.Builder builder = new Builder();
    builder.addPostFetchFilter(filter)
        .setLinkExtractor(new MockLinkExtractor())
        .setLinkFetcher(new MockLinkFetcher())
        .setPageStore(new MockPageStore())
        .setSeedPage(new URL("http://www.sitemorph.net/"));
    return builder.build();
  }

  private Spider getTestSpider(PreFetchFilter pre, PostFetchFilter post)
      throws MalformedURLException {
    Spider.Builder builder = new Builder();
    builder.addPreFetchFilter(pre)
        .addPostFetchFilter(post)
        .setLinkExtractor(new MockLinkExtractor())
        .setLinkFetcher(new MockLinkFetcher())
        .setPageStore(new MockPageStore())
        .setSeedPage(new URL("http://www.sitemorph.net/"));
    return builder.build();
  }

  @Test
  public void testPostFilterCalledOnceThenExit() throws MalformedURLException {
    SinglePageFilter filter = new SinglePageFilter();
    Spider spider = getTestSpider(filter);
    spider.run();
    assertEquals(filter.calledCount(), 2);
  }

  @Test
  public void testSpiderClearNoFetches() throws MalformedURLException {
    SinglePageFilter filter = new SinglePageFilter();
    Spider spider = getTestSpider(filter);
    spider.clearQueue();
    spider.run();
    assertEquals(filter.calledCount(), 0);
  }

  @Test
  public void testUrlsEnqued() throws MalformedURLException {
    PostFetchFilter post = new SinglePageFilter();
    CollectingPreFilter pre = new CollectingPreFilter();
    Spider spider = getTestSpider(pre, post);
    spider.run();
    assertEquals(pre.shouldVisitCalled(), MockLinkExtractor.result);
  }

  @Test
  public void testTimingAdded() throws MalformedURLException {
    Spider.Builder builder = new Builder();
    builder.setLinkExtractor(new MockLinkExtractor())
        .setLinkFetcher(new MockLinkFetcher())
        .setPageStore(new MockPageStore())
        .setMinimumTimeBetweenRequests(1000)
        .setSeedPage(new URL("http://www.sitemorph.net/"))
        .addPostFetchFilter(new SinglePageFilter());
    Spider spider = builder.build();
    long start = System.currentTimeMillis();
    spider.run();
    long stop = System.currentTimeMillis();
    // there should have been two post fetch calls (always 1 plus second)
    assertTrue((stop - start) >= 1000, "Delay was " + (stop - start));
  }

  @Test
  public void testStoredResponses() throws MalformedURLException {
    MockPageStore store = new MockPageStore();
    Spider.Builder builder = new Builder();
    builder.setLinkExtractor(new MockLinkExtractor())
        .setPageStore(store)
        .setLinkFetcher(new MockLinkFetcher())
        .setSeedPage(new URL("http://www.sitemorph.net"))
        .addPostFetchFilter(new SinglePageFilter());
    Spider spider = builder.build();
    spider.run();
    List<Response> responses = store.getResponses();
    assertTrue(responses.size() == 1, "Expected one response");
    Response test = responses.get(0);
    assertEquals(test.getBody(), MockLinkFetcher.getResponse().getBody());
    assertEquals(test.getStatusCode(),
        MockLinkFetcher.getResponse().getStatusCode());
  }
}
