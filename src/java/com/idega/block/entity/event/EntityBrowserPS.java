package com.idega.block.entity.event;

import java.util.Enumeration;
import java.util.Hashtable;

import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationStateImpl;
import com.idega.idegaweb.IWException;
import com.idega.presentation.IWContext;
import com.idega.presentation.event.ResetPresentationEvent;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class EntityBrowserPS extends IWPresentationStateImpl implements IWActionListener {

  private Hashtable parameterValues = null;

  public void reset(){
    this.parameterValues = null;
    super.reset();
  }

  public boolean isParameterSet(String parameter) {
    return (this.parameterValues != null && this.parameterValues.containsKey(parameter));
  }

  public String getParameter(String parameter) {
    return (this.parameterValues != null) ? (String) this.parameterValues.get(parameter) : null;
  }

  public void actionPerformed(IWPresentationEvent e)throws IWException{
    
    
    if (e instanceof EntityBrowserEvent)  {
      // store all parameters
      IWContext mainIwc = e.getIWContext();
      this.parameterValues = new Hashtable();
      Enumeration enumeration = mainIwc.getParameterNames();
      while (enumeration.hasMoreElements()) {
        String parameter = (String) enumeration.nextElement();
        if (mainIwc.isParameterSet(parameter)) {
			this.parameterValues.put(parameter, mainIwc.getParameter(parameter));
		}
      }
      this.fireStateChanged();
    }
    if (e instanceof ResetPresentationEvent) {
      this.reset();
      this.fireStateChanged();
    }


  }


}