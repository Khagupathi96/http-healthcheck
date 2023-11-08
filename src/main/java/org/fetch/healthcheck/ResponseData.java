package org.fetch.healthcheck;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseData {
  private String name;
  private String status;
}
