/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

/**
* Status code enumeration to avoid the usual code guessing.
 *
 * @author Damien Allison (damien@sitemorph.net)
*/
public enum StatusCode {

  BAD_REQUEST {
    @Override
    public int getCodeNumber() {
      return 400;
    }
  },
  NOT_FOUND {
    @Override
    public int getCodeNumber() {
      return 404;
    }
  },
  OK {
    @Override
    public int getCodeNumber() {
      return 200;
    }
  },
  SERVER_ERROR {
    @Override
    public int getCodeNumber() {
      return 501;
    }
  },
  UNAVAILABLE {
    @Override
    public int getCodeNumber() {
      return 503;
    }
  },
  MOVED_PERMANENTLY {
    @Override
    public int getCodeNumber() {
      return 301;
    }
  },
  FOUND_REDIRECT {
    @Override
    public int getCodeNumber() {
      return 302;
    }
  },
  SEE_OTHER_REDIRECT {
    @Override
    public int getCodeNumber() {
      return 303;
    }
  },
  UNKNOWN {
    @Override
    public int getCodeNumber() {
      return Integer.MIN_VALUE;
    }
  };

  @Override
  public String toString() {
    return this.name() + "(" + this.getCodeNumber() + ")";
  }

  public abstract int getCodeNumber();

  public static StatusCode forNumericCode(int code) {
    for (StatusCode statusCode : StatusCode.values()) {
      if (statusCode == UNKNOWN) {
        continue;
      }
      if (statusCode.getCodeNumber() == code) {
        return statusCode;
      }
    }
    return UNKNOWN;
  }
}
