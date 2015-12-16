/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.net.MalformedURLException;
import java.net.URL;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for spider.
 *
 * @author Damien Allison
 */
public class SpiderTest {

  @DataProvider(name = "referenceUrls")
  public Object[][] getReferenceUrls() {
    try {
      return new Object[][] {
          {new URL("http://localhost/index.jsp#abcdefg")},
          {new URL("http://localhost/index.jsp?abcd#fff")},
          {new URL("http://damien@localhost/index.jsp?abc#fed")}
      };
    } catch (MalformedURLException e) {
      throw new Error("Could not create example urls");
    }
  }

  @Test(dataProvider = "referenceUrls")
  public void testStripUrlReference(URL reference) {
    URL stripped = Spider.stripReference(reference);
    assertEquals(stripped.getProtocol(), reference.getProtocol());
    assertEquals(stripped.getHost(), reference.getHost());
    assertEquals(stripped.getPort(), reference.getPort());
    assertEquals(stripped.getPath(), reference.getPath());
    assertEquals(stripped.getQuery(), reference.getQuery());
    assertEquals(stripped.getUserInfo(), reference.getUserInfo());
    assertNull(stripped.getRef());
  }

  @Test
  public void testStripHttps() throws MalformedURLException {
    URL with = new URL("https://www.sitemorph.net/test");
    URL without = Spider.stripHttps(with);
    assertEquals(without, new URL("http://www.sitemorph.net/test"));
  }
}
