package com.idega.block.entity.presentation.converter.editable;

import java.util.Iterator;
import java.util.List;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.block.entity.presentation.converter.ConverterConstants;
import com.idega.data.EntityRepresentation;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Parameter;
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
public class EditOkayButtonConverter implements EntityToPresentationObjectConverter {
	
  boolean addCancel = true;
	List maintainParametersList = null;

    /**
	 * @return
	 */
	public List getParametersToMaintain() {
		return this.maintainParametersList;
	}

	/**
	 * @param maintainParametersList
	 */
	public void maintainParameters(List maintainParametersList) {
		this.maintainParametersList = maintainParametersList;
	}

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
    
    Table table = new Table(2,1);
		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(getBundleIdentifier()).getResourceBundle(iwc.getCurrentLocale());
    

    Integer id = (Integer) ((EntityRepresentation) value).getPrimaryKey();
    if (iwc.isParameterSet(ConverterConstants.EDIT_ENTITY_KEY)) {
      String idEditEntity = iwc.getParameter(ConverterConstants.EDIT_ENTITY_KEY);
      try {
        Integer primaryKey = new Integer(idEditEntity);
        if (id.equals(primaryKey))  {
          SubmitButton button = new SubmitButton(iwrb.getLocalizedString("ok.button","ok"), ConverterConstants.EDIT_ENTITY_SUBMIT_KEY, id.toString());
          button.setAsImageButton(true);
		  table.add(button,1,1);
          if(this.addCancel){
            Parameter showAllEntries = browser.getShowAllEntriesParameter();
           	Link cancel = new Link(iwrb.getLocalizedImageButton("cancel.button","cancel"));
            cancel.addParameter(showAllEntries);
          	cancel.addParameter(ConverterConstants.SUBMIT_CANCEL_KEY,"true");
          	if(this.maintainParametersList!=null && !this.maintainParametersList.isEmpty()){
          		Iterator iter = this.maintainParametersList.iterator();
          		while (iter.hasNext()) {
			      String param = (String) iter.next();
 				  cancel.maintainParameter(param,iwc);
                }
          		
          	}
          	table.add(cancel,2,1);
          }
          
          return table;
 
        }
      }
      catch (NumberFormatException ex)  {
      }
    }
    return new Text("");
  }
  
  public void addCancelButton(boolean addCancel){
  	this.addCancel = addCancel;
  }
  
  public String getBundleIdentifier(){
    return EntityBrowser.IW_BUNDLE_IDENTIFIER;
  }

}
