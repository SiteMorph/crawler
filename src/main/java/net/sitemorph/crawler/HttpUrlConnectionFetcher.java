/*
 * Copyright (c) 2012. Sitemorph Ltd. All rights reserved.
 */

package net.sitemorph.crawler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.sitemorph.crawler.Response.Builder;
import net.sitemorph.crawler.Spider.UrlTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetch a page using an underlying http url connection.
 *
 * @author Damien Allison (damien@sitemorph.net)
 */
public class HttpUrlConnectionFetcher implements LinkFetcher {

  private Logger log = LoggerFactory.getLogger(getClass());
  private static final int READ_TIMEOUT = 60000;
  static final String USER_AGENT = "User-Agent";
  private static final String AGENT_STRING = "SiteMorphCrawler/0.1 (http://www.sitemorph.net/)";
  private static final String ACCEPT_ENCODING = "Accept-Encoding",
      ACCEPTED_ENCODINGS = "gzip, zip, deflate";
  private static final String CONTENT_ENCODING = "Content-Encoding",
      ZIP = "zip", DEFLATE = "deflate", GZIP = "gzip";

  HttpUrlConnectionFetcher() {
    ignoreSllErrors();
  }

  @Override
  public Response fetchUrl(UrlTarget url) {
    StatusCode status = StatusCode.UNKNOWN;
    Map<String, List<String>> headers = Collections.emptyMap();
    try {
      URLConnection connection = url.getTarget().openConnection();
      if (!(connection instanceof HttpURLConnection)) {
        log.error("Connection is not a http connection.");
        throw new IOException("URL is not a http url");
      }
      HttpURLConnection http = (HttpURLConnection) connection;
      http.setRequestMethod("GET");
      http.setInstanceFollowRedirects(false);
      http.setDoInput(true);
      http.setRequestProperty(USER_AGENT, AGENT_STRING);
      http.setRequestProperty(ACCEPT_ENCODING, ACCEPTED_ENCODINGS);
      http.setReadTimeout(READ_TIMEOUT);
      http.connect();

      status = StatusCode.forNumericCode(http.getResponseCode());
      headers = http.getHeaderFields();

      Response.Builder builder = new Builder();
      builder.setHeaders(headers)
          .setStatusCode(status)
          .setUrl(url.getTarget())
          .setReferrer(url.getReferrer());

      StringBuilder string = new StringBuilder();
      
      BufferedReader in;
      String contentEncoding = http.getContentEncoding();
      if (null == contentEncoding) {
        contentEncoding = "";
      }
      if (contentEncoding.equalsIgnoreCase(ZIP)) {
        in = new BufferedReader(new InputStreamReader(
            new ZipInputStream(http.getInputStream())));
      } else if (contentEncoding.equalsIgnoreCase(GZIP)) {
        in = new BufferedReader(new InputStreamReader(
            new GZIPInputStream(http.getInputStream())));
      } else if (contentEncoding.equalsIgnoreCase(DEFLATE)) {
        in = new BufferedReader(new InputStreamReader(
            new InflaterInputStream(http.getInputStream())));
      } else {
        in = new BufferedReader(
            new InputStreamReader(http.getInputStream()));
      }
      String line;
      while ((line = in.readLine()) != null) {
        string.append(line);
        string.append("\n");
      }
      in.close();
      http.disconnect();
      builder.setBody(string.toString());
      return builder.build();
    } catch (FileNotFoundException e) {
      log.info("Http Connection threw not found exception");
      Response.Builder notFound = new Builder();
      notFound.setStatusCode(StatusCode.NOT_FOUND)
          .setUrl(url.getTarget())
          .setReferrer(url.getReferrer())
          .setHeaders(headers);
      return notFound.build();
    } catch (IOException e) {
      log.error("Error connecting to url " + url.getTarget().toExternalForm(),
          e);
      return Response.ioException(status, headers, url, e);
    }
  }

  @Override
  public String getAgentString() {
    return USER_AGENT;
  }

  private void ignoreSllErrors() {

    // set up the ssl system property
    System.setProperty ("jsse.enableSNIExtension", "false");

    TrustManager[] trustAllManager = new TrustManager[]{
        new X509TrustManager() {
          @Override
          public void checkClientTrusted(X509Certificate[] x509Certificates,
                                         String s) throws CertificateException {
            // do nothing - ignore trust chain
          }

          @Override
          public void checkServerTrusted(X509Certificate[] x509Certificates,
                                         String s) throws CertificateException {
            // do nothing - ignore trust chain
          }

          @Override
          public X509Certificate[] getAcceptedIssuers() {
            return null;
          }
        }
    };
    HostnameVerifier hostnameVerifier = new HostnameVerifier() {
      @Override
      public boolean verify(String s, SSLSession sslSession) {
        return true;
      }
    };

    try {
      SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllManager, new SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(
          sslContext.getSocketFactory());
      HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Error getting SSL context to update " +
          "trust manager", e);
    } catch (KeyManagementException e) {
      throw new RuntimeException("Error initialising the SSL context", e);
    }
  }
}
