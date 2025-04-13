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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */

 package io.cdap.wrangler.api.parser;

 import com.google.gson.JsonElement;
 import com.google.gson.JsonPrimitive;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * This class represents a token for a byte size value (e.g., 10KB, 150MB).
  * It parses the provided string value and converts it to a canonical unit (bytes).
  */
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
       case "KB": return value * 1024L;
       case "MB": return value * 1024L * 1024;
       case "GB": return value * 1024L * 1024 * 1024;
       case "TB": return value * 1024L * 1024 * 1024 * 1024;
       default: throw new IllegalArgumentException("Unknown unit: " + unit);
     }
   }
 
   @Override
   public Long value() {
     return this.bytes;
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

