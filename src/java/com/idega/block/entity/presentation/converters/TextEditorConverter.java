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
import com.idega.data.GenericEntity;
import com.idega.data.IDOEntity;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
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
  private static final String TEXTINPUT_KEY = "te_textinput";
  private static final String SUBMIT_KEY = "te_submit";
  private static final char DELIMITER = '|';
  
  private Map maintainParameterMap = new HashMap(0);
  private List maintainParameterList = new ArrayList(0);

  public static EntityPathValueContainer getResultByParsing(IWContext iwc) {
    EntityPathValueContainer container = new EntityPathValueContainer();
    String submitKey = getGeneralSubmitKey();
    String action = "";
    if (iwc.isParameterSet(submitKey))  {
      action = iwc.getParameter(submitKey);
      StringTokenizer tokenizer = new StringTokenizer(action, Character.toString(DELIMITER));
      if (tokenizer.hasMoreTokens())  {
        container.setEntityPathShortKey(tokenizer.nextToken());
      }
      if (tokenizer.hasMoreTokens())  {
        container.setEntityId(tokenizer.nextToken());
      }
      String key = getUniqueKey(container.getEntityId(), container.getEntityPathShortKey()).toString();
      if (iwc.isParameterSet(key))  {
        Object value = iwc.getParameter(key);
        container.setValue(value);
      }
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
      return new Text("");
  }

  /** This method uses a copy of the specified map */
  public void maintainParameters(Map maintainParameters) {
    this.maintainParameterMap.putAll(maintainParameters);
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
    IDOEntity idoEntity = (IDOEntity) entity;
    Object object = path.getValue((GenericEntity) entity);
    String text = (object == null) ? "" : object.toString();      
    Integer id = (Integer) idoEntity.getPrimaryKey();
    String shortKeyPath = path.getShortKey();
    
    String uniqueKeyLink = getLinkUniqueKey(id, shortKeyPath);
    // decide to show a link or a text inputfield
    if (iwc.isParameterSet(uniqueKeyLink)) {
      // show text input with submitButton
      String uniqueKeyTextInput = getTextInputUniqueKey(id, shortKeyPath);
      TextInput textInput = new TextInput( uniqueKeyTextInput, text);
      SubmitButton button = new SubmitButton(getGeneralSubmitKey(), "OK");
      button.setValue(this.getUniqueKey(id, shortKeyPath).toString());
      // add maintain parameters
      Iterator iterator = maintainParameterMap.entrySet().iterator();
      while (iterator.hasNext())  {
        Map.Entry entry = (Map.Entry) iterator.next();
        button.addParameterToWindow((String) entry.getKey(), entry.getValue().toString());
      }
      button.setAsImageButton(true);
      Table table = new Table(2,1);
      table.add(textInput,1,1);
      table.add(button,2,1);
      return table;      
    } 
    else {
      // show link
      Link link = new Link(text);
      link.addParameter(uniqueKeyLink,"dummy_value");
      // add maintain parameters with set values
      Iterator iterator = maintainParameterMap.entrySet().iterator();
      while (iterator.hasNext())  {
        Map.Entry entry = (Map.Entry) iterator.next();
        link.addParameter((String) entry.getKey(), entry.getValue().toString());
      }
      // add maintain parameters
      Iterator iteratorList = maintainParameterList.iterator();
      while (iteratorList.hasNext())  {
        String parameter = (String) iteratorList.next();
        link.maintainParameter(parameter, iwc);
      }
      return link;
    }
      
  }

  private String getLinkUniqueKey(Integer id, String shortKeyOfPath)  {
    StringBuffer buffer = getUniqueKey(id, shortKeyOfPath).append(DELIMITER).append(LINK_KEY);
    return buffer.toString();
  }

  private static String getTextInputUniqueKey(Integer id, String shortKeyOfPath)  {
    StringBuffer buffer = getUniqueKey(id, shortKeyOfPath).append(DELIMITER).append(TEXTINPUT_KEY);
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
