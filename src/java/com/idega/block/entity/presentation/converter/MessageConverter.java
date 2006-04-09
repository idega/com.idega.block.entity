package com.idega.block.entity.presentation.converter;

import java.util.Map;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.data.IDOEntity;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Text;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: idega Software</p>
 * @author <a href="thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Apr 26, 2003
 */
public class MessageConverter implements EntityToPresentationObjectConverter {
  
  private Map entityMessageMap = null;

  /* (non-Javadoc)
   * @see com.idega.block.entity.business.EntityToPresentationObjectConverter#getHeaderPresentationObject(com.idega.block.entity.data.EntityPath, com.idega.block.entity.presentation.EntityBrowser, com.idega.presentation.IWContext)
   */
  public PresentationObject getHeaderPresentationObject(
    EntityPath entityPath,
    EntityBrowser browser,
    IWContext iwc) {
    return new Text("");
  }

  /* (non-Javadoc)
   * @see com.idega.block.entity.business.EntityToPresentationObjectConverter#getPresentationObject(java.lang.Object, com.idega.block.entity.data.EntityPath, com.idega.block.entity.presentation.EntityBrowser, com.idega.presentation.IWContext)
   */
  public PresentationObject getPresentationObject(
    Object value,
    EntityPath path,
    EntityBrowser browser,
    IWContext iwc) {
    IDOEntity entity = (IDOEntity) value;
    Integer id = (Integer) entity.getPrimaryKey();
    String message = (String) this.entityMessageMap.get(id);
    if (message == null)  {
      message = "";
    }
    return new Text(message);
  }
  
  public void setEntityMessageMap(Map entityMessageMap) {
    this.entityMessageMap = entityMessageMap;
  }
  

}
