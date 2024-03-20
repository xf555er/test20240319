package net.jsign.jca;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;
import net.jsign.commons.io.IOUtils;
import net.jsign.json-io.util.io.JsonReader;

class RESTClient {
   private final String endpoint;
   private final Consumer authenticationHandler;

   RESTClient(String endpoint, Consumer authenticationHeaderSupplier) {
      this.endpoint = endpoint;
      this.authenticationHandler = authenticationHeaderSupplier;
   }

   Map get(String resource) throws IOException {
      return this.query("GET", resource, (String)null);
   }

   Map post(String resource, String body) throws IOException {
      return this.query("POST", resource, body);
   }

   private Map query(String method, String resource, String body) throws IOException {
      URL url = new URL(resource.startsWith("http") ? resource : this.endpoint + resource);
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setRequestMethod(method);
      conn.setRequestProperty("User-Agent", "Jsign (https://ebourg.github.io/jsign/)");
      this.authenticationHandler.accept(conn);
      if (body != null) {
         byte[] data = body.getBytes(StandardCharsets.UTF_8);
         conn.setDoOutput(true);
         conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
         conn.setRequestProperty("Content-Length", String.valueOf(data.length));
         conn.getOutputStream().write(data);
      }

      int responseCode = conn.getResponseCode();
      String contentType = conn.getHeaderField("Content-Type");
      String error;
      if (responseCode < 400) {
         error = IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8);
         return JsonReader.jsonToMaps(error);
      } else {
         error = IOUtils.toString(conn.getErrorStream(), StandardCharsets.UTF_8);
         if (contentType != null && contentType.startsWith("application/json")) {
            throw new IOException(this.getErrorMessage(JsonReader.jsonToMaps(error)));
         } else {
            throw new IOException("HTTP Error " + responseCode + (conn.getResponseMessage() != null ? " - " + conn.getResponseMessage() : "") + " (" + url + ")");
         }
      }
   }

   private String getErrorMessage(Map response) {
      Map error = (Map)response.get("error");
      StringBuilder message = new StringBuilder();
      if (error.get("code") != null) {
         message.append(error.get("code"));
      }

      if (error.get("status") != null) {
         if (message.length() > 0) {
            message.append(" - ");
         }

         message.append(error.get("status"));
      }

      if (error.get("message") != null) {
         if (message.length() > 0) {
            message.append(": ");
         }

         message.append(error.get("message"));
      }

      return message.toString();
   }
}
