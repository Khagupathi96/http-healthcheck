package org.fetch.healthcheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EndPoint {
  @JsonProperty("url")
  private String url;

  @JsonProperty("name")
  private String name;

  @JsonProperty("method")
  private String method;

  @JsonProperty("body")
  private String body;

  @JsonProperty("headers")
  private Map<String, String> headers;
}
