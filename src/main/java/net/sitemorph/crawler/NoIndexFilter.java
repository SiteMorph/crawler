package net.sitemorph.crawler;

/**
 * NoIndex page filter.
 *
 * @author damien@sitemorph.net
 */
public class NoIndexFilter implements PostFetchFilter {

  private static final String ROBOTS = "ROBOTS";

  @Override
  public boolean storeResponse(Spider spider, Response response) {

    if (response.getStatusCode() != StatusCode.OK) {
      return true;
    }

    HtmlDocument document;
    try {
      document = HtmlDocument.from(response);
    } catch (IllegalArgumentException e) {
      // not html so not relevant
      return true;
    }

    if (document.hasMeta(ROBOTS)) {
      String directive = document.getMeta(ROBOTS);
      if (null != directive && directive.contains("NOINDEX")) {
        return false;
      }
    }
    return true;
  }
}
