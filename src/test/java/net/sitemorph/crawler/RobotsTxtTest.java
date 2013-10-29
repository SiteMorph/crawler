/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import net.sitemorph.crawler.Response.Builder;
import org.testng.annotations.Test;

/**
 * Test robots implementation.
 *
 * @author dak
 */
public class RobotsTxtTest {

  @Test
  public void testWildcardDisallowAllowed() throws MalformedURLException {
    String counterCaseMatchingNoPaths =
        "User-Agent: *\n" +
        "Disallow:";
    RobotsTxt robots = RobotsTxt.fromResponse(buildOkResponseWithBody(
        counterCaseMatchingNoPaths));
    assertTrue(robots.pathIsAllowed(HttpUrlConnectionFetcher.USER_AGENT,
        "/testPath.html"), "Expected test path to be allowed");
  }

  @Test
  public void testWildcardDisallowDenied() throws MalformedURLException {
    String excludeCase =
        "User-Agent: *\n" +
        "Disallow: /";
    RobotsTxt robots = RobotsTxt.fromResponse(buildOkResponseWithBody(
        excludeCase));
    assertFalse(robots.pathIsAllowed(HttpUrlConnectionFetcher.USER_AGENT,
        "/testPath.html"), "Expected deny for robots: " + robots.toString());
  }

  @Test
  public void testRobotsWithWildcardExcludes() throws MalformedURLException {
    String excludedMatches =
        "User-agent: *\n" +
        "Disallow: /cgi-bin/\n" +
        "Disallow: /images/\n";
    RobotsTxt robots = RobotsTxt.fromResponse(buildOkResponseWithBody(
        excludedMatches));
    assertFalse(robots.pathIsAllowed(HttpUrlConnectionFetcher.USER_AGENT,
        "/cgi-bin/test"), robots.toString());
    assertFalse(robots.pathIsAllowed(HttpUrlConnectionFetcher.USER_AGENT,
        "/images/testImage.png"), robots.toString());
    assertTrue(robots.pathIsAllowed(HttpUrlConnectionFetcher.USER_AGENT,
        "/index.html"), robots.toString());
  }

  @Test
  public void testBadRobotWouldBeExcluded() throws MalformedURLException {
    String content =
        "User-agent: BadBot # replace 'BadBot' with the actual " +
            "user-agent of the bot\n" +
            "Disallow: /private/\n";
    RobotsTxt robots = RobotsTxt.fromResponse(buildOkResponseWithBody(
        content));
    assertTrue(robots.pathIsAllowed(HttpUrlConnectionFetcher.USER_AGENT,
        "/private/"), robots.toString());
    assertFalse(robots.pathIsAllowed("BadBot", "/private/"), robots.toString());
    assertTrue(robots.pathIsAllowed("BadBot", "/index.html"), robots.toString());
  }

  @Test
  public void testOtherFilesOk() throws MalformedURLException {
    String content =
        "User-agent: *\n" +
        "Disallow: /directory/file.html";
    RobotsTxt robots = RobotsTxt.fromResponse(buildOkResponseWithBody(
        content));
    assertFalse(robots.pathIsAllowed(
        HttpUrlConnectionFetcher.USER_AGENT, "/directory/file.html"),
        robots.toString());
    assertTrue(robots.pathIsAllowed(HttpUrlConnectionFetcher.USER_AGENT,
        "/directory/file1.html"), robots.toString());
  }

  @Test
  public void testAllDeniedAfterComments() throws MalformedURLException {
    String content =
        "# Comments appear after the \"#\" symbol at the start of a line, or after a directive\n" +
            "User-agent: * # match all bots\n" +
            "Disallow: / # keep them out\n";
    RobotsTxt robotsTxt = RobotsTxt.fromResponse(buildOkResponseWithBody(
        content));
    assertFalse(robotsTxt.pathIsAllowed(HttpUrlConnectionFetcher.USER_AGENT,
        "/"), robotsTxt.toString());
  }

  @Test
  public void someAgentsAllowed() throws MalformedURLException {
    String content =
        "User-agent: googlebot        # all services\n" +
            "Disallow: /private           # disallow this directory\n" +
            " \n" +
            "User-agent: googlebot-news   # only the news service\n" +
            "Disallow: /                  # on everything\n" +
            " \n" +
            "User-agent: *                # all robots\n" +
            "Disallow: /something         # on this folder\n";
    RobotsTxt robots = RobotsTxt.fromResponse(buildOkResponseWithBody(
        content));
    assertTrue(robots.pathIsAllowed("googlebot", "/"), robots.toString());
    // TODO 20121127 (dak) Implement directory or post path match
    assertFalse(robots.pathIsAllowed("googlebot-news", "/somefile.html"),
        robots.toString());
    assertTrue(robots.pathIsAllowed(HttpUrlConnectionFetcher.USER_AGENT,
        "/index.html"), robots.toString());
    assertFalse(robots.pathIsAllowed(HttpUrlConnectionFetcher.USER_AGENT,
        "/something"), robots.toString());
  }

  private Response buildOkResponseWithBody(String body)
      throws MalformedURLException {
    return new Builder()
        .setUrl(new URL("http://www.sitemorph.net/robots.txt"))
        .setHeaders(new HashMap<String, List<String>>())
        .setStatusCode(StatusCode.OK)
        .setBody(body)
        .build();
  }
}
