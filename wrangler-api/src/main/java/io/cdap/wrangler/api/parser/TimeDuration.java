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
import com.google.gson.JsonObject;
import io.cdap.wrangler.api.annotations.PublicEvolving;

/**
 * The TimeDuration class represents a token for time duration values (e.g., 10ms, 150s).
 * It parses the provided string value and converts it to a canonical unit (nanoseconds).
 */
@PublicEvolving
public class TimeDuration implements Token {
    private final double value;
    private final String unit;
    private final long nanoseconds;

    /**
     * Constructor for creating a time duration token.
     *
     * @param value The string representation of the time duration (e.g., "10ms", "1.5s")
     */
    public TimeDuration(String value) {
        // Extract the numeric value and unit
        String trimmedValue = value.trim();
        int i = 0;
        while (i < trimmedValue.length() && 
              (Character.isDigit(trimmedValue.charAt(i)) || trimmedValue.charAt(i) == '.')) {
            i++;
        }
        
        this.value = Double.parseDouble(trimmedValue.substring(0, i).trim());
        this.unit = trimmedValue.substring(i).trim().toLowerCase();
        this.nanoseconds = convertToNanoseconds(this.value, this.unit);
    }

    /**
     * Returns the time duration value in nanoseconds.
     *
     * @return The duration in nanoseconds
     */
    public long getNanoseconds() {
        return nanoseconds;
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
     * Convert a value with a specific unit to nanoseconds.
     *
     * @param value The numeric value
     * @param unit The unit string
     * @return The equivalent value in nanoseconds
     */
    private long convertToNanoseconds(double value, String unit) {
        switch (unit) {
            case "ns":
                return (long) value;
            case "ms":
                return (long) (value * 1_000_000);
            case "s":
            case "sec":
                return (long) (value * 1_000_000_000);
            case "m":
            case "min":
                return (long) (value * 60 * 1_000_000_000);
            case "h":
                return (long) (value * 60 * 60 * 1_000_000_000);
            default:
                throw new IllegalArgumentException("Invalid time unit: " + unit);
        }
    }

    @Override
    public Object value() {
        return nanoseconds;
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", TokenType.TIME_DURATION.name());
        object.addProperty("value", value);
        object.addProperty("unit", unit);
        object.addProperty("nanoseconds", nanoseconds);
        return object;
    }
}
