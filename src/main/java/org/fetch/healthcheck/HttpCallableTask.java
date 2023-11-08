package org.fetch.healthcheck;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

public class HttpCallableTask implements Callable<ResponseData> {

  private EndPoint endPoint;

  public HttpCallableTask(EndPoint endPoint) {
    this.endPoint = endPoint;
  }

  @Override
  public ResponseData call() {
    StringBuilder response = new StringBuilder();
    int responseCode = -1;
    long latency = 0;
    long start = 0;
    try {
      URL url = new URL(endPoint.getUrl());
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod(endPoint.getMethod() == null ? "GET" : endPoint.getMethod());
      if (endPoint.getHeaders() != null) {
        for (Entry<String, String> entry : endPoint.getHeaders().entrySet()) {
          connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
      }
      if (endPoint.getMethod() != null && (endPoint.getMethod().equalsIgnoreCase("POST") ||
          endPoint.getMethod().equalsIgnoreCase("PUT"))) {
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(endPoint.getBody());
        outputStream.flush();
        outputStream.close();
      }
      start = System.currentTimeMillis();
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(connection.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();
      connection.disconnect();
      long end = System.currentTimeMillis();
      latency = end - start;
      responseCode = connection.getResponseCode();
    } catch (Exception e) {
      if (e.getMessage().contains("response code: 403")) {
        responseCode = 403;
      }
      long end = System.currentTimeMillis();
      latency = end - start;
    }
    String status = "DOWN";
    if (latency < 500 && String.valueOf(responseCode).startsWith("2")) {
      status = "UP";
    }
    return ResponseData.builder().name(endPoint.getName()).status(status)
        .build();
  }
}
