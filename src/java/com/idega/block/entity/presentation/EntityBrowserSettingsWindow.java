package com.idega.block.entity.presentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.idega.block.entity.business.EntityPropertyHandler;
import com.idega.block.entity.business.MultiEntityPropertyHandler;
import com.idega.block.entity.data.EntityPath;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.StyledIWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.IntegerInput;
import com.idega.presentation.ui.SelectionBox;
import com.idega.presentation.ui.SelectionDoubleBox;
import com.idega.presentation.ui.SubmitButton;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class EntityBrowserSettingsWindow extends StyledIWAdminWindow {
  
  public final static String IW_BUNDLE_IDENTIFIER = "com.idega.block.entity";
  
  public final static String LEADING_ENTITY_NAME_KEY = "leading_entity_name";
  
  // use the method getParamterKeyForEntityName to get this key
  public final static String ENTITY_NAME_KEY_PREFIX = "e_n_";
  public final static String DEFAULT_SHORT_KEY_KEY_PREFIX = "s_k_";
  public final static String OPTION_SHORT_KEY_KEY_PREFIX = "s_k_o_";
  
  public final static String DEFAULT_NUMBER_OF_ROWS_KEY = "default_rows_key";
  
  private final static String FORM_SUBMIT_KEY = "browser_setting_mode";
  private final static String ACTION_SAVE_FORM = "save_form";
  private final static String ACTION_CLOSE = "close_form";
  
  private final static String RIGHT_SELECTION_BOX_KEY = "right_selection_box_key";
  private final static String INPUTFIELD_KEY = "input_field";
  
  // view settings
  private final static int SIZE_OF_INPUTFIELD = 3;
  private final static int MAX_LENGTH_OF_INPUTFIELD = 3;
  
  private final static String HELP_TEXT_KEY = "entity_browser_settings_window";
  
  private String mainStyleClass = "main";

 
  private MultiEntityPropertyHandler multiEntityPropertyHandler = null;
  
  private SortedMap allPathes;
  
  private List defaultShortKeys;
  private List optionShortKeys;
  
  private int defaultNumberOfRows = 1;
  private List visibleColumns = null;
  
  public EntityBrowserSettingsWindow() {
    setResizable(true);
    setWidth(720);
    setHeight(460);
  }
  
  public String getBundleIdentifier(){
    return EntityBrowser.IW_BUNDLE_IDENTIFIER;
  }  

  public static void setParameters(GenericButton settingsButton, Collection entityNames, Collection defaultShortKeys, Collection optionShortKeys, int defaultNumberOfRows) {
  	if (optionShortKeys != null) {
  		Iterator iterator = optionShortKeys.iterator();
  		int i = 0;
  		while (iterator.hasNext()) {
  			String shortKeyOption = (String) iterator.next();
  			StringBuffer buffer = new StringBuffer(OPTION_SHORT_KEY_KEY_PREFIX);
  			buffer.append(i++);
  			settingsButton.addParameter(buffer.toString(), shortKeyOption);
  		}
  	}
  	setParameters(settingsButton, entityNames, defaultShortKeys, defaultNumberOfRows);
  }
  			
  
  
  public static void setParameters(GenericButton settingsButton, Collection entityNames, Collection defaultShortKeys, int defaultNumberOfRows) {
    if (entityNames != null)  {
      Iterator iterator = entityNames.iterator();
      int i = 0;
      while (iterator.hasNext())  {
        String entityName = (String) iterator.next();
        StringBuffer buffer = new StringBuffer(ENTITY_NAME_KEY_PREFIX);
        buffer.append(i++);
        settingsButton.addParameter(buffer.toString(), entityName);
      }
    }
    if (defaultShortKeys != null) {
      int i = 0;
      Iterator iterator = defaultShortKeys.iterator();
      while (iterator.hasNext())  {
        String shortKey = (String) iterator.next();
        StringBuffer buffer = new StringBuffer(DEFAULT_SHORT_KEY_KEY_PREFIX);
        buffer.append(i++);
        settingsButton.addParameter(buffer.toString(), shortKey);
      }
    }
    settingsButton.addParameter(DEFAULT_NUMBER_OF_ROWS_KEY, Integer.toString(defaultNumberOfRows));
  }
 
  public static void setParameters(Form form, Collection entityNames, Collection defaultShortKeys, Collection optionShortKeys, int defaultNumberOfRows) {
  	if (optionShortKeys != null) {
  		Iterator iterator = optionShortKeys.iterator();
  		int i = 0;
  		while (iterator.hasNext()) {
  			String shortKeyOption = (String) iterator.next();
  			StringBuffer buffer = new StringBuffer(OPTION_SHORT_KEY_KEY_PREFIX);
  			buffer.append(i++);
  			form.addParameter(buffer.toString(), shortKeyOption);
  		}
  	}
  	setParameters(form, entityNames, defaultShortKeys, defaultNumberOfRows);
  }
  
  public static void setParameters(Form form, Collection entityNames, Collection defaultShortKeys, int defaultNumberOfRows) {
    if (entityNames != null)  {
      Iterator iterator = entityNames.iterator();
      int i = 0;
      while (iterator.hasNext())  {
        String entityName = (String) iterator.next();
        StringBuffer buffer = new StringBuffer(ENTITY_NAME_KEY_PREFIX);
        buffer.append(i++);
        form.addParameter(buffer.toString(), entityName);
      }
    }
    if (entityNames != null)  {
      int i = 0;
      Iterator iterator = defaultShortKeys.iterator();
      while (iterator.hasNext())  {
        String shortKey = (String) iterator.next();
        StringBuffer buffer = new StringBuffer(DEFAULT_SHORT_KEY_KEY_PREFIX);
        buffer.append(i++);
        form.addParameter(buffer.toString(), shortKey);
      }
    }
    form.addParameter(DEFAULT_NUMBER_OF_ROWS_KEY, Integer.toString(defaultNumberOfRows));
  }

    
  public void main(IWContext iwc){
    // get resource bundle 
    IWResourceBundle iwrb = getResourceBundle(iwc);
    setTitle(iwrb.getLocalizedString("settings", "Settings"));
    addTitle(iwrb.getLocalizedString("settings", "Settings"), TITLE_STYLECLASS);
    
    if (! initialize(iwc))
      setErrorContent();
    if (! doAction(iwc))
      return;
    setContent(iwc);
  }
  
  private boolean initialize(IWContext iwc)  {
    // this parameter is necessary    
    if (! iwc.isParameterSet(LEADING_ENTITY_NAME_KEY))  
      return false;
    // get the parameter  
    String leadingEntityName = iwc.getParameter(LEADING_ENTITY_NAME_KEY);
    try {
      multiEntityPropertyHandler = new MultiEntityPropertyHandler(iwc, leadingEntityName);
      // get the parameters of the foreign entities and
      // initialize the multiEntityPropertyHandler
      String key = ENTITY_NAME_KEY_PREFIX + "0";
      int i = 1;
      while (iwc.isParameterSet(key)) {
        multiEntityPropertyHandler.addEntity(iwc.getParameter(key));
        StringBuffer buffer = new StringBuffer(ENTITY_NAME_KEY_PREFIX);
        buffer.append(i++);
        key = buffer.toString();
      }
      // get the option short keys
      optionShortKeys = new ArrayList();
      key = OPTION_SHORT_KEY_KEY_PREFIX + "0";
      i = 1;
      while (iwc.isParameterSet(key)) {
        optionShortKeys.add(iwc.getParameter(key));
        StringBuffer buffer = new StringBuffer(OPTION_SHORT_KEY_KEY_PREFIX);
        buffer.append(i++);
        key = buffer.toString();
      }
      // get the default short keys
      defaultShortKeys = new ArrayList();
      key = DEFAULT_SHORT_KEY_KEY_PREFIX + "0";
      i = 1;
      while (iwc.isParameterSet(key)) {
        defaultShortKeys.add(iwc.getParameter(key));
        StringBuffer buffer = new StringBuffer(DEFAULT_SHORT_KEY_KEY_PREFIX);
        buffer.append(i++);
        key = buffer.toString();
      }
      // get the default number of rows
      if (iwc.isParameterSet(DEFAULT_NUMBER_OF_ROWS_KEY))  {
        defaultNumberOfRows = Integer.parseInt(iwc.getParameter(DEFAULT_NUMBER_OF_ROWS_KEY));
      }
      
    }
    catch (ClassNotFoundException ex) {
      System.err.println("Class "+"entityName"+ "was not found"+ ex.getMessage());
      ex.printStackTrace(System.err);
      return false;
    }
    // set available entity pathes
    // if option short keys are set do not fetch all entity pathes
    if (optionShortKeys.isEmpty())  {
    	allPathes = multiEntityPropertyHandler.getAllEntityPathes();
    }
    else {
    	allPathes = new TreeMap();
    	Iterator optionShortKeysIterator = optionShortKeys.iterator();
    	while (optionShortKeysIterator.hasNext())  {
    		String shortKey = (String) optionShortKeysIterator.next();
    		// do not add pathes that are default pathes (they are handled later)
    		if (! defaultShortKeys.contains(shortKey)) {
    			EntityPath path = multiEntityPropertyHandler.getEntityPath(shortKey);
    			allPathes.put(shortKey, path);
    		}
    	}
    }
    // get the user settings for the columns
    visibleColumns = multiEntityPropertyHandler.getVisibleOrderedEntityPathes();
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

    if (allPathes == null)  {
      setErrorContent();
      return; 
    }
    
    int numberOfRowsPerPage = multiEntityPropertyHandler.getNumberOfRowsPerPage();
    // if the user has not set the desired number of rows per page fetch default value 
    if (numberOfRowsPerPage == EntityPropertyHandler.NUMBER_OF_ROWS_PER_PAGE_NOT_SET)
      numberOfRowsPerPage = defaultNumberOfRows;
    
    List allColumnsColl = new ArrayList();
    // get existing settings of the user

    // if the user has not chosen any columns use default pathes else 
    // add the default values to the available columns list
    List targetListForDefaultPathes = (visibleColumns.isEmpty()) ? visibleColumns : allColumnsColl; 
    // get default columns
    Iterator defaultShortKeysIterator = defaultShortKeys.iterator();
    while (defaultShortKeysIterator.hasNext())  {
      String shortKey = (String) defaultShortKeysIterator.next();
      targetListForDefaultPathes.add(multiEntityPropertyHandler.getEntityPath(shortKey));
    }
    // get short keys
    List visibleColumnKeys = new ArrayList();
    Iterator iteratorVisibleColumns = visibleColumns.iterator();
    while (iteratorVisibleColumns.hasNext())  {
      EntityPath path = (EntityPath) iteratorVisibleColumns.next();
      visibleColumnKeys.add(path.getShortKey());
    }

    allColumnsColl.addAll(allPathes.values());
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
    
    Help help = getHelp(HELP_TEXT_KEY);
        
    // choose visible columns and order of them 
		SelectionDoubleBox selectionDoubleBox = getColumnsChooserDoubleSelectionBox(resourceBundle, availableColumns, visibleColumns);
    // how many rows per page
    IntegerInput rowsInput = getNumberPerPageInputField(numberOfRowsPerPage, iwuc);
    // close Button
    SubmitButton closeButton = 
      new SubmitButton(resourceBundle.getLocalizedString("close","CLOSE"),FORM_SUBMIT_KEY,ACTION_CLOSE);
      closeButton.setAsImageButton(true);
    // save Button 
    SubmitButton saveButton = 
      new SubmitButton(resourceBundle.getLocalizedString("save","SAVE"), FORM_SUBMIT_KEY,ACTION_SAVE_FORM);
      saveButton.setAsImageButton(true);
    // create form 
    // add selection double box
    Table formTable = new Table(2,3);
    formTable.setStyleClass(mainStyleClass);
    formTable.mergeCells(1,1,2,1);
    formTable.mergeCells(1,2,2,2);
    formTable.add(selectionDoubleBox,1,1);
    // add inputField for number of rows
    Table inputTable = new Table(2,1);
    Text descriptionInput = new Text(getLocalizedString("number_of_rows_per_page:", "Number of rows per page:", iwuc));
//    descriptionInput.setFontFace(Text.FONT_FACE_VERDANA);
//    descriptionInput.setFontSize(2);
    inputTable.add(descriptionInput, 1,1);
    inputTable.add(rowsInput,2,1);
    formTable.add(inputTable,1,2);
    //helpTable
    Table helpTable = new Table(1,1);
    helpTable.add(help,1,1);
    // add buttons
    Table buttonTable = new Table(2,1);
    buttonTable.add(closeButton,1,1);
    buttonTable.add(saveButton,2,1);
    formTable.setAlignment(1,3, "left");
    formTable.setAlignment(2,3,"right");
    formTable.add(helpTable,1,3);
    formTable.add(buttonTable,2,3);
    Form form = new Form();  
    form.add(formTable);
    // the name of the entity is necessary for initializing this class
    form.add(new HiddenInput(LEADING_ENTITY_NAME_KEY, multiEntityPropertyHandler.getLeadingEntityClassName()));
    EntityBrowserSettingsWindow.setParameters(form, multiEntityPropertyHandler.getEntityNames(),defaultShortKeys, optionShortKeys,defaultNumberOfRows); 
    //get the iwcontext for the add-method in StyledIWAdminWindow
    IWContext iwc = IWContext.getInstance();  
    // finally add form        
    add(form,iwc);
  }

  /** gets selection double box
   * 
   */
	private SelectionDoubleBox getColumnsChooserDoubleSelectionBox(IWResourceBundle resourceBundle, List availableColumns, List visibleColumns) {
		// create selection double box and set parameter string
		SelectionDoubleBox selectionDoubleBox = 
      new SelectionDoubleBox(RIGHT_SELECTION_BOX_KEY,
      		resourceBundle.getLocalizedString("available_columns","Available columns"),
					resourceBundle.getLocalizedString("visible_columns","Visible Columns"));
    // set size
    selectionDoubleBox.getRightBox().setWidth("300");
    selectionDoubleBox.getLeftBox().setWidth("300");   
    selectionDoubleBox.getRightBox().setHeight("20");
    selectionDoubleBox.getLeftBox().setHeight("20");   
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
        // reload parent after when settings were saved
        setOnLoad("window.opener.location.reload()");
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
          if (numberOfRows > 0)
            multiEntityPropertyHandler.setNumberOfRowsPerPage(numberOfRows);
        }
      }
    }
    return true;
  }
      
  
  private void setVisibleColumns(String[] selectedKeys) {
    // if the selected keys equals to the default keys do nothing
    List selectedKeysList = Arrays.asList(selectedKeys);
    if (defaultShortKeys.equals(selectedKeysList) && visibleColumns.isEmpty())
      return;
    List entityPathes = new ArrayList();
    Iterator iterator = selectedKeysList.iterator();
    while (iterator.hasNext())  {
      String shortKey = (String) iterator.next();
      EntityPath path = multiEntityPropertyHandler.getEntityPath(shortKey);
      entityPathes.add(path);
    }
    multiEntityPropertyHandler.setVisibleOrderedEntityPathes(entityPathes); 
    visibleColumns = entityPathes;
  }
 
  
}
