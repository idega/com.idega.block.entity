package com.idega.block.entity.presentation.converter;

import java.text.DateFormat;
import java.util.Locale;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.data.EntityRepresentation;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Text;
import com.idega.util.IWTimestamp;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: idega Software</p>
 * @author <a href="thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Apr 30, 2003
 */
public class DateConverter implements EntityToPresentationObjectConverter {

  /* (non-Javadoc)
   * @see com.idega.block.entity.business.EntityToPresentationObjectConverter#getHeaderPresentationObject(com.idega.block.entity.data.EntityPath, com.idega.block.entity.presentation.EntityBrowser, com.idega.presentation.IWContext)
   */
  public PresentationObject getHeaderPresentationObject(
    EntityPath entityPath,
    EntityBrowser browser,
    IWContext iwc) {
    return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc); 
  }

  /* (non-Javadoc)
   * @see com.idega.block.entity.business.EntityToPresentationObjectConverter#getPresentationObject(java.lang.Object, com.idega.block.entity.data.EntityPath, com.idega.block.entity.presentation.EntityBrowser, com.idega.presentation.IWContext)
   */
  public PresentationObject getPresentationObject(
    Object value,
    EntityPath path,
    EntityBrowser browser,
    IWContext iwc) {
    Object object = path.getValue((EntityRepresentation) value);
    if (object == null)  {
      return new Text("");
    }
    Locale locale = iwc.getIWMainApplication().getSettings().getDefaultLocale();  
    IWTimestamp timestamp = new IWTimestamp(object.toString());
    String date = timestamp.getLocaleDate(locale, DateFormat.LONG);
    return new Text(date);
  }

}
