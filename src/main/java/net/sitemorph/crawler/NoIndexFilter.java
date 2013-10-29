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

    HtmlDocument document = HtmlDocument.from(response);

    if (document.hasMeta(ROBOTS)) {
      String directive = document.getMeta(ROBOTS);
      if (null != directive && directive.contains("NOINDEX")) {
        return false;
      }
    }
    return true;
  }
}
