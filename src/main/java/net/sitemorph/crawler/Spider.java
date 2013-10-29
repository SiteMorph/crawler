/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spider does the work of managing the crawl queue and using the fetcher
 * components, link extractor and store to execute a job.
 *
 * Note that this class is not thread safe.
 *
 * TODO 20121102 (dak) The crawler can go over
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public class Spider implements Runnable {

  private static final String LOCATION = "Location";
  private static final Set<StatusCode> REDIRECTS = new HashSet<StatusCode>();
  static {
    REDIRECTS.add(StatusCode.MOVED_PERMANENTLY);
    REDIRECTS.add(StatusCode.FOUND_REDIRECT);
    REDIRECTS.add(StatusCode.SEE_OTHER_REDIRECT);
  }

  private Logger log = LoggerFactory.getLogger(getClass());
  private long minimumTime = 0;
  private long nextRunAfter = 0;
  private Queue<UrlTarget> queue;
  private LinkExtractor extractor = null;
  private LinkFetcher fetcher = null;
  private PageStore store = null;
  private List<PreFetchFilter> preFetchFilters = null;
  private List<PostFetchFilter> postFetchFilters = null;
  private long maximumRunningTime = 0;


  private Spider() {}

  @Override
  public void run() {
    nextRunAfter = now();
    long startTime = now();
    log.info("Spider starting {} job at {}", queue.peek(), new DateTime(now()));
    while (!queue.isEmpty()) {
      long now = now();
      long runningTime = now - startTime;
      if (maximumRunningTime > 0 && runningTime > maximumRunningTime) {
        break;
      }
      if (now < nextRunAfter) {
        try {
          Thread.sleep(nextRunAfter - now);
        } catch (InterruptedException e) {
          throw new RuntimeException("Spider was interrupted waiting for " +
              "next fetch cycle.");
        }
      }
      UrlTarget candidate = queue.poll();
      log.info("Spider Fetching {}", candidate.getTarget());
      store.markVisited(candidate.getTarget());
      Response response = fetcher.fetchUrl(candidate);
      nextRunAfter += minimumTime;
      if (skipByPostFilter(response)) {
        log.debug("Skipped save due to post filter");
        continue;
      }
      store.saveResponse(response);
      if (response.getStatusCode() == StatusCode.OK) {
        Set<URL> urls = extractor.extractLinks(response);
        log.debug("Extracted {} links", urls.size());
        enqueueUrls(candidate.getTarget(), urls);
      } else if (REDIRECTS.contains(response.getStatusCode())) {
        if (response.hasHeader(LOCATION)) {
          enqueueUrls(candidate.getTarget(), extractLocationUrls(
              response.getHeaderValues(LOCATION), candidate.getTarget()));
        }
      }
      // if the last response was 'unavailable' then stop trying
      if (StatusCode.UNAVAILABLE == response.getStatusCode()) {
        log.info("Spider stopping as received unavailable error processing " +
            "crawl of {}", candidate.getTarget());
        queue.clear();
        break;
      }
    }
    store.clearVisited();
  }

  private Set<URL> extractLocationUrls(List<String> locations, URL target) {
    Set<URL> result = new HashSet<URL>(locations.size());
    for (String url : locations) {
      try {
        URL candidate = new URL(target, url);
        result.add(candidate);
      } catch (MalformedURLException e) {
        log.debug("Error extracting url from [{}] in context {}",
            url, target.toExternalForm());
      }
    }
    return result;
  }

  private long now() {
    return System.currentTimeMillis();
  }

  private void enqueueUrls(URL referrer, Set<URL> urls) {
    for (URL urlUnescaped: urls) {
      if (!urlUnescaped.getProtocol().startsWith("http")) {
        log.debug("Skipping non http url {}", urlUnescaped.toExternalForm());
        continue;
      }
      URL url = stripReference(urlUnescaped);
      if (urlAlreadyQueued(url)) {
        continue;
      }
      if (skipByFilter(url)) {
        continue;
      }

      // check for both http and https versions of the url
      url = stripHttps(url);

      if (store.isVisited(url)) {
        continue;
      }
      log.debug("Queueing url {}", url);
      queue.offer(new UrlTarget(referrer, url));
    }
  }

  static URL stripHttps(URL url) {
    if ("https".equals(url.getProtocol())) {
      try {
        return new URL("http" + url.toExternalForm().substring(5));
      } catch (MalformedURLException e) {
      }
    }
    return url;
  }

  private boolean urlAlreadyQueued(URL url) {
    for (UrlTarget target : queue) {
      if (target.getTarget().equals(url)) {
        return true;
      }
    }
    return false;
  }

  static URL stripReference(URL urlUnescaped) {
    String external = urlUnescaped.toExternalForm();
    external = external.replaceFirst("#.*$", "");
    try {
      return new URL(external);
    } catch (MalformedURLException e) {
      LoggerFactory.getLogger(Spider.class).
          error("Failed to construct url from stripped external form " +
              external);
      return urlUnescaped;
    }
  }

  private boolean skipByPostFilter(Response response) {
    for (PostFetchFilter filter : postFetchFilters) {
      if (!filter.storeResponse(this, response)) {
        log.debug("Spider Skipping by filter: {}",
            filter.getClass().getCanonicalName());
        return true;
      }
    }
    return false;
  }

  private boolean skipByFilter(URL candidate) {
    for (PreFetchFilter filter : preFetchFilters) {
      if (!filter.shouldVisit(candidate)) {
        log.debug("Pre filter skip of {} by {}", candidate,
            filter.getClass().getCanonicalName());
        return true;
      }
    }
    return false;
  }

  public void clearQueue() {
    queue.clear();
  }

  public static class UrlTarget {
    private URL referrer, target;

    public UrlTarget(URL referrer, URL target) {
      this.referrer = referrer;
      this.target = target;
    }

    public URL getReferrer() {
      return referrer;
    }

    public URL getTarget() {
      return target;
    }
  }

  public static class Builder {

    private Spider spider;

    public Builder() {
      spider = new Spider();
      spider.queue = new LinkedList<UrlTarget>();
      spider.preFetchFilters = new ArrayList<PreFetchFilter>(1);
      spider.postFetchFilters = new ArrayList<PostFetchFilter>(1);
    }

    public Builder setMaximumRunningTime(long maximumRunningTime) {
      spider.maximumRunningTime = maximumRunningTime;
      return this;
    }

    public Builder setLinkExtractor(LinkExtractor linkExtractor) {
      spider.extractor = linkExtractor;
      return this;
    }

    public Builder setLinkFetcher(LinkFetcher linkFetcher) {
      spider.fetcher = linkFetcher;
      return this;
    }

    public Builder setPageStore(PageStore pageStore) {
      spider.store = pageStore;
      return this;
    }

    public Builder setSeedPage(URL url) {
      spider.queue.clear();
      spider.queue.offer(new UrlTarget(null, url));
      return this;
    }

    public Builder addPreFetchFilter(PreFetchFilter filter) {
      spider.preFetchFilters.add(filter);
      return this;
    }

    public Builder addPostFetchFilter(PostFetchFilter filter) {
      spider.postFetchFilters.add(filter);
      return this;
    }

    public Builder setMinimumTimeBetweenRequests(long timeInMillis) {
      spider.minimumTime = timeInMillis;
      return this;
    }

    public Spider build() {
      if (spider.queue.isEmpty()) {
        throw new IllegalStateException("Spider seed page not set");
      }
      if (null == spider.fetcher) {
        throw new IllegalStateException("Spider fetcher not set");
      }
      if (null == spider.extractor) {
        throw new IllegalStateException("Spider link extractor not set");
      }
      if (null == spider.store) {
        throw new IllegalStateException("Spider store not set");
      }
      return spider;
    }
  }
}
