/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

 package io.cdap.wrangler.parser;

 import io.cdap.wrangler.api.parser.TimeDuration;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;

 /**
 * Unit tests for {@link io.cdap.wrangler.api.parser.TimeDuration}.
 * Validates correct parsing of time durations and error handling.
 */
 public class TimeDurationTest {
 
   @Test
   public void testParsing() {
     assertEquals(500L, (long) new TimeDuration("500ms").value());
     assertEquals(1000L, (long) new TimeDuration("1s").value());
     assertEquals(60000L, (long) new TimeDuration("1m").value());
     assertEquals(3600000L, (long) new TimeDuration("1h").value());
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void testInvalidUnit() {
     new TimeDuration("10xyz");
   }
 }

