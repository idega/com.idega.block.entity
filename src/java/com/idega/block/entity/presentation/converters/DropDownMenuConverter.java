package com.idega.block.entity.presentation.converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.data.EntityPathValueContainer;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.data.EntityRepresentation;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Parameter;
import com.idega.presentation.ui.SubmitButton;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: idega Software</p>
 * @author <a href="thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Jul 7, 2003
 */
public class DropDownMenuConverter
  implements EntityToPresentationObjectConverter {

  private static final String LINK_KEY = "dd_link";
  private static final String DROPDOWNMENU_KEY = "dd_input";
  private static final String DROPDOWNMENU_KEY_PREVIOUS_VALUE = "dd_prevValue";
  private static final String SUBMIT_KEY = "dd_submit";
  private static final char DELIMITER = '<';
  
  protected OptionProvider optionProvider = null;
  protected List maintainParameterList = new ArrayList(0);
  private Form externalForm = null;
  private boolean  editable = true;
  
  
  // flag 
  private boolean workWithExternalSubmitButton = true;
  
  public void setWorkWithExternalSubmitButton(boolean workWithExternalSubmitButton) {
    this.workWithExternalSubmitButton = workWithExternalSubmitButton;
  }

  public void setEditable(boolean editable)  {
    this.editable = editable;
  }
  
  public DropDownMenuConverter(Form externalForm) {
    this.externalForm = externalForm;
  } 

  public static EntityPathValueContainer getResultByParsing(IWContext iwc) {
    String entityPathShortKey = null;
    Integer id = null;
    String submitKey = getGeneralSubmitKey();
    if (iwc.isParameterSet(submitKey))  {
      String action = iwc.getParameter(submitKey);
      StringTokenizer tokenizer = new StringTokenizer(action, String.valueOf(DELIMITER));
      if (tokenizer.hasMoreTokens())  {
        // set shortkey
        entityPathShortKey = tokenizer.nextToken();
      }
      if (tokenizer.hasMoreTokens())  {
        // set id of entity
        try {
        id = new Integer(tokenizer.nextToken());
        }
        catch (NumberFormatException ex)  {
          System.err.println("[DropDownMenuConverter] Could not retrieve id of entity. Message is: "+ ex.getMessage());
          ex.printStackTrace(System.err);
          id = null;
        }
      }
    }
    return getResultByEntityIdAndEntityPathShortKey(id, entityPathShortKey, iwc);
  }
  
  public static EntityPathValueContainer getResultByEntityIdAndEntityPathShortKey(Integer id, String entityPathShortKey, IWContext iwc)  {
    EntityPathValueContainer container = new EntityPathValueContainer();
    container.setEntityId(id);
    container.setEntityPathShortKey(entityPathShortKey);
    // set current chosen value
    String key = getDropdownMenuUniqueKey(id, entityPathShortKey);
    if (iwc.isParameterSet(key))  {
      Object value = iwc.getParameter(key);
      container.setValue(value);
    }
    // set previous value (that is the current value of the entity)
    String keyPreviousValue = getDropDownMenuUniqueKeyPreviousValue(id, entityPathShortKey);
    if (iwc.isParameterSet(keyPreviousValue))  {
      Object value = iwc.getParameter(keyPreviousValue);
      container.setPreviousValue(value);
    }
    return container;
  }
    

  /* (non-Javadoc)
   * @see com.idega.block.entity.business.EntityToPresentationObjectConverter#getHeaderPresentationObject(com.idega.block.entity.data.EntityPath, com.idega.block.entity.presentation.EntityBrowser, com.idega.presentation.IWContext)
   */
  public PresentationObject getHeaderPresentationObject (
    EntityPath entityPath,
    EntityBrowser browser,
    IWContext iwc) {
      return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc); 
  }

  public void setOptionProvider(OptionProvider optionProvider)  {
    this.optionProvider = optionProvider;   
  }


  /** This method uses a copy of the specified list */
  public void maintainParameters(List maintainParameters) {
    this.maintainParameterList.addAll(maintainParameters);
  }

  /* (non-Javadoc)
   * @see com.idega.block.entity.business.EntityToPresentationObjectConverter#getPresentationObject(java.lang.Object, com.idega.block.entity.data.EntityPath, com.idega.block.entity.presentation.EntityBrowser, com.idega.presentation.IWContext)
   */
  public PresentationObject getPresentationObject(
    Object entity,
    EntityPath path,
    EntityBrowser browser,
    IWContext iwc) {
    	  	
    Object value = getValue(entity,path,browser,iwc);
    if( value == null ) value = "";
    
    Integer id = (Integer) ((EntityRepresentation) entity).getPrimaryKey();
    // show drop down menu without a submit button if the entity is new
    boolean newEntity = id.equals(ConverterConstants.NEW_ENTITY_ID);
    boolean editEntity = false;
    String shortKeyPath = path.getShortKey();
    String uniqueKeyLink = getLinkUniqueKey(id, shortKeyPath);
    if (iwc.isParameterSet(ConverterConstants.EDIT_ENTITY_KEY)) {
      String idEditEntity = iwc.getParameter(ConverterConstants.EDIT_ENTITY_KEY);
      Integer primaryKey = null;

      try {
        primaryKey = new Integer(idEditEntity);
        editEntity = id.equals(primaryKey);
      }
      catch (NumberFormatException ex)  {
      }
    }
    // decide to show a link or a drop down menu
    if (newEntity || 
        editEntity ||
        iwc.isParameterSet(uniqueKeyLink)) {
      // show drop down menu with submitButton
      String uniqueKeyDropdownMenu = getDropdownMenuUniqueKey(id, shortKeyPath);
      DropdownMenu dropdownMenu = 
        getDropdownMenu(
          value, 
          uniqueKeyDropdownMenu,
          entity,
          path,
          browser,
          iwc);
      // add old value as hidden value
      externalForm.addParameter(getDropDownMenuUniqueKeyPreviousValue(id, shortKeyPath), value.toString());  
      Table table = (newEntity) ? new Table(1,1) : new Table(2,1);
      table.add(dropdownMenu,1,1);
      // add submit button
      if (! editEntity && ! newEntity) {    
        SubmitButton button = new SubmitButton("OK", getGeneralSubmitKey(), getUniqueKey(id, shortKeyPath).toString());
        button.setAsImageButton(true);
        table.add(button,2,1);
      }
      return table;      
    } 
    else {
      // show link
      return getLink(value, uniqueKeyLink, id.toString(), browser, iwc);
      
    }
      
  }
  
  protected PresentationObject getLink(Object value, String uniqueKeyLink, String id, EntityBrowser browser, IWContext iwc)  {
  	
    String display = (value!=null) ? value.toString() : "";
    display = (display.length() == 0) ? "_" : display;
    if (! editable) {
      return new Text(display);
    }
    Link link = new Link(display);
    if (workWithExternalSubmitButton) {
      link.addParameter(ConverterConstants.EDIT_ENTITY_KEY, id);
    }
    else {
      link.addParameter(uniqueKeyLink,"dummy_value");
    }
    // add maintain parameters
    Iterator iteratorList = maintainParameterList.iterator();
    while (iteratorList.hasNext())  {
      String parameter = (String) iteratorList.next();
      link.maintainParameter(parameter, iwc);
    }
    Parameter showAllEntries = browser.getShowAllEntriesParameter();
    link.addParameter(showAllEntries);
    return link;
  	
  }
  
  protected Object getValue(
      Object entity,
      EntityPath path,
      EntityBrowser browser,
      IWContext iwc)  {
    Object object = path.getValue((EntityRepresentation) entity);
    return (object == null) ? "" : object;
  }      
    
  
  protected DropdownMenu getDropdownMenu(
      Object preselection, 
      String name,
      Object entity,
      EntityPath path,
      EntityBrowser browser,
      IWContext iwc)  {
    Map options = (optionProvider == null) ? new HashMap(0) : optionProvider.getOptions(entity,path,browser,iwc);  
    DropdownMenu dropdownMenu = new DropdownMenu(name);
    Iterator iterator = options.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry option = (Map.Entry) iterator.next();
      String value = option.getKey().toString();
      String display = option.getValue().toString();
      // key is option, value is option
      dropdownMenu.addMenuElement(value, display);
    }
    // set preselection
    if (preselection != null) {
      // sometimes the preselection does not exist, 
      // add to options without localization
      String preselectionAsString = preselection.toString();
      if (! options.containsKey(preselectionAsString)) {
        dropdownMenu.addMenuElement(preselectionAsString, preselectionAsString);
      }
      dropdownMenu.setSelectedElement(preselectionAsString);
    }
    return dropdownMenu;
  }
    

  private String getLinkUniqueKey(Integer id, String shortKeyOfPath)  {
    StringBuffer buffer = getUniqueKey(id, shortKeyOfPath).append(DELIMITER).append(LINK_KEY);
    return buffer.toString();
  }

  private static String getDropdownMenuUniqueKey(Integer id, String shortKeyOfPath)  {
    StringBuffer buffer = getUniqueKey(id, shortKeyOfPath).append(DELIMITER).append(DROPDOWNMENU_KEY);
    return buffer.toString();
  }
  
  private static String getDropDownMenuUniqueKeyPreviousValue(Integer id, String shortKeyOfPath) {
    StringBuffer buffer = getUniqueKey(id, shortKeyOfPath).append(DELIMITER).append(DROPDOWNMENU_KEY_PREVIOUS_VALUE);
    return buffer.toString();
  }       
  
  private static String getGeneralSubmitKey()  {
    return SUBMIT_KEY;
  }  
  
  
  private static StringBuffer getUniqueKey(Integer id, String shortKeyOfPath) {
    if (id == null) {
      id = new Integer(-1);
    }
    if (shortKeyOfPath == null || shortKeyOfPath.length() == 0) {
      shortKeyOfPath = "dummy";
    }
    StringBuffer buffer = new StringBuffer(shortKeyOfPath);
    buffer.append(DELIMITER);
    buffer.append(id);
    return buffer;
  }
}
