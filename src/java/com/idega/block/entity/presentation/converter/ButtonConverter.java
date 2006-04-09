package com.idega.block.entity.presentation.converter;

import java.util.StringTokenizer;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.data.EntityPathValueContainer;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.data.EntityRepresentation;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.ui.SubmitButton;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: idega Software</p>
 * @author <a href="thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Oct 14, 2003
 */
public class ButtonConverter implements EntityToPresentationObjectConverter {
	
	public static final String SUBMIT_KEY = "bc_submit";
	private static final char DELIMITER = '<';


	private String displayName = null;
	private Image image = null;
	private String setOnClick = null;

	private static String getGeneralSubmitKey() {
    return SUBMIT_KEY;
  }  
	
	public static EntityPathValueContainer getResultByParsing(IWContext iwc) {
    String entityPathShortKey = null;
    Integer id = null;
    String submitKey = getGeneralSubmitKey();
    if (iwc.isParameterSet(submitKey))  {
      String action = iwc.getParameter(submitKey);
      StringTokenizer tokenizer = new StringTokenizer(action, String.valueOf(DELIMITER));
      if (tokenizer.hasMoreTokens())  {
        // set shortkey
        entityPathShortKey = tokenizer.nextToken();
      }
      if (tokenizer.hasMoreTokens())  {
        // set id of entity
        try {
        id = new Integer(tokenizer.nextToken());
        }
        catch (NumberFormatException ex)  {
          System.err.println("[ButtonConverter] Could not retrieve id of entity. Message is: "+ ex.getMessage());
          ex.printStackTrace(System.err);
          id = null;
        }
      }
    }
		EntityPathValueContainer container = new EntityPathValueContainer();
    container.setEntityId(id);
    container.setEntityPathShortKey(entityPathShortKey);
		container.setValue("button was  chosen");
		return container;
  }

	
	
	
  private static StringBuffer getUniqueKey(String id, String shortKeyOfPath) {
    if (id == null) {
      id = "-1";
    }
    if (shortKeyOfPath == null || shortKeyOfPath.length() == 0) {
      shortKeyOfPath = "dummy";
    }
    StringBuffer buffer = new StringBuffer(shortKeyOfPath);
    buffer.append(DELIMITER);
    buffer.append(id);
    return buffer;
  }
  
	public ButtonConverter(String displayName)	{
		this.displayName = displayName;
	}

	public ButtonConverter(Image image) {
		this.image = image;
	}

	public void setOnClick(String setOnClick) {
		this.setOnClick = setOnClick;
	}
	
	
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
		String shortKeyPath = path.getShortKey();
		EntityRepresentation idoEntity = (EntityRepresentation) value;
		String id = idoEntity.getPrimaryKey().toString();
		SubmitButton submitButton;
		// clone the image because JSF stores the image as facet with only one parent (and removes the image from the old parent)
		Image tempImage = (Image) this.image.clone();
		if (this.displayName == null)  {   
			submitButton = new SubmitButton( tempImage, getGeneralSubmitKey(), getUniqueKey(id, shortKeyPath).toString(), this.setOnClick);
		}
		else {
		 	submitButton = new SubmitButton( this.displayName, getGeneralSubmitKey(), getUniqueKey(id, shortKeyPath).toString(), this.setOnClick);
			submitButton.setAsImageButton(true);
		}
		return submitButton;
	}
		 
}


