package com.idega.block.entity.event;

import com.idega.event.IWPresentationEvent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Page;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class EntityBrowserEvent extends IWPresentationEvent {
  
  
  private static final String ENTITY_NAME = "entity_name";

  private String entityName = null;

	/**
	 * @see com.idega.event.IWPresentationEvent#initializeEvent(com.idega.presentation.IWContext)
	 */
  public boolean initializeEvent(IWContext iwc) {
    // null if parameter is not set
    entityName = iwc.getParameter(ENTITY_NAME);
    return true;
	}
  
	/**
	 * Returns the entityName.
	 * @return String
	 */
	public String getEntityName() {
		return entityName;
	}

	/**
	 * Sets the entityName.
	 * @param entityName The entityName to set
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
    addParameter(EntityBrowserEvent.ENTITY_NAME, entityName);
	}

}
