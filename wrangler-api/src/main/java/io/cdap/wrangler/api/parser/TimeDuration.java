/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * This class represents a token for a time duration value (e.g., 150ms, 2.5s).
 * It parses the provided string value and converts it to a canonical unit (nanoseconds).
 */
public class TimeDuration implements Token {
  private static final Pattern PATTERN = Pattern.compile("(?i)^(\\d+)(ms|s|m|h)$");
  private final long milliseconds;
  private final String raw;

  public TimeDuration(String raw) {
    this.raw = raw;
    this.milliseconds = parseDuration(raw);
  }

  private long parseDuration(String str) {
    Matcher matcher = PATTERN.matcher(str.trim());
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid time duration format: " + str);
    }
    long value = Long.parseLong(matcher.group(1));
    String unit = matcher.group(2).toLowerCase();
    switch (unit) {
      case "ms": return value;
      case "s": return value * 1000;
      case "m": return value * 60 * 1000;
      case "h": return value * 60 * 60 * 1000;
      default: throw new IllegalArgumentException("Unknown time unit: " + unit);
    }
  }

  @Override
  public Long value() {
      return this.milliseconds;
  }
  

  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }

  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(raw);
  }
}
