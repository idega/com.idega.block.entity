package com.idega.block.entity.presentation.converter;

import java.util.ArrayList;
import java.util.List;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.data.EntityRepresentation;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.ui.CheckBox;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: 
 * Provides a checkbox for the column and a "check all" checkbox in the header.
 * Instanciate this converter with the desired key for the checkbox and parse the request with this key.
 * The parsing returns a collection of ids (primary keys) of those entities that are checked.
 * </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: idega Software</p>
 * @author <a href="thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Apr 14, 2003
 */
public class CheckBoxConverter implements EntityToPresentationObjectConverter {
  
  protected static final String DEFAULT_KEY = "selected";
  
  protected String key = DEFAULT_KEY;
  
  protected boolean editable = true;
  protected boolean useShortKeyAsKey = false;
  protected boolean showTitle = false;
  
  public static CheckBoxConverter getInstanceUsesShortKeyAsKeyShowsTitle()	{
  	CheckBoxConverter converter = new CheckBoxConverter();
  	converter.showTitle = true;
  	converter.useShortKeyAsKey = true;
  	return converter;
  }
  
  
  public CheckBoxConverter() {
  }
    
  public CheckBoxConverter(String key) {
    setKey(key);
  }  
  
  public void setEditable(boolean editable) {
    this.editable = editable;
  }
  
  /** Gets all ids of the entities that are checked.
   *  This method should only be used if the checkbox converter was instanciated using the default key.
   * @param iwc - Context
   * @return a list of primary keys (Integer) or an empty list (is never null)
   */
  public static List getResultByParsingUsingDefaultKey(IWContext iwc) {
    return getResultByParsing(iwc, DEFAULT_KEY);
  } 
  
  /** Gets all ids of the entities that are checked.
   * @param iwc - Context
   * @param key - the key that was used during instanciation of the checkbox converter
   * @return a list of primary keys (Integer or Strings) or an empty list (is never null)
   */
  public static List getResultByParsing(IWContext iwc, String key) {
    List result = new ArrayList();
    key = (key == null || key.length() == 0) ? DEFAULT_KEY : key;
    if (iwc.isParameterSet(key))  {
      String[] id = iwc.getParameterValues(key);
      for (int i = 0; i < id.length; i++) {
        try {
          Integer primaryKey = new Integer(Integer.parseInt(id[i]));
          result.add(primaryKey);
        }
        catch (NumberFormatException ex)  {
          //its a string
		  result.add(id[i]);
        }
      }
    }
    return result;
  }

  
  /** Checks if the specified entity is checked or not.
   * @param iwc - Context
   * @param key - the key that was used during instanciation of the checkbox converter
   * @param id - the id of an entity
   * @return true if the specified id is checked else false
   */
  public static boolean isEntityChecked(IWContext iwc, String key, Object id) {
    return getResultByParsing(iwc, key).contains(id);
  }
  
  public static boolean isEntityCheckedUsingDefaultKey(IWContext iwc, Object id)  {
    return getResultByParsingUsingDefaultKey(iwc).contains(id);
  }
  
  
  public void setKey(String key) {
    this.key = (key == null || key.length() == 0) ? DEFAULT_KEY : key;      
  }
  
  public String getKeyForCheckBox() {
    return this.key;
  }
  

  /* (non-Javadoc)
   * @see com.idega.block.entity.business.EntityToPresentationObjectConverter#getHeaderPresentationObject(com.idega.block.entity.data.EntityPath, com.idega.block.entity.presentation.EntityBrowser, com.idega.presentation.IWContext)
   */
  public PresentationObject getHeaderPresentationObject(
    EntityPath entityPath,
    EntityBrowser browser,
    IWContext iwc) {
    CheckBox checkAllCheckBox = new CheckBox("checkAll");
    String checkBoxKey = (this.useShortKeyAsKey) ? entityPath.getShortKey() : this.key;
    checkAllCheckBox.setToCheckOnClick(checkBoxKey, "this.checked");
    if (this.showTitle) {
    	PresentationObject presentation = browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
			Table table = new Table(2,1);
			table.add(checkAllCheckBox, 1, 1 );
			table.add(presentation, 2,1);
			return table;
    }
    else {			
    	return checkAllCheckBox;
    }
  }

  /* (non-Javadoc)
   * @see com.idega.block.entity.business.EntityToPresentationObjectConverter#getPresentationObject(java.lang.Object, com.idega.block.entity.data.EntityPath, com.idega.block.entity.presentation.EntityBrowser, com.idega.presentation.IWContext)
   */
  public PresentationObject getPresentationObject(
    Object entity,
    EntityPath path,
    EntityBrowser browser,
    IWContext iwc) {
    EntityRepresentation idoEntity = (EntityRepresentation) entity;
    String checkBoxKey = (this.useShortKeyAsKey) ? path.getShortKey() : this.key; 
    CheckBox checkBox = new CheckBox(checkBoxKey, idoEntity.getPrimaryKey().toString());
    if (! this.editable) {
      checkBox.setDisabled(true);
    }
    return checkBox;
  }

/**
 * @return Returns the showTitle.
 */
public boolean isShowTitle() {
	return this.showTitle;
}

/**
 * @param showTitle The showTitle to set.
 */
public void setShowTitle(boolean showTitle) {
	this.showTitle = showTitle;
}

}
