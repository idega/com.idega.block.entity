package com.idega.block.entity.business;

import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 *
 * This interface is used to define own converters for the EntityBrowser.
 * The EntityBrowser provides a default converter, that is used if 
 * a special converter was not defined. 
 * That inner class is a good example how to implement this interface.
 *
 */
public interface EntityToPresentationObjectConverter {
  
  /** If you don't want to implement this method, just use the following line:
   * return browser.getDefaultConverter().getHeadergetHeaderPresentationObject(entityPath, browser, iwc);
   * 
   * @param entityPath
   * @param browser
   * @param iwc
   * @return
   */
  public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc);
  
  public PresentationObject getPresentationObject(Object value, EntityPath path, EntityBrowser browser, IWContext iwc);
}
