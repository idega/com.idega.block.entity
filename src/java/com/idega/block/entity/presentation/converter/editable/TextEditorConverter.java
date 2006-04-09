package com.idega.block.entity.presentation.converter.editable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.data.EntityPathValueContainer;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.block.entity.presentation.converter.ConverterConstants;
import com.idega.data.EntityRepresentation;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Parameter;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: idega Software</p>
 * @author <a href="thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Jul 3, 2003
 */
public class TextEditorConverter implements EntityToPresentationObjectConverter{

  private static final String LINK_KEY = "te_link";
  private static final String TEXTINPUT_KEY = "te_input";
  private static final String TEXTINPUT_KEY_PREVIOUS_VALUE = "te_prevValue";
  private static final String SUBMIT_KEY = "te_submit";
  private static final char DELIMITER = '|';
  
  private List maintainParameterList = new ArrayList(0);
  private Form externalForm = null;
  
  // flag 
  private boolean workWithExternalSubmitButton = true;
  
  // flag if editable
  private boolean editable = true;
  
  // if set the text input allows only float values
  private boolean isFloatField = false;
  private String setAsFloatMessage = null;
  // if set the text input allows only double values
  private boolean isDoubleField = false;
  private String setAsDoubleMessage = null;
  // if set the text input allows only social security numbers
  private boolean isIcelandicSocialSecurityNumberField = false;
  private String setAsIcelandicSocialSecurityNumberMessage = null;
  
  public void setEditable(boolean editable)  {
    this.editable = editable;
  }
  
  public void setWorkWithExternalSubmitButton(boolean workWithExternalSubmitButton) {
    this.workWithExternalSubmitButton = workWithExternalSubmitButton;
  }
  
  public void setAsFloat(String setAsFloatMessage)  {
    setAsFloatOrDoubleOrIclandicSocialSecurityNumber(setAsFloatMessage, null, null);
  }
  
  public void setAsDouble(String setAsDoubleMessage) {
  	setAsFloatOrDoubleOrIclandicSocialSecurityNumber(null,setAsDoubleMessage, null);
  }
  
  public void setAsIcelandicSocialSecurityNumber(String setAsIcelandicSocialSecurityNumberMessage)  {
    setAsFloatOrDoubleOrIclandicSocialSecurityNumber(null, null,setAsIcelandicSocialSecurityNumberMessage);
  }
  
  private void setAsFloatOrDoubleOrIclandicSocialSecurityNumber(String setAsFloatMessage, String setAsDoubleMessage, String setAsIcelandicSocialSecurityNumberMessage) {	
  	this.isFloatField = setAsFloatMessage != null;
  	this.isDoubleField = setAsDoubleMessage != null;
  	this.isIcelandicSocialSecurityNumberField  = setAsIcelandicSocialSecurityNumberMessage != null;
  	this.setAsFloatMessage = setAsFloatMessage;
  	this.setAsDoubleMessage = setAsDoubleMessage;
  	this.setAsIcelandicSocialSecurityNumberMessage = setAsIcelandicSocialSecurityNumberMessage;
  }
  
  public TextEditorConverter(Form externalForm) {
    this.externalForm = externalForm;
  }
  
  public static EntityPathValueContainer getResultByParsing(IWContext iwc) {
    String submitKey = getGeneralSubmitKey();
    String entityPathShortKey = null;
    Integer id = null;
    if (iwc.isParameterSet(submitKey))  {
      String action = iwc.getParameter(submitKey);
      StringTokenizer tokenizer = new StringTokenizer(action, String.valueOf(DELIMITER));
      // set short key of path
      if (tokenizer.hasMoreTokens())  {
        entityPathShortKey = tokenizer.nextToken();
      }
      // set id of entity
      if (tokenizer.hasMoreTokens())  {
        try {
          id = new Integer(tokenizer.nextToken());
        }
        catch (NumberFormatException ex)  {
          System.err.println("[TextInputConverter] Could not retrieve id of entity. Message is: "+ ex.getMessage());
          ex.printStackTrace(System.err);
          id = null;
        }
      }
    }
    return getResultByEntityIdAndEntityPathShortKey(id, entityPathShortKey, iwc) ;
  }

  public static EntityPathValueContainer getResultByEntityIdAndEntityPathShortKey(Integer id, String entityPathShortKey, IWContext iwc)  {
    EntityPathValueContainer container = new EntityPathValueContainer();
    container.setEntityId(id);
    container.setEntityPathShortKey(entityPathShortKey);
    // set current chosen value
    String key = getTextInputUniqueKey(id, entityPathShortKey);
    if (iwc.isParameterSet(key))  {
      Object value = iwc.getParameter(key);
      container.setValue(value);
    }
    else {
      container.setValue("");
    }
    // set previous value (that is the current value of the entity)
    String keyPreviousValue = getTextInputUniqueKeyPreviousValue(id, entityPathShortKey);
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
   

    Integer id = (Integer) ((EntityRepresentation) entity).getPrimaryKey();
    // show text input without a submit button if the entity is new 
    boolean newEntity = id.equals(ConverterConstants.NEW_ENTITY_ID);
    String shortKeyPath = path.getShortKey();
    String uniqueKeyLink = getLinkUniqueKey(id, shortKeyPath);
   	boolean isRequestSender =  iwc.isParameterSet(uniqueKeyLink);
    boolean editEntity = false;
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
    // decide to show a link or a text inputfield
    if (newEntity || 
        editEntity || 
        isRequestSender) {
      Object value = getValueForInput(entity, path, browser, iwc);
      String text = value.toString(); 
      // show text input with submitButton
      String uniqueKeyTextInput = getTextInputUniqueKey(id, shortKeyPath);
      TextInput textInput = new TextInput( uniqueKeyTextInput, text);
      if (isRequestSender)	{
      	textInput.setInFocusOnPageLoad(true);
      }
      if (this.isFloatField) {
        textInput.setAsFloat(this.setAsFloatMessage);
      }
      else if (this.isDoubleField) {
      	textInput.setAsDouble(this.setAsDoubleMessage);
      }
      else if (this.isIcelandicSocialSecurityNumberField) { 
        textInput.setAsIcelandicSSNumber(this.setAsIcelandicSocialSecurityNumberMessage);
        textInput.setAsNotEmpty(this.setAsIcelandicSocialSecurityNumberMessage);
      }

      // add old value as hidden value
      this.externalForm.addParameter(getTextInputUniqueKeyPreviousValue(id, shortKeyPath), text);  

      Table table = (newEntity) ? new Table(1,1) : new Table(2,1);
      table.add(textInput,1,1);
      if (! editEntity && ! newEntity) {
        SubmitButton button = new SubmitButton("OK", getGeneralSubmitKey(), getUniqueKey(id, shortKeyPath).toString());
        button.setAsImageButton(true);
        table.add(button,2,1);
      }
      return table;      
    } 
    else {
      // show link
      Object value = getValueForLink(entity, path, browser, iwc);
      String text = value.toString();
      text = (text.length() == 0) ? "_" : text;  
      if (! this.editable) {
        return new Text(text);
      }
      Link link = new Link(text);
      if (this.workWithExternalSubmitButton) {
        link.addParameter(ConverterConstants.EDIT_ENTITY_KEY, id.toString());
        link.addParameter(uniqueKeyLink,"dummy_Value");
      }
      else {
        link.addParameter(uniqueKeyLink,"dummy_value");
      }
      // add some parameters from browser
      Parameter showAllEntriesParameter = browser.getShowAllEntriesParameter();
      link.addParameter(showAllEntriesParameter);
      // add maintain parameters
      Iterator iteratorList = this.maintainParameterList.iterator();
      while (iteratorList.hasNext())  {
        String parameter = (String) iteratorList.next();
        link.maintainParameter(parameter, iwc);
      }
      return link;
    }
      
  }
  /** Overwrite this method if necessary */
 protected Object getValueForLink(
      Object entity,
      EntityPath path,
      EntityBrowser browser,
      IWContext iwc)  {
    return getValue(entity, path, browser, iwc);
 }
 
 /** Overwrite this method if necessary */
 protected Object getValueForInput(
      Object entity,
      EntityPath path,
      EntityBrowser browser,
      IWContext iwc)  {
    return getValue(entity, path, browser, iwc);
  }
  
  /** Overwrite this method if necessary */
  protected Object getValue(
      Object entity,
      EntityPath path,
      EntityBrowser browser,
      IWContext iwc)  {
    Object object = path.getValue((EntityRepresentation) entity);
    return (object == null) ? "" : object;
  }        

  protected String getLinkUniqueKey(Integer id, String shortKeyOfPath)  {
    StringBuffer buffer = getUniqueKey(id, shortKeyOfPath).append(DELIMITER).append(LINK_KEY);
    return buffer.toString();
  }

  private static String getTextInputUniqueKey(Integer id, String shortKeyOfPath)  {
    StringBuffer buffer = getUniqueKey(id, shortKeyOfPath).append(DELIMITER).append(TEXTINPUT_KEY);
    return buffer.toString();
  }
  
  private static String getTextInputUniqueKeyPreviousValue(Integer id, String shortKeyOfPath)  {
    StringBuffer buffer = getUniqueKey(id, shortKeyOfPath).append(DELIMITER).append(TEXTINPUT_KEY_PREVIOUS_VALUE);
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
