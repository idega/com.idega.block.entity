package com.idega.block.entity.presentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import com.idega.block.entity.business.EntityPropertyHandler;
import com.idega.block.entity.data.EntityPath;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.presentation.IWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.IntegerInput;
import com.idega.presentation.ui.SelectionBox;
import com.idega.presentation.ui.SelectionDoubleBox;
import com.idega.presentation.ui.SubmitButton;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class EntityBrowserSettingWindow extends IWAdminWindow {
  
  
  public final static String ENTITY_NAME_KEY = "entity_name";
  
  private final static String FORM_SUBMIT_KEY = "browser_setting_mode";
  private final static String ACTION_SAVE_FORM = "save_form";
  private final static String ACTION_CLOSE = "close_form";
  
  private final static String RIGHT_SELECTION_BOX_KEY = "right_selection_box_key";
  private final static String INPUTFIELD_KEY = "input_field";
  
  // view settings
  private final static int SIZE_OF_INPUTFIELD = 3;
  private final static int MAX_LENGTH_OF_INPUTFIELD = 3;

 
  private EntityPropertyHandler propertyHandler = null;
  
  private SortedMap allColumns;
  
  public EntityBrowserSettingWindow() {
    setResizable(true);
    setWidth(600);
    setHeight(230);
  }
    
  public void main(IWContext iwc){
    
    

    if (! initialize(iwc))
      setErrorContent();
    
    if (! doAction(iwc))
      return;
    setContent(iwc);
    setParentToReload();
  }
 
  

  
 
  
  private boolean initialize(IWContext iwc)  {
    // this parameter is necessary    
    if (! iwc.isParameterSet(ENTITY_NAME_KEY))  
      return false;
    // get the parameter  
    String entityName = iwc.getParameter(ENTITY_NAME_KEY);
    // set propertyHandler
    try {
      propertyHandler = new EntityPropertyHandler(iwc, entityName);
    }
    catch (ClassNotFoundException ex) {
      System.err.println("Class "+"entityName"+ "was not found"+ ex.getMessage());
      ex.printStackTrace(System.err);
      return false;
    }
    // set available entity pathes
    allColumns = propertyHandler.getAllEntityPathes();

    return true;
  }
    
  private void fillBox(IWResourceBundle resourceBundle, SelectionBox selectionBox, List entityPathes) {
    Iterator iterator = entityPathes.iterator();
    while (iterator.hasNext())  {
      EntityPath entityPath = (EntityPath) iterator.next();
      String displayString = entityPath.getLocalizedDescription(resourceBundle);
      selectionBox.addElement( entityPath.getShortKey(), displayString );
    }
  }
  
  private void setContent(IWUserContext iwuc) {
    // get all columns of the corresponding table

    if (allColumns == null)  {
      setErrorContent();
      return; 
    }
    
    int numberOfRowsPerPage = propertyHandler.getNumberOfRowsPerPage();
    
    // get existing settings of the user
    List visibleColumns = propertyHandler.getVisibleOrderedEntityPathes();
    // get short keys
    List visibleColumnKeys = new ArrayList();
    Iterator iteratorVisibleColumns = visibleColumns.iterator();
    while (iteratorVisibleColumns.hasNext())  {
      EntityPath path = (EntityPath) iteratorVisibleColumns.next();
      visibleColumnKeys.add(path.getShortKey());
    }

    Collection allColumnsColl = allColumns.values();

    List availableColumns = new ArrayList();
    // fill list of available columns without visible columns
    Iterator iterator = allColumnsColl.iterator();
    while (iterator.hasNext())  {
      EntityPath entityPath = (EntityPath) iterator.next();
      if (! visibleColumnKeys.contains(entityPath.getShortKey()))
        availableColumns.add(entityPath);
    }
    
    // get resourceBundle
    IWResourceBundle resourceBundle = getResourceBundle(iwuc);
        
    // choose visible columns and order of the them 
		SelectionDoubleBox selectionDoubleBox = getColumnsChooserDoubleSelectionBox(resourceBundle, availableColumns, visibleColumns);
    // how many rows per page
    IntegerInput rowsInput = getNumberPerPageInputField(numberOfRowsPerPage, iwuc);
    // save Button

    SubmitButton saveButton = 
      new SubmitButton(resourceBundle.getLocalizedImageButton("close","CLOSE"),FORM_SUBMIT_KEY,ACTION_CLOSE);
    // close Button 
    SubmitButton closeButton = 
      new SubmitButton(resourceBundle.getLocalizedImageButton("save","SAVE"), FORM_SUBMIT_KEY,ACTION_SAVE_FORM);
    // create form 
    // add selection double box
    Table formTable = new Table(1,3);
    formTable.add(selectionDoubleBox,1,1);
    // add inputField
    Table inputTable = new Table(2,1);
    String descriptionInput = getLocalizedString("number_of_rows_per_page", "Number of rows per page", iwuc);
    inputTable.add(descriptionInput, 1,1);
    inputTable.add(rowsInput);
    formTable.add(inputTable,1,2);
    // add buttons
    Table buttonTable = new Table(2,1);
    buttonTable.add(saveButton,1,1);
    buttonTable.add(closeButton,2,1);
    formTable.add(buttonTable,1,3);
    Form form = new Form();  
    form.add(formTable);
    // the name of the entity is necessary for initializing this class
    form.add(new HiddenInput(ENTITY_NAME_KEY, propertyHandler.getEntityClassName()));   
    // finally add form        
    add(form);
  }

  /** gets selection double box
   * 
   */
	private SelectionDoubleBox getColumnsChooserDoubleSelectionBox(IWResourceBundle resourceBundle, List availableColumns, List visibleColumns) {
		// create selection double box and set parameter string
		SelectionDoubleBox selectionDoubleBox = 
      new SelectionDoubleBox(RIGHT_SELECTION_BOX_KEY,"Available columns","Visible Columns");
		// submit selection on right box
		selectionDoubleBox.getRightBox().selectAllOnSubmit();
		// build SelectionDoubleBox
		fillBox(resourceBundle, selectionDoubleBox.getLeftBox(), availableColumns); 
		// get visible columns from user properties
		fillBox(resourceBundle, selectionDoubleBox.getRightBox(), visibleColumns);
		selectionDoubleBox.getRightBox().addUpAndDownMovers();
		return selectionDoubleBox;
	}
    
  
  /**
   * gets input field
   */
  private IntegerInput getNumberPerPageInputField(int initValue, IWUserContext iwuc)  {
    String integerErrorWarning = getLocalizedString("input_must_be_an_integer", "The input must be an integer", iwuc);
    String notEmptyWarning = getLocalizedString("input_is_mandatory", "The input is mandatory", iwuc);
    IntegerInput input = new IntegerInput(INPUTFIELD_KEY, initValue, integerErrorWarning);
    // set size and maxLength
    input.setSize(SIZE_OF_INPUTFIELD);
    input.setMaxlength(MAX_LENGTH_OF_INPUTFIELD);
    // set not empty
    input.setAsNotEmpty(notEmptyWarning);
    return input;
  }
    
    
  
  
  private void setErrorContent()  {
  }
  
  private boolean doAction(IWContext iwc) {
    if (iwc.isParameterSet(FORM_SUBMIT_KEY)) { 
      String action = iwc.getParameter(FORM_SUBMIT_KEY);
      if ( ACTION_CLOSE.equals(action)) {
        // close window
        close();
        return false;
      }
      else if ( ACTION_SAVE_FORM.equals(action))  {
        // if the parameter RIGHT_SELECTION_BOX_KEY is not set nothing was selected
        String[] selectedColumns =
          (iwc.isParameterSet(RIGHT_SELECTION_BOX_KEY)) ?
           iwc.getParameterValues(RIGHT_SELECTION_BOX_KEY) : new String[] {};
        setVisibleColumns(selectedColumns);
        if (iwc.isParameterSet(INPUTFIELD_KEY)) {
          // set value
          String numberOfRowsPerPage = iwc.getParameter(INPUTFIELD_KEY);
          // get absolute value
          int numberOfRows = Math.abs(Integer.parseInt(numberOfRowsPerPage));
          propertyHandler.setNumberOfRowsPerPage(numberOfRows);
        }
      }
    }
    return true;
  }
      
  
  private void setVisibleColumns(String [] selectedKeys) {
    List entityPathes = new ArrayList();
    int i;
    for (i=0; i < selectedKeys.length; i++) {
      EntityPath path = (EntityPath) allColumns.get(selectedKeys[i]);
      entityPathes.add(path);
    }
    propertyHandler.setVisibleOrderedEntityPathes(entityPathes);    
  }
 
  
}
