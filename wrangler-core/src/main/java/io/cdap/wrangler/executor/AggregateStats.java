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

package io.cdap.wrangler.executor;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.annotations.PublicEvolving;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * A directive that aggregates byte size and time duration values from multiple rows.
 * This directive calculates total size and total/average time duration.
 */
@PublicEvolving
public class AggregateStats implements Directive {
    public static final String NAME = "aggregate-stats";
    private String sizeColumn;
    private String timeColumn;
    private String totalSizeColumn;
    private String totalTimeColumn;
    private long totalBytes = 0;
    private long totalNanoseconds = 0;
    private int rowCount = 0;

    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
        builder.define("size-column", TokenType.COLUMN_NAME);
        builder.define("time-column", TokenType.COLUMN_NAME);
        builder.define("total-size-column", TokenType.COLUMN_NAME);
        builder.define("total-time-column", TokenType.COLUMN_NAME);
        return builder.build();
    }

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        this.sizeColumn = ((ColumnName) args.value("size-column")).value();
        this.timeColumn = ((ColumnName) args.value("time-column")).value();
        this.totalSizeColumn = ((ColumnName) args.value("total-size-column")).value();
        this.totalTimeColumn = ((ColumnName) args.value("total-time-column")).value();
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
        for (Row row : rows) {
            // Process byte size
            Object sizeValue = row.getValue(sizeColumn);
            if (sizeValue instanceof String) {
                ByteSize byteSize = new ByteSize((String) sizeValue);
                totalBytes += byteSize.getBytes();
            }

            // Process time duration
            Object timeValue = row.getValue(timeColumn);
            if (timeValue instanceof String) {
                TimeDuration timeDuration = new TimeDuration((String) timeValue);
                totalNanoseconds += timeDuration.getNanoseconds();
            }

            rowCount++;
        }

        // Create a single row with the aggregated results
        List<Row> result = new ArrayList<>();
        Row aggregatedRow = new Row();
        
        // Convert total bytes to MB
        double totalSizeMB = totalBytes / (1024.0 * 1024.0);
        aggregatedRow.add(totalSizeColumn, totalSizeMB);
        
        // Convert total nanoseconds to seconds
        double totalTimeSec = totalNanoseconds / 1_000_000_000.0;
        aggregatedRow.add(totalTimeColumn, totalTimeSec);
        
        result.add(aggregatedRow);
        return result;
    }

    @Override
    public void destroy() {
        // Clean up any resources if needed
    }
} 