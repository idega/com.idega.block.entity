package com.idega.block.entity.presentation.converters;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.data.IDOEntity;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.ui.CheckBox;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: idega Software</p>
 * @author <a href="thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Apr 14, 2003
 */
public class CheckBoxConverter implements EntityToPresentationObjectConverter {
  
  public CheckBoxConverter() {
  }
    
  public CheckBoxConverter(String key) {
    this.key = key;
  }  
  
  
  
  private String key = "selected";
  
  public void setKeyForCheckBox(String key) {
    this.key = key;
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
    CheckBox checkAllCheckBox = new CheckBox(key);
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
    IDOEntity idoEntity = (IDOEntity) entity;
    return new CheckBox(key, idoEntity.getPrimaryKey().toString());
  }

}
