/*
 * Copyright (c) 2013. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import net.sitemorph.crawler.Spider.UrlTarget;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A apache http client backed fetcher.
 *
 * @author dak
 */
public class HttpClientFetcher implements LinkFetcher {

  private static final int SMALL_FILE = 8 * 1024;
  private HttpClient client;
  private Logger log = LoggerFactory.getLogger(getClass());
  private SSLContext sslContext;

  HttpClientFetcher(String userAgent) {

    HttpClientBuilder builder = HttpClientBuilder.create()
        .disableAutomaticRetries()
        .disableCookieManagement()
        .disableRedirectHandling()
        .setHostnameVerifier(
            new X509HostnameVerifier() {
              @Override
              public void verify(String s,
                  SSLSocket sslSocket) throws IOException {
              }

              @Override
              public void verify(String s,
                  X509Certificate x509Certificate) throws SSLException {
              }

              @Override
              public void verify(String s, String[] strings,
                  String[] strings2) throws SSLException {
              }

              @Override
              public boolean verify(String s, SSLSession sslSession) {
                return true;
              }
            }
        );
    if (null != userAgent) {
      builder.setUserAgent(userAgent);
    }

    client = builder.build();
  }

  public HttpClientFetcher() {
    this(null);
  }

  @Override
  public Response fetchUrl(UrlTarget url) {
    HttpGet get = null;
    Response.Builder builder = new Response.Builder();
    try {
      get = new HttpGet(url.getTarget().toExternalForm());

      log.debug("EXECUTING {}", url.getTarget().toExternalForm());
      HttpResponse response = client.execute(get);
      log.debug("Status Code Returned: {}",
          response.getStatusLine().getStatusCode());
      builder.setStatusCode(StatusCode.forNumericCode(
          response.getStatusLine().getStatusCode()));
      for (Header header : response.getAllHeaders()) {
        builder.addHeader(header.getName(), header.getValue());
      }
      builder.setUrl(url.getTarget())
          .setReferrer(url.getReferrer());
      HttpEntity entity = response.getEntity();
      InputStreamReader in = new InputStreamReader(entity.getContent());
      StringBuilder data = new StringBuilder(SMALL_FILE);
      int character = -1;
      while (-1 != (character = in.read())) {
        data.append((char) character);
      }
      get.releaseConnection();
      builder.setBody(data.toString());
    } catch (IOException e) {
      log.info("IO Error fetching URL: {}", url.getTarget().toExternalForm(),
          e);
      builder.setReferrer(url.getReferrer())
          .setUrl(url.getTarget())
          .setStatusCode(StatusCode.SERVER_ERROR)
          .setBody("");
    } catch (IllegalArgumentException e) {
      log.info("Illegal Argument (probably from apache) fetching URL {}",
          url.getTarget().toExternalForm(), e);
      builder.setReferrer(url.getReferrer())
          .setUrl(url.getTarget())
          .setStatusCode(StatusCode.UNKNOWN)
          .setBody("");
    }
    if (null != get) {
      get.releaseConnection();
    }
    return builder.build();
  }

  @Override
  public String getAgentString() {
    return HttpUrlConnectionFetcher.USER_AGENT;
  }
}
