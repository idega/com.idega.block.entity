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
import com.idega.presentation.ui.DropdownMenu;
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
  private static final String DROPDOWNMENU_KEY = "dd_dropDownInput";
  private static final String SUBMIT_KEY = "dd_submit";
  private static final char DELIMITER = '|';
  
  private OptionProvider optionProvider = null;
  private Map maintainParameterMap = new HashMap(0);
  private List maintainParameterList = new ArrayList(0);

  public static EntityPathValueContainer getResultByParsing(IWContext iwc) {
    EntityPathValueContainer container = new EntityPathValueContainer();
    String submitKey = getGeneralSubmitKey();
    String action = "";
    if (iwc.isParameterSet(submitKey))  {
      action = iwc.getParameter(submitKey);
      StringTokenizer tokenizer = new StringTokenizer(action, String.valueOf(DELIMITER));
      if (tokenizer.hasMoreTokens())  {
        container.setEntityPathShortKey(tokenizer.nextToken());
      }
      if (tokenizer.hasMoreTokens())  {
        container.setEntityId(tokenizer.nextToken());
      }
      String key = getDropdownMenuUniqueKey(container.getEntityId(), container.getEntityPathShortKey()).toString();
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
      return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc); 
  }

  public void setOptionProvider(OptionProvider optionProvider)  {
    this.optionProvider = optionProvider;   
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
    Object value = getValue(entity,path,browser,iwc);  
    Integer id = (Integer) ((IDOEntity) entity).getPrimaryKey();
    String shortKeyPath = path.getShortKey();
    
    String uniqueKeyLink = getLinkUniqueKey(id, shortKeyPath);
    // decide to show a link or a drop down menu
    if (iwc.isParameterSet(uniqueKeyLink)) {
      // show text input with submitButton
      String uniqueKeyDropdownMenu = getDropdownMenuUniqueKey(id, shortKeyPath);
      DropdownMenu dropdownMenu = 
        getDropdownMenu(
          value, 
          uniqueKeyDropdownMenu,
          entity,
          path,
          browser,
          iwc);
      SubmitButton button = new SubmitButton("OK", getGeneralSubmitKey(), getUniqueKey(id, shortKeyPath).toString());
      // add maintain parameters
      Iterator iterator = maintainParameterMap.entrySet().iterator();
      while (iterator.hasNext())  {
        Map.Entry entry = (Map.Entry) iterator.next();
        button.addParameterToWindow((String) entry.getKey(), entry.getValue().toString());
      }
      button.setAsImageButton(true);
      Table table = new Table(2,1);
      table.add(dropdownMenu,1,1);
      table.add(button,2,1);
      return table;      
    } 
    else {
      // show link
      Link link = new Link(value.toString());
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
  
  protected Object getValue(
      Object entity,
      EntityPath path,
      EntityBrowser browser,
      IWContext iwc)  {
    Object object = path.getValue((GenericEntity) entity);
    return (object == null) ? "" : object;
  }      
    
  
  private DropdownMenu getDropdownMenu(
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
      if (! options.containsKey(preselection)) {
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
