package com.idega.block.entity.presentation.converters;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.data.EntityRepresentation;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.SubmitButton;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: idega Software</p>
 * @author <a href="thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Aug 26, 2003
 */
public class EditOkayButtonConverter
  implements EntityToPresentationObjectConverter {

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
    Integer id = (Integer) ((EntityRepresentation) value).getPrimaryKey();
    if (iwc.isParameterSet(ConverterConstants.EDIT_ENTITY_KEY)) {
      String idEditEntity = iwc.getParameter(ConverterConstants.EDIT_ENTITY_KEY);
      try {
        Integer primaryKey = new Integer(idEditEntity);
        if (id.equals(primaryKey))  {
          SubmitButton button = new SubmitButton("OK", ConverterConstants.EDIT_ENTITY_SUBMIT_KEY, id.toString());
          button.setAsImageButton(true);
          return button;
        }
      }
      catch (NumberFormatException ex)  {
      }
    }
    return new Text("");
  }

}