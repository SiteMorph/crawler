package net.sitemorph.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * A html document wrapper which is backed by a Jsoup document. This class is
 * a decorator offering convenicence methods for extracting document aspects.
 *
 * @author dak
 */
public class HtmlDocument {

  private static final String HTML_CONTENT_TYPE = "text/html";
  private static final String TITLE_SELECTOR = "html head title";
  private static final String META_SELECTOR = "html head meta";
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String CONTENT = "content";
  private static final String KEYWORDS = "keywords";
  private Document document;

  private HtmlDocument() {}

  public static HtmlDocument from(String pageContent, String url) {
    HtmlDocument document = new HtmlDocument();
    document.document = parse(pageContent, url);
    return document;
  }

  /**
   * Create a document from a crawler response.
   *
   * @param response
   * @return constructed document
   * @throws IllegalArgumentException if the response is not an html document
   *    or fails to parse etc.
   */
  public static HtmlDocument from(Response response)
      throws IllegalArgumentException {

    if (StatusCode.OK != response.getStatusCode()) {
      throw new IllegalArgumentException("Response status not OK");
    }
    if(!response.hasHeader(Response.CONTENT_TYPE_HEADER) ||
        0 == response.getHeaderValues(Response.CONTENT_TYPE_HEADER).size()) {
      throw new IllegalArgumentException("Response has no content type");
    }
    String contentType = response.getHeaderValues(Response.CONTENT_TYPE_HEADER)
        .get(0);
    if (!contentType.startsWith(HTML_CONTENT_TYPE)) {
      throw new IllegalArgumentException("Expected text/html content type " +
          "but found: " + contentType);
    }

    HtmlDocument document = new HtmlDocument();

    document.document = parse(response.getBody(),
        response.getUrl().toExternalForm());

    return document;
  }

  private static Document parse(String html, String url) {
    return Jsoup.parse(html, url);
        /*Jsoup.clean(html,
        url, Whitelist.relaxed()
        .addTags("title head")
        .addAttributes("meta", "name", "content")
        .addAttributes("a", "href")));*/
  }

  public String getHtml() {
    return document.outerHtml();
  }

  public boolean hasTitle() {
    return null != getTitle();
  }

  /**
   * Get the document title or null if none is round.
   *
   * @return first title or null
   */
  public String getTitle() {
    Elements elements = document.select(TITLE_SELECTOR);
    if (0 < elements.size()) {
      Element element = elements.get(0);
      return element.text().trim();
    } else {
      return null;
    }
  }

  public boolean hasDescription() {
    return null != getDescription();
  }

  public String getDescription() {
    return getMeta(DESCRIPTION);
  }

  public boolean hasKeywords() {
    return null != getKeywords();
  }

  public String getKeywords() {
    return getMeta(KEYWORDS);
  }


  public boolean hasMeta(String metaName) {
    return null != getMeta(metaName);
  }

  public String getMeta(String metaName) {
    Elements metas = document.select(META_SELECTOR);
    for (Element meta : metas) {
      if (meta.hasAttr(NAME) &&
          meta.attr(NAME).equalsIgnoreCase(metaName) &&
          meta.hasAttr(CONTENT)) {
        return meta.attr(CONTENT).trim();
      }
    }
    return null;
  }
}
