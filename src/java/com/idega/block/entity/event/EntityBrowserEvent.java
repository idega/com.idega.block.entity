package com.idega.block.entity.event;

import com.idega.event.IWPresentationEvent;
import com.idega.presentation.IWContext;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class EntityBrowserEvent extends IWPresentationEvent {
  
  
  private static final String EVENT_NAME = "event_name";

  private String eventName = null;

	/**
	 * @see com.idega.event.IWPresentationEvent#initializeEvent(com.idega.presentation.IWContext)
	 */
  public boolean initializeEvent(IWContext iwc) {
    // null if parameter is not set
    this.eventName = iwc.getParameter(EVENT_NAME);
    return true;
	}
  
	/**
	 * Returns the name of the event 
	 * @return String
	 */
	public String getEventName() {
		return this.eventName;
	}

	/**
	 * Sets the name of this event.
	 * @param eventName The name of the event
	 */
	public void setEventName(String eventName) {
		this.eventName = eventName;
    addParameter(EVENT_NAME, eventName);
	}

}
