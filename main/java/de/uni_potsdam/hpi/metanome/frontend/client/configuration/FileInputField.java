/*
 * Copyright 2014 by the Metanome project
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

package de.uni_potsdam.hpi.metanome.frontend.client.configuration;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

import de.uni_potsdam.hpi.metanome.frontend.client.helpers.InputValidationException;
import de.uni_potsdam.hpi.metanome.frontend.client.services.InputDataService;
import de.uni_potsdam.hpi.metanome.frontend.client.services.InputDataServiceAsync;
import de.uni_potsdam.hpi.metanome.input.csv.CsvFile;
import de.uni_potsdam.hpi.metanome.results_db.FileInput;

import java.text.ParseException;


public class FileInputField extends HorizontalPanel {

  /**
   * Dropdown menu for choosing a CSV file
   */
  private ListBox fileListBox;
  /**
   * Triggers whether advanced options are displayed and evaluated.
   */
  protected CheckBox advancedCheckbox;
  /**
   * Wraps all advanced options' UI elements
   */
  protected FlexTable advancedTable;
  protected TextBox separatorTextbox;
  protected TextBox quoteTextbox;
  protected TextBox escapeTextbox;
  protected IntegerBox skiplinesIntegerbox;
  protected CheckBox strictQuotesCheckbox;
  protected CheckBox ignoreLeadingWhiteSpaceCheckbox;
  protected CheckBox headerCheckbox;
  protected CheckBox skipDifferingLinesCheckbox;


  /**
   * Constructor. Set up all UI elements.
   */
  public FileInputField() {
    HorizontalPanel standardPanel = new HorizontalPanel();
    this.add(standardPanel);

    fileListBox = createListbox();
    standardPanel.add(fileListBox);

    advancedCheckbox = createAdvancedCheckbox();
    standardPanel.add(advancedCheckbox);

    advancedTable = new FlexTable();
    advancedTable.setVisible(false);
    this.add(advancedTable);

    separatorTextbox = getNewOneCharTextbox();
    addRow(advancedTable, separatorTextbox, "Separator Character");

    quoteTextbox = getNewOneCharTextbox();
    addRow(advancedTable, quoteTextbox, "Quote Character");

    escapeTextbox = getNewOneCharTextbox();
    addRow(advancedTable, escapeTextbox, "Escape Character");

    skiplinesIntegerbox = new IntegerBox();
    skiplinesIntegerbox.setWidth("5em");
    addRow(advancedTable, skiplinesIntegerbox, "Line");

    strictQuotesCheckbox = new CheckBox();
    addRow(advancedTable, strictQuotesCheckbox, "Strict Quotes");

    ignoreLeadingWhiteSpaceCheckbox = new CheckBox();
    addRow(advancedTable, ignoreLeadingWhiteSpaceCheckbox, "Ignore Leading Whitespace");

    headerCheckbox = new CheckBox();
    addRow(advancedTable, headerCheckbox, "Has Header");

    skipDifferingLinesCheckbox = new CheckBox();
    addRow(advancedTable, skipDifferingLinesCheckbox, "Skip Lines With Differing Length");

    setDefaultAdvancedSettings();
  }

  /**
   * Set the default values of the advanced settings
   */
  private void setDefaultAdvancedSettings() {
    setSeparator(CSVParser.DEFAULT_SEPARATOR);
    setQuotechar(CSVParser.DEFAULT_QUOTE_CHARACTER);
    setEscapechar(CSVParser.DEFAULT_ESCAPE_CHARACTER);
    setSkipLines(CSVReader.DEFAULT_SKIP_LINES);
    setStrictQuotes(CSVParser.DEFAULT_STRICT_QUOTES);
    setIgnoreLeadingWhiteSpace(CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE);
    setHasHeader(CsvFile.DEFAULT_HAS_HEADER);
    setSkipDifferingLines(CsvFile.DEFAULT_SKIP_DIFFERING_LINES);
  }

  /**
   * Add another user input row to the bottom of the given table
   *
   * @param table       the UI object on which to add the row
   * @param inputWidget the widget to be used for input
   * @param name        the name of the input property
   */
  protected void addRow(FlexTable table, Widget inputWidget, String name) {
    int row = table.getRowCount();
    table.setText(row, 0, name);
    table.setWidget(row, 1, inputWidget);
  }

  /**
   * Creates a UI element for one-character user input
   *
   * @return a TextBox with width and input length limited to 2 (>1 to allow for escape characters)
   */
  private TextBox getNewOneCharTextbox() {
    TextBox textbox = new TextBox();
    textbox.setMaxLength(2);
    textbox.setWidth("2em");
    return textbox;
  }

  /**
   * Create the CheckBox that triggers the display/hiding of advanced CSV configuration parameters
   *
   * @return the CheckBox with the mentioned behavior
   */
  protected CheckBox createAdvancedCheckbox() {
    CheckBox checkbox = new CheckBox("Use Advanced Configuration");
    checkbox.setValue(false);
    checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        advancedTable.setVisible(advancedCheckbox.getValue());
      }
    });

    return checkbox;
  }

  /**
   * Finds all available CSV files and adds them to a drop-down menu with an empty
   * entry ("--"), which is selected by default but cannot be selected (it is disabled
   * because it does not represent a valid input file).
   *
   * @return a GWT ListBox containing all currently available CSV files
   */
  private ListBox createListbox() {
    ListBox listbox = new ListBox();

    //unselectable default entry
    listbox.addItem("--");
    listbox.getElement().getFirstChildElement().setAttribute("disabled", "disabled");
    //other entries
    addAvailableFiles();
    return listbox;
  }

  /**
   * Calls the InputDataService to retrieve available CSV files (specified by their
   * file paths) and adds them as entries to the given ListBox. Only the actual file
   * name (not the preceding directories) are displayed.
   */
  private void addAvailableFiles() {
    AsyncCallback<String[]> callback = getCallback(this);

    InputDataServiceAsync service = GWT.create(InputDataService.class);
    service.listCsvInputFiles(callback);
  }


  protected AsyncCallback<String[]> getCallback(final FileInputField widget) {
    return new AsyncCallback<String[]>() {
      public void onFailure(Throwable caught) {
        // TODO: Do something with errors.
        caught.printStackTrace();
      }

      public void onSuccess(String[] result) {
        widget.addFilesToListBox(result);
      }
    };
  }

  /**
   * Add files  of CSV files
   * @param files which should be added to the list box
   */
  private void addFilesToListBox(String[] files) {
    for (String fileName: files) {
      this.fileListBox.addItem(fileName);
    }
  }

  /**
   * @return a FileInput with the selected File and the advanced settings, if selected
   */
  public FileInput getValue() throws InputValidationException {
    FileInput fileInput = new FileInput();

    Integer index = this.fileListBox.getSelectedIndex();
    String fileName = this.fileListBox.getValue(index);

    if (fileName.isEmpty())
      throw new InputValidationException("The file name is invalid.");

    fileInput.setFileName(fileName);

    if (this.advancedCheckbox.getValue()){
      return setAdvancedSettings(fileInput);
    }

    return fileInput;
  }

  /**
   * Setting the advanced settings at the given file input.
   * @param fileInput the file input at which the advanced settings should be set
   * @return the file input with set advanced settings
   */
  private FileInput setAdvancedSettings(FileInput fileInput) throws InputValidationException {
    fileInput.setEscapechar(getChar(this.escapeTextbox));
    fileInput.setHasHeader(this.headerCheckbox.getValue());
    fileInput.setIgnoreLeadingWhiteSpace(this.ignoreLeadingWhiteSpaceCheckbox.getValue());
    fileInput.setQuotechar(getChar(this.quoteTextbox));
    fileInput.setSeparator(getChar(this.separatorTextbox));
    fileInput.setSkipDifferingLines(this.skipDifferingLinesCheckbox.getValue());
    fileInput.setSkipLines(getInteger(this.skiplinesIntegerbox));
    fileInput.setStrictQuotes(this.strictQuotesCheckbox.getValue());

    return fileInput;
  }

  /**
   * Checks, if the given text box contains only a character.
   * If yes, the character is returned. Otherwise an exception is thrown.
   * @param textBox
   * @return the character of the text box
   * @throws InputValidationException
   */
  private char getChar(TextBox textBox) throws InputValidationException {
    String value = textBox.getValue();

    if (value.length() != 1)
      throw new InputValidationException(textBox.getName() + " should only contain one character!");

    return value.charAt(0);
  }

  /**
   * Checks, if the value of the integer box is an integer.
   * If yes, the integer is returned. Otherwise an exception is thrown.
   * @param integerBox
   * @return the integer of the integer box
   * @throws InputValidationException
   */
  private int getInteger(IntegerBox integerBox) throws InputValidationException {
    try {
      return integerBox.getValueOrThrow();
    } catch (ParseException e) {
      throw new InputValidationException(integerBox.getName() + " should only contain number!");
    }
  }

  /**
   * Only for tests
   * @param fileName
   */
  protected void setFileName(String fileName) {
    this.fileListBox.addItem(fileName);
    this.fileListBox.setSelectedIndex(1);
  }

  protected void setSeparator(char separator) {
    this.separatorTextbox.setValue(String.valueOf(separator));
  }

  protected void setEscapechar(char escapechar) {
    this.escapeTextbox.setValue(String.valueOf(escapechar));
  }

  protected void setQuotechar(char quotechar) {
    this.quoteTextbox.setValue(String.valueOf(quotechar));
  }

  protected void setSkipLines(int skipLines) {
    this.skiplinesIntegerbox.setValue(skipLines);
  }

  protected void setStrictQuotes(boolean strictQuotes) {
    this.strictQuotesCheckbox.setValue(strictQuotes);
  }

  protected void setIgnoreLeadingWhiteSpace(boolean ignoreLeadingWhiteSpace) {
    this.ignoreLeadingWhiteSpaceCheckbox.setValue(ignoreLeadingWhiteSpace);
  }

  protected void setHasHeader(boolean hasHeader) {
    this.headerCheckbox.setValue(hasHeader);
  }

  protected void setSkipDifferingLines(boolean skipDifferingLines) {
    this.skipDifferingLinesCheckbox.setValue(skipDifferingLines);
  }

}