/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mock storage for the crawler. Always returns false to checks for a contained
 * url.
 *
 * @author dak
 */
public class MockPageStore implements PageStore {

  private List<Response> responses = new ArrayList<Response>();
  private Set<URL> visited = new HashSet<URL>();

  @Override
  public void saveResponse(Response response) {
    // drop it on the floor
    responses.add(response);
  }

  @Override
  public void markVisited(URL url) {
    visited.add(url);
  }

  @Override
  public void clearVisited() {
    visited.clear();
  }

  @Override
  public boolean isVisited(URL url) {
    return visited.contains(url);
  }

  @Override
  public void open() {
  }

  @Override
  public void close() {
  }

  public List<Response> getResponses() {
    return responses;
  }
}
