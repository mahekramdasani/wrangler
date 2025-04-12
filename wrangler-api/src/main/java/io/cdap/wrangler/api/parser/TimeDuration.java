package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  public Object value() {
    return milliseconds;
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
