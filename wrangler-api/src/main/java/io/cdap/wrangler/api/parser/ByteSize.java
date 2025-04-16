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
import com.google.gson.JsonObject;
import io.cdap.wrangler.api.annotations.PublicEvolving;

/**
 * The ByteSize class represents a token for byte size values (e.g., 10KB, 150MB).
 * It parses the provided string value and converts it to a canonical unit (bytes).
 */
@PublicEvolving
public class ByteSize implements Token {
    private final double value;
    private final String unit;
    private final long bytes;

    /**
     * Constructor for creating a byte size token.
     *
     * @param value The string representation of the byte size (e.g., "10MB", "1.5KB")
     */
    public ByteSize(String value) {
        // Extract the numeric value and unit
        String trimmedValue = value.trim();
        int i = 0;
        while (i < trimmedValue.length() && 
              (Character.isDigit(trimmedValue.charAt(i)) || trimmedValue.charAt(i) == '.')) {
            i++;
        }
        
        this.value = Double.parseDouble(trimmedValue.substring(0, i).trim());
        this.unit = trimmedValue.substring(i).trim().toUpperCase();
        this.bytes = convertToBytes(this.value, this.unit);
    }

    /**
     * Returns the byte size value in bytes.
     *
     * @return The size in bytes
     */
    public long getBytes() {
        return bytes;
    }

    /**
     * Returns the original numeric value before unit conversion.
     *
     * @return The numeric value
     */
    public double getValue() {
        return value;
    }

    /**
     * Returns the original unit.
     *
     * @return The unit string
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Convert a value with a specific unit to bytes.
     *
     * @param value The numeric value
     * @param unit The unit string
     * @return The equivalent value in bytes
     */
    private long convertToBytes(double value, String unit) {
        switch (unit) {
            case "B":
                return (long) value;
            case "KB":
                return (long) (value * 1024);
            case "MB":
                return (long) (value * 1024 * 1024);
            case "GB":
                return (long) (value * 1024 * 1024 * 1024);
            case "TB":
                return (long) (value * 1024 * 1024 * 1024 * 1024);
            default:
                throw new IllegalArgumentException("Invalid byte size unit: " + unit);
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
        JsonObject object = new JsonObject();
        object.addProperty("type", TokenType.BYTE_SIZE.name());
        object.addProperty("value", value);
        object.addProperty("unit", unit);
        object.addProperty("bytes", bytes);
        return object;
    }
}

