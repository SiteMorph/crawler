/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Robots directive parser and logic.
 */
public class RobotsTxt {

  private static final String MATCH_ANY_AGENT = "*",
      MATCH_ANY_PATH = ".*",
      EMPTY = "",
      END_OF_LINE_COMMENT = "\\#.*$";

  private Map<String, List<Rule>> agents = new HashMap<String, List<Rule>>();

  private enum Permission {
    ALLOW,
    DENY
  }

  private static final class Rule {
    private Permission permission;
    private String regex;

    Rule(String path, Permission permission) {
      this.regex = path;
      this.permission = permission;
    }

    boolean matches(String path) {
      return null != path && path.matches(regex);
    }

    boolean fetchPermissible() {
      return permission == Permission.ALLOW;
    }

    public Permission getPermission() {
      return permission;
    }

    public String getPathMatcher() {
      return regex;
    }
  }

  /**
   * Stateful builder that understands new agent directives and path rule
   * order addition. Assumes rules are added as specified.
   */
  public static class Builder {

    RobotsTxt robots;
    private String currentAgent;

    private static final Pattern AGENT = Pattern.compile(
        "\\s*User-Agent:.*", Pattern.CASE_INSENSITIVE),
        ALLOW = Pattern.compile("\\s*Allow\\s*:.*", Pattern.CASE_INSENSITIVE),
        DISALLOW = Pattern.compile("\\s*Disallow\\s*:.*",
            Pattern.CASE_INSENSITIVE);

    public Builder() {
      robots = new RobotsTxt();
    }

    public Builder newAgent(String agent) {
      currentAgent = agent;
      return this;
    }

    public Builder matchPath(String pathRegex, Permission permission) {
      if (!robots.agents.containsKey(currentAgent)) {
        robots.agents.put(currentAgent, new ArrayList<Rule>(1));
      }
      robots.agents.get(currentAgent).add(new Rule(pathRegex, permission));
      return this;
    }

    public Builder processDirective(String line) {
      if (isAgentDirective(line)) {
        currentAgent = rightHandOf(line).trim();
      } else if (isPathDirective(line)) {
        if (DISALLOW.matcher(line).matches()) {
          if (EMPTY.equals(rightHandOf(line))) {
            // special case with disallow with empty path is allow
            matchPath(MATCH_ANY_PATH, Permission.ALLOW);
          } else {
            matchPath(buildPathRegex(rightHandOf(line)), Permission.DENY);
          }
        } else {
          matchPath(buildPathRegex(rightHandOf(line)), Permission.ALLOW);
        }
      }
      return this;
    }

    String buildPathRegex(String path) {
      if (EMPTY.equals(path.trim())) {
        // no path implies match nothing
        return "";
      }
      // replace any . match all with escaped version
      path = path.replaceAll("\\.", "\\.");
      path = path.replaceAll("\\?", "\\?");
      // replace wildcard star with regex match any
      path = path.replaceAll("\\*", ".*");
      return path + ".*";
    }

    public static boolean isAgentDirective(String line) {
      return line != null && AGENT.matcher(line).matches();
    }

    public static boolean isPathDirective(String line) {
      return line != null &&
          (DISALLOW.matcher(line).matches() ||
              ALLOW.matcher(line).matches());
    }

    public static String rightHandOf(String line) {
      int colonIndex = line.indexOf(":");
      return line.substring(colonIndex + 1).trim();
    }

    public RobotsTxt build() {
      return robots;
    }
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder();
    out.append("{");
    for (Map.Entry<String, List<Rule>> entry : agents.entrySet()) {
      out.append("[")
          .append(entry.getKey())
          .append(" ");
      for (int i = 0; i < entry.getValue().size(); i++) {
        out.append(i)
            .append(" ")
            .append(entry.getValue().get(i).getPermission())
            .append(" ")
            .append(" : '")
            .append(entry.getValue().get(i).getPathMatcher())
            .append("', ");
      }
      out.append("],");
    }
    out.append("}");
    return out.toString();
  }

  public static RobotsTxt fromResponse(Response response) {
    Builder builder = new Builder();
    if (response.getStatusCode() != StatusCode.OK) {
      builder.newAgent(MATCH_ANY_AGENT)
          .matchPath(MATCH_ANY_PATH, Permission.ALLOW);
      return builder.build();
    }
    if (response.hasBody()) {
      for (String line : response.getBody().split("[\n\r]+")) {
        line = line.trim();
        if (line.indexOf('#') > 0) {
          line = line.substring(0, line.indexOf('#'));
        }
        builder.processDirective(line);
      }
    }
    return builder.build();
  }

  public boolean pathIsAllowed(String agent, String path) {
    if (agents.containsKey(agent))  {
      List<Rule> rules = agents.get(agent);
      for (Rule rule : rules) {
        if (rule.matches(path)) {
          return rule.fetchPermissible();
        }
      }
    }
    // check for wildcard matches
    if (agents.containsKey(MATCH_ANY_AGENT)) {
      for (Rule rule : agents.get(MATCH_ANY_AGENT)) {
        if (rule.matches(path)) {
          return rule.fetchPermissible();
        }
      }
    }
    // just go for it!
    return true;
  }
}