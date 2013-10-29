/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.net.URL;

/**
 * Listener for status code responses from fetch attempts.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public interface StatusCodeListener {

  public void handleStatusCode(URL url, StatusCode code);

}
