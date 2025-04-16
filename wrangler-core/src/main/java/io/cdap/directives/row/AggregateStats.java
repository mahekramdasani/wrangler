/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */
package io.cdap.directives.row;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;

import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.annotations.PublicEvolving;

import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Identifier;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A directive to aggregate ByteSize and TimeDuration columns.
 * Example: aggregate-stats :data_size :response_time total_size_mb total_time_sec
 */
@Name("aggregate-stats")
@Description("Aggregate size and time values using byte/time duration parsers")
@Categories(categories = { "transform", "aggregate" })
@PublicEvolving
public class AggregateStats implements Directive {
  private String sizeInputCol;
  private String timeInputCol;
  private String sizeOutputCol;
  private String timeOutputCol;

  private long totalBytes = 0;
  private long totalNanoseconds = 0;
  private int rowCount = 0;

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
    builder.define("sizeInputCol", TokenType.COLUMN_NAME);
    builder.define("timeInputCol", TokenType.COLUMN_NAME);
    builder.define("sizeOutputCol", TokenType.IDENTIFIER);
    builder.define("timeOutputCol", TokenType.IDENTIFIER);
    return builder.build();
  }

  @Override
  public void initialize(Arguments arguments) {
    this.sizeInputCol = ((ColumnName) arguments.value("sizeInputCol")).value();
    this.timeInputCol = ((ColumnName) arguments.value("timeInputCol")).value();
    this.sizeOutputCol = ((Identifier) arguments.value("sizeOutputCol")).value();
    this.timeOutputCol = ((Identifier) arguments.value("timeOutputCol")).value();
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    for (Row row : rows) {
      // Process byte size
      Object sizeValue = row.getValue(sizeInputCol);
      if (sizeValue instanceof String) {
        ByteSize byteSize = new ByteSize((String) sizeValue);
        totalBytes += ((Number) byteSize.value()).longValue();
      }

      // Process time duration
      Object timeValue = row.getValue(timeInputCol);
      if (timeValue instanceof String) {
        TimeDuration timeDuration = new TimeDuration((String) timeValue);
        totalNanoseconds += ((Number) timeDuration.value()).longValue();
      }

      rowCount++;
    }

    // Create a single row with the aggregated results
    List<Row> result = new ArrayList<>();
    Row aggregatedRow = new Row();
    
    // Convert total bytes to MB
    double totalSizeMB = totalBytes / (1024.0 * 1024.0);
    aggregatedRow.add(sizeOutputCol, totalSizeMB);
    
    // Convert total nanoseconds to seconds
    double totalTimeSec = totalNanoseconds / 1_000_000_000.0;
    aggregatedRow.add(timeOutputCol, totalTimeSec);
    
    result.add(aggregatedRow);
    return result;
  }
  
  @Override
  public void destroy() {
    // No cleanup needed for this directive
  }
}

