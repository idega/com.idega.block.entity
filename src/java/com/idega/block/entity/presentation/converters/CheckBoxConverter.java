package com.idega.block.entity.presentation.converters;

import java.util.ArrayList;
import java.util.List;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.data.EntityRepresentation;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
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
  
  private static final String DEFAULT_KEY = "selected";
  
  private String key = DEFAULT_KEY;
  
  public CheckBoxConverter() {
  }
    
  public CheckBoxConverter(String key) {
    setKeyForCheckBox(key);
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
   * @return a list of primary keys (Integer) or an empty list (is never null)
   */
  public static List getResultByParsing(IWContext iwc, String key) {
    List result = new ArrayList();
    key = (key == null || key.length() == 0) ? DEFAULT_KEY : key;
    if (iwc.isParameterSet(key))  {
      String[] id = iwc.getParameterValues(key);
      for (int i = 0; i < id.length; i++) {
        try {
          Integer primaryKey = new Integer(id[i]);
          result.add(primaryKey);
        }
        catch (NumberFormatException ex)  {
          // do nothings
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
  public static boolean isEntityChecked(IWContext iwc, String key, Integer id) {
    return getResultByParsing(iwc, key).contains(id);
  }
  
  
  
  public void setKeyForCheckBox(String key) {
    this.key = (key == null || key.length() == 0) ? DEFAULT_KEY : key;      
  }
  
  public String getKeyForCheckBox() {
    return key;
  }
  

  /* (non-Javadoc)
   * @see com.idega.block.entity.business.EntityToPresentationObjectConverter#getHeaderPresentationObject(com.idega.block.entity.data.EntityPath, com.idega.block.entity.presentation.EntityBrowser, com.idega.presentation.IWContext)
   */
  public PresentationObject getHeaderPresentationObject(
    EntityPath entityPath,
    EntityBrowser browser,
    IWContext iwc) {
    CheckBox checkAllCheckBox = new CheckBox("checkAll");
    checkAllCheckBox.setToCheckOnClick(key, "this.checked");
    return checkAllCheckBox;
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
    return new CheckBox(key, idoEntity.getPrimaryKey().toString());
  }

}
