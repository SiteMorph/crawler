package net.sitemorph.crawler;

import net.sitemorph.crawler.Spider.UrlTarget;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

/**
 * Post fetcher handles parameter setting etc.
 *
 * @author dak
 */
public class HttpPostFetcher {

  private static final String ACCEPT_ENCODING = "Accept-Encoding",
      ACCEPTED_ENCODINGS = "gzip, zip, deflate",
      POST = "POST";
  private static final String CONTENT_TYPE_NAME = "Content-Type",
      CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded",
      CONTENT_LENGTH = "Content-Length",
      ZIP = "zip", DEFLATE = "deflate", GZIP = "gzip";
  private static final String CONTENT_CHARSET = "charset",
      CHARSET = "UTF-8";
  private static final int READ_TIMEOUT = 60000;

  private Logger log = LoggerFactory.getLogger(getClass());

  private URL endpoint;
  private byte[] body;

  private HttpPostFetcher() {}

  public Response doPost() {
    StatusCode status = StatusCode.UNKNOWN;
    Map<String, List<String>> headers = Collections.emptyMap();
    try {
      URLConnection connection = endpoint.openConnection();
      if (!(connection instanceof HttpURLConnection)) {
        log.error("Connection is not a http connection.");
        throw new IOException("URL is not a http url");
      }
      HttpURLConnection http = (HttpURLConnection) connection;
      http.setRequestMethod(POST);
      http.setInstanceFollowRedirects(false);
      http.setDoInput(true);
      http.setDoOutput(true);
      //http.setRequestProperty(USER_AGENT, AGENT_STRING);
      http.setRequestProperty(ACCEPT_ENCODING, ACCEPTED_ENCODINGS);
      http.setRequestProperty(CONTENT_TYPE_NAME, CONTENT_TYPE_VALUE);
      http.setRequestProperty(CONTENT_CHARSET, CHARSET);    
      http.setRequestProperty(CONTENT_LENGTH, Integer.toString(body.length));
      http.setUseCaches(false);
      http.setReadTimeout(READ_TIMEOUT);

      http.connect();

      DataOutputStream out = new DataOutputStream(connection.getOutputStream());
      out.write(body);
      out.flush();
      out.close();

      status = StatusCode.forNumericCode(http.getResponseCode());
      headers = http.getHeaderFields();

      Response.Builder builder = new Response.Builder();
      builder.setHeaders(headers)
          .setStatusCode(status)
          .setUrl(endpoint)
          .setReferrer(null);

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
      Response.Builder notFound = new Response.Builder();
      notFound.setStatusCode(StatusCode.NOT_FOUND)
          .setUrl(endpoint)
          .setReferrer(null)
          .setHeaders(headers);
      return notFound.build();
    } catch (IOException e) {
      log.error("Error connecting to url " + endpoint.toExternalForm(),
          e);
      return Response.ioException(status, headers,
          new UrlTarget(endpoint, null), e);
    }
  }

  public static class Builder {

    private HttpPostFetcher result = new HttpPostFetcher();
    private StringBuilder data = new StringBuilder();

    public Builder setUri(String uri) throws MalformedURLException {
      result.endpoint = new URL(uri);
      return this;
    }

    public Builder addParameter(String name, String value) {
      data.append(escapeUri(name))
          .append("=")
          .append(escapeUri(value))
          .append("&");
      return this;
    }

    private String escapeUri(String value) {
      URLCodec codec = new URLCodec("utf-8");
      try {
        return codec.encode(value);
      } catch (EncoderException e) {
        throw new IllegalArgumentException("Could not url encode " + value, e);
      }
    }

    public HttpPostFetcher build() {
      try {
        result.body = data.toString().getBytes(CHARSET);
      } catch (UnsupportedEncodingException e) {
        throw new IllegalStateException("Could not get encoding for utf-8");
      }
      return result;
    }
  }
}
