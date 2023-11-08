package org.fetch.healthcheck;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {


  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Input file not provided");
      return;
    }
    String filePath = args[0];
    if (!filePath.endsWith("yaml") && !filePath.endsWith(".yml")) {
      System.out.println("Input file not a yaml file");
      return;
    }
    File file = new File(filePath);
    final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); // jackson databind
    EndPoint[] endPoints = null;
    try {
      endPoints = mapper.readValue(file, EndPoint[].class);
    } catch (IOException e) {
      System.out.println("Invalid yaml file");
      return;
    }
    List<HttpCallableTask> callableTaskList = new ArrayList<>();
    for (EndPoint endPoint : endPoints) {
      callableTaskList.add(new HttpCallableTask(endPoint));
    }

    Map<String, Integer> requestCountMap = new HashMap<>();
    Map<String, Integer> upRequestCountMap = new HashMap<>();
    while (true) {
      ExecutorService executor = Executors.newScheduledThreadPool(4);
      try {
        List<Future<ResponseData>> futures = executor.invokeAll(callableTaskList);
        for (Future<ResponseData> future : futures) {
          ResponseData resp = future.get();
          String domain = resp.getName().split(" ")[0];
          if (!requestCountMap.containsKey(domain)) {
            requestCountMap.put(domain, 0);
          }
          requestCountMap.put(domain, requestCountMap.get(domain) + 1);
          if ("UP".equalsIgnoreCase(resp.getStatus())) {
            if (!upRequestCountMap.containsKey(domain)) {
              upRequestCountMap.put(domain, 0);
            }
            upRequestCountMap.put(domain, upRequestCountMap.get(domain) + 1);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      for(Entry<String, Integer> entry : requestCountMap.entrySet()) {
        String domain = entry.getKey();
        int percentage = (upRequestCountMap.getOrDefault(domain, 0) * 100) / entry.getValue();
        System.out.println(domain + " has " + percentage + "% availability percentage");
      }
      try {
        Thread.sleep(15000l);
      } catch (Exception e) {
      }
    }
  }
}
