package com.idega.block.entity.event;

import com.idega.event.IWPresentationEvent;
import com.idega.presentation.IWContext;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class EntityBrowserEvent extends IWPresentationEvent {
	/**
	 * @see com.idega.event.IWPresentationEvent#initializeEvent(com.idega.presentation.IWContext)
	 */
	public boolean initializeEvent(IWContext iwc) {
    // there is nothing to initialize
    return true;
	}
}
