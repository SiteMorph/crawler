/*
 * Copyright (c) 2013. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

/**
 * Get a new link fetcher.
 *
 * @author dak
 */
public class LinkFetcherFactory {

  public static LinkFetcher getDetault(String agent) {
    return getHttpClientFetcher(agent);
  }

  public static LinkFetcher getDetault() {
    return getHttpClientFetcher();
  }

  public static LinkFetcher getHttpClientFetcher(String agent) {
    return new HttpClientFetcher(agent);
  }

  public static LinkFetcher getHttpClientFetcher() {
    return new HttpClientFetcher();
  }

  public static LinkFetcher getUrlConnectionFetcher() {
    return new HttpUrlConnectionFetcher();
  }
}
