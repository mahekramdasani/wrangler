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

 package io.cdap.wrangler.parser;

 import io.cdap.wrangler.api.parser.ByteSize;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * Unit tests for {@link io.cdap.wrangler.api.parser.ByteSize}.
  * Validates correct parsing of byte sizes and error handling.
  */
 public class ByteSizeTest {
 
   @Test
   public void testParsing() {
     assertEquals(1024L, (long) new ByteSize("1KB").value());
     assertEquals(1048576L, (long) new ByteSize("1MB").value());
     assertEquals(1073741824L, (long) new ByteSize("1GB").value());
     assertEquals(1099511627776L, (long) new ByteSize("1TB").value()); // updated to match 1024^4
     assertEquals(123L, (long) new ByteSize("123B").value());
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void testInvalidUnit() {
     new ByteSize("10XYZ");
   }
 }

