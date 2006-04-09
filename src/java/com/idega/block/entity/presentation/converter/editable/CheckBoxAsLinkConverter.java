package com.idega.block.entity.presentation.converter.editable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.block.entity.presentation.converter.CheckBoxConverter;
import com.idega.block.entity.presentation.converter.ConverterConstants;
import com.idega.data.EntityRepresentation;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.Parameter;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: idega Software</p>
 * @author <a href="thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Sep 11, 2003
 */
public class CheckBoxAsLinkConverter extends CheckBoxConverter {

  private List maintainParameterList = new ArrayList(0);
  
  private Map primaryKeyShouldBeChecked = null;  
  
  public CheckBoxAsLinkConverter()  {
    super();
  }
    
  public CheckBoxAsLinkConverter(String key) {
    super(key);
  }
    
  /** This method uses a copy of the specified list */
  public void maintainParameters(List maintainParameters) {
    this.maintainParameterList.addAll(maintainParameters);
  }    

  /** This method uses a copy of the specified map */        
  public void setPrimaryKeyShouldBeCheckedMap(Map primaryKeyShouldBeCheckedMap)  {
    this.primaryKeyShouldBeChecked = new HashMap(primaryKeyShouldBeCheckedMap);
  }

  public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc)  {
    return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
  }


        
  public PresentationObject getPresentationObject(
    Object entity,
    EntityPath path,
    EntityBrowser browser,
    IWContext iwc) {
      
    EntityRepresentation idoEntity = (EntityRepresentation) entity;
    Integer id = (Integer) idoEntity.getPrimaryKey();

    boolean shouldBeChecked = shouldEntityBeChecked(idoEntity, id);

    //boolean disableCheckBox = true;
    if (iwc.isParameterSet(ConverterConstants.EDIT_ENTITY_KEY)) {
      String idEditEntity = iwc.getParameter(ConverterConstants.EDIT_ENTITY_KEY);
      try {
        Integer primaryKey = new Integer(idEditEntity);
        if (id.equals(primaryKey))  {
          CheckBox checkBox = new CheckBox(getKeyForCheckBox(), id.toString());
          checkBox.setChecked(shouldBeChecked);
          return checkBox;
        }
      }
      catch (NumberFormatException ex)  {
      }
    }
    String text;
    if (shouldBeChecked) {
      // black dot
      text = "X";
    }
    else {
      text = "_";
    }
    if (! this.editable) {
      return new Text(text);
    }
    Link link = new Link(text);
    Parameter showAllEntries = browser.getShowAllEntriesParameter();
    link.addParameter(showAllEntries);
    link.addParameter(ConverterConstants.EDIT_ENTITY_KEY, id.toString());
     // add maintain parameters
    Iterator iteratorList = this.maintainParameterList.iterator();
    while (iteratorList.hasNext())  {
      String parameter = (String) iteratorList.next();
      link.maintainParameter(parameter, iwc);
    }
    return link;
  }
  
  /** Overwrite this method if necessary */
  protected boolean shouldEntityBeChecked(Object entity, Integer primaryKey) {
    
    if (this.primaryKeyShouldBeChecked == null) {
      return false;
    }
    Boolean shouldBeChecked = (Boolean) this.primaryKeyShouldBeChecked.get(primaryKey);
    return (shouldBeChecked == null) ? false : shouldBeChecked.booleanValue();
  } 
  
}
