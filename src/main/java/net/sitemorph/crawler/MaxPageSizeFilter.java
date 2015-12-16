package net.sitemorph.crawler;

/**
 * Page size filter to avoid save attempt of large pages.
 *
 * @author dak
 */
public class MaxPageSizeFilter implements PostFetchFilter {

  private long size;

  public MaxPageSizeFilter(long size) {
    this.size = size;
  }

  @Override
  public boolean storeResponse(Spider spider, Response response) {
    if (response.hasBody() && response.getBody().length() > size) {
      return false;
    }
    return true;
  }
}
