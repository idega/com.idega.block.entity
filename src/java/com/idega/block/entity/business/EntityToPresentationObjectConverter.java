package com.idega.block.entity.business;

import com.idega.block.entity.data.EntityPath;
import com.idega.data.GenericEntity;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public interface EntityToPresentationObjectConverter {
  
  public PresentationObject getPresentationObject(Object value, EntityPath path, IWContext iwc);
}
