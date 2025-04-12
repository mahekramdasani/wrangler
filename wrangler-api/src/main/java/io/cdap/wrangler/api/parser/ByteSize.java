package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ByteSize implements Token {
  private static final Pattern PATTERN = Pattern.compile("(?i)^(\\d+)(B|KB|MB|GB|TB)$");
  private final long bytes;
  private final String raw;

  public ByteSize(String raw) {
    this.raw = raw;
    this.bytes = parseByteSize(raw);
  }

  private long parseByteSize(String str) {
    Matcher matcher = PATTERN.matcher(str.trim());
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid byte size format: " + str);
    }
    long value = Long.parseLong(matcher.group(1));
    String unit = matcher.group(2).toUpperCase();
    switch (unit) {
      case "B": return value;
      case "KB": return value * 1024;
      case "MB": return value * 1024 * 1024;
      case "GB": return value * 1024 * 1024 * 1024;
      case "TB": return value * 1024L * 1024 * 1024 * 1024;
      default: throw new IllegalArgumentException("Unknown unit: " + unit);
    }
  }

  @Override
  public Object value() {
    return bytes;
  }

  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }

  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(raw);
  }
}
