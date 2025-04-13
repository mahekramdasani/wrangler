/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */

 package io.cdap.directives.row;

 import io.cdap.wrangler.TestingRig;
 import io.cdap.wrangler.api.Row;
 import io.cdap.wrangler.api.parser.ByteSize;
 import io.cdap.wrangler.api.parser.TimeDuration;
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Unit test for the AggregateStats directive.
  * This test verifies the correctness of aggregated size and time calculations.
  */
 public class AggregateStatsTest {
 
   @Test
   public void testAggregateStats() throws Exception {
     List<Row> rows = Arrays.asList(
       new Row().add("data_size", new ByteSize("1MB")).add("response_time", new TimeDuration("2s")),
       new Row().add("data_size", new ByteSize("512KB")).add("response_time", new TimeDuration("1500ms")),
       new Row().add("data_size", new ByteSize("2MB")).add("response_time", new TimeDuration("500ms"))
     );
 
     String[] recipe = {
       "aggregate-stats :data_size :response_time total_size_mb total_time_sec"
     };
 
     List<Row> results = TestingRig.execute(recipe, rows);
 
     Assert.assertEquals(1, results.size());
 
     Row result = results.get(0);
     double expectedSizeMB = (1 * 1024 * 1024 + 512 * 1024 + 2 * 1024 * 1024) / (1024.0 * 1024.0);
     double expectedTimeSec = (2000 + 1500 + 500) / 1000.0;
 
     Assert.assertEquals(expectedSizeMB, (double) result.getValue("total_size_mb"), 0.001);
     Assert.assertEquals(expectedTimeSec, (double) result.getValue("total_time_sec"), 0.001);
   }
 }

