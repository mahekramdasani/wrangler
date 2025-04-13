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

import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Identifier;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;


import java.util.Collections;
import java.util.List;


/**
 * A directive to aggregate ByteSize and TimeDuration columns.
 * Example: aggregate-stats :data_size :response_time total_size_mb total_time_sec
 */
@Name("aggregate-stats")
@Description("Aggregate size and time values using byte/time duration parsers")
@Categories(categories = { "transform", "aggregate" })
public class AggregateStats implements Directive {
  private String sizeInputCol;
  private String timeInputCol;
  private String sizeOutputCol;
  private String timeOutputCol;

  private long totalBytes = 0;
  private long totalMillis = 0;

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
    long totalBytes = 0;
    long totalMillis = 0;
  
    for (Row row : rows) {
      Object sizeObj = row.getValue(sizeInputCol);
      Object timeObj = row.getValue(timeInputCol);
  
      long sizeVal = ((ByteSize) sizeObj).value();
      long timeVal = ((TimeDuration) timeObj).value();
  
      totalBytes += sizeVal;
      totalMillis += timeVal;
    }
  
    // ✅ Emit only one row with totals converted
    Row result = new Row();
    result.add(sizeOutputCol, totalBytes / (1024.0 * 1024.0)); // bytes to MB
    result.add(timeOutputCol, totalMillis / 1000.0);           // ms to sec
    return Collections.singletonList(result);
  }
  


  @Override
  public void destroy() {
    // No cleanup needed for this directive
  }
}
