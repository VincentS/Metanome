/*
 * Copyright 2015 by the Metanome project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.metanome.frontend.client.results.pagination_table;

import com.google.gwt.user.cellview.client.TextColumn;

import de.metanome.backend.result_postprocessing.result_comparator.ConditionalUniqueColumnCombinationResultComparator;
import de.metanome.backend.result_postprocessing.results.ConditionalUniqueColumnCombinationResult;
import de.metanome.backend.results_db.ResultType;

import java.util.ArrayList;
import java.util.List;

/**
 * Pagination table for conditional unique column combination results.
 */
public class ConditionalUniqueColumnCombinationPaginationTable
    extends AbstractPaginationTable<ConditionalUniqueColumnCombinationResult> {

  /**
   * Constructs the table for given result type
   *
   * @param resultType the result type
   */
  public ConditionalUniqueColumnCombinationPaginationTable(ResultType resultType) {
    super(resultType);
  }

  /**
   * Initializes the table columns
   *
   * @return Returns the list of sort properties for sortable columns
   */
  @Override
  protected List<String> initializeColumns() {
    List<String> columnNames = new ArrayList<>();

    // Unique Column Combination column
    TextColumn<ConditionalUniqueColumnCombinationResult>
        columnCombinationColumn =
        new TextColumn<ConditionalUniqueColumnCombinationResult>() {
          @Override
          public String getValue(ConditionalUniqueColumnCombinationResult columnCombination) {
            return columnCombination.getColumnCombination().toString();
          }
        };
    this.table.addColumn(columnCombinationColumn, "Column Combination");
    columnNames.add(ConditionalUniqueColumnCombinationResultComparator.COLUMN_COMBINATION_COLUMN);

    // condition column
    TextColumn<ConditionalUniqueColumnCombinationResult>
        conditionColumn =
        new TextColumn<ConditionalUniqueColumnCombinationResult>() {
          @Override
          public String getValue(ConditionalUniqueColumnCombinationResult columnCombination) {
            return columnCombination.getCondition().toString();
          }
        };
    this.table.addColumn(conditionColumn, "Condition");
    columnNames.add(ConditionalUniqueColumnCombinationResultComparator.CONDITION_COLUMN);

    // coverage column
    TextColumn<ConditionalUniqueColumnCombinationResult>
        coverageColumn =
        new TextColumn<ConditionalUniqueColumnCombinationResult>() {
          @Override
          public String getValue(ConditionalUniqueColumnCombinationResult columnCombination) {
            return String.valueOf(columnCombination.getCondition().getCoverage());
          }
        };
    this.table.addColumn(coverageColumn, "Coverage");
    columnNames.add(ConditionalUniqueColumnCombinationResultComparator.COVERAGE_COLUMN);

    // column ratio column
    TextColumn<ConditionalUniqueColumnCombinationResult>
        columnRatioColumn =
        new TextColumn<ConditionalUniqueColumnCombinationResult>() {
          @Override
          public String getValue(ConditionalUniqueColumnCombinationResult columnCombination) {
            return String.valueOf(columnCombination.getColumnRatio());
          }
        };
    this.table.addColumn(columnRatioColumn, "Column Ratio");
    columnNames.add(ConditionalUniqueColumnCombinationResultComparator.COLUMN_RATIO);

    // Set all columns as sortable
    for (int i = 0; i < table.getColumnCount(); i++) {
      table.getColumn(i).setSortable(true);
      table.getColumn(i).setDefaultSortAscending(true);
    }

    return columnNames;
  }

}
