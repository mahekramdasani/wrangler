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

package io.cdap.wrangler.executor;

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.TestingRig;
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
        // Create test data
        List<Row> rows = Arrays.asList(
            createRow("data_transfer_size", "1MB", "response_time", "100ms"),
            createRow("data_transfer_size", "2MB", "response_time", "200ms"),
            createRow("data_transfer_size", "3MB", "response_time", "300ms")
        );

        // Define the recipe
        String[] recipe = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };

        // Execute the recipe
        List<Row> results = TestingRig.execute(recipe, rows);

        // Verify results
        Assert.assertEquals(1, results.size());
        Row result = results.get(0);
        
        // Total size should be 6MB (1 + 2 + 3)
        Assert.assertEquals(6.0, result.getValue("total_size_mb"), 0.001);
        
        // Total time should be 0.6 seconds (100 + 200 + 300 ms)
        Assert.assertEquals(0.6, result.getValue("total_time_sec"), 0.001);
    }

    @Test
    public void testMixedUnits() throws Exception {
        // Create test data with mixed units
        List<Row> rows = Arrays.asList(
            createRow("data_transfer_size", "1MB", "response_time", "1s"),
            createRow("data_transfer_size", "1024KB", "response_time", "500ms"),
            createRow("data_transfer_size", "2MB", "response_time", "2s")
        );

        // Define the recipe
        String[] recipe = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };

        // Execute the recipe
        List<Row> results = TestingRig.execute(recipe, rows);

        // Verify results
        Assert.assertEquals(1, results.size());
        Row result = results.get(0);
        
        // Total size should be 4MB (1MB + 1MB + 2MB)
        Assert.assertEquals(4.0, result.getValue("total_size_mb"), 0.001);
        
        // Total time should be 3.5 seconds (1 + 0.5 + 2)
        Assert.assertEquals(3.5, result.getValue("total_time_sec"), 0.001);
    }

    @Test(expected = Exception.class)
    public void testInvalidSizeFormat() throws Exception {
        // Create test data with invalid size format
        List<Row> rows = Arrays.asList(
            createRow("data_transfer_size", "invalid", "response_time", "100ms")
        );

        // Define the recipe
        String[] recipe = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };

        // Execute the recipe - should throw exception
        TestingRig.execute(recipe, rows);
    }

    @Test(expected = Exception.class)
    public void testInvalidTimeFormat() throws Exception {
        // Create test data with invalid time format
        List<Row> rows = Arrays.asList(
            createRow("data_transfer_size", "1MB", "response_time", "invalid")
        );

        // Define the recipe
        String[] recipe = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };

        // Execute the recipe - should throw exception
        TestingRig.execute(recipe, rows);
    }

    private Row createRow(String sizeColumn, String sizeValue, String timeColumn, String timeValue) {
        Row row = new Row();
        row.add(sizeColumn, sizeValue);
        row.add(timeColumn, timeValue);
        return row;
    }
} 