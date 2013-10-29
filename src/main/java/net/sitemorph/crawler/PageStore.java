/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.net.URL;

/**
 * Page store provider.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public interface PageStore {

  public void saveResponse(Response response);

  /**
   * Mark that a url has been visited.
   * @param url that was visited
   */
  public void markVisited(URL url);

  /**
   * Clear the list of visited urls.
   *
   */
  public void clearVisited();


  /**
   * Test if a URL has been visited.
   *
   * @param url to test
   * @return true if the url has been visited.
   */
  public boolean isVisited(URL url);

  /**
   * Open the page store.
   */
  public void open();

  /**
   * Close the page store.
   */
  public void close();
}
