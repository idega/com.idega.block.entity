package com.idega.block.entity.event;

import java.util.Enumeration;
import java.util.Hashtable;

import com.idega.event.*;
import com.idega.event.IWPresentationStateImpl;

import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.presentation.event.ResetPresentationEvent;
import com.idega.presentation.IWContext;
import com.idega.idegaweb.IWException;
import com.idega.event.*;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class EntityBrowserPS extends IWPresentationStateImpl implements IWActionListener {



  private Hashtable parameterValues = null;
  
  private String entityName = null;


  public void reset(){
    parameterValues = null;
    entityName = null;
    super.reset();

  }

  public boolean isParameterSet(String parameter) {
    return (parameterValues != null && parameterValues.containsKey(parameter));
  }


  public String getParameter(String parameter) {
    return (parameterValues != null) ? (String) parameterValues.get(parameter) : null;
  }
  
  
  public String getEntityName() {
    return entityName;
  }


  public void actionPerformed(IWPresentationEvent e)throws IWException{
    
    
    if (e instanceof EntityBrowserEvent)  {
      // store all parameters
      IWContext mainIwc = e.getIWContext();
      parameterValues = new Hashtable();
      Enumeration enumeration = mainIwc.getParameterNames();
      while (enumeration.hasMoreElements()) {
        String parameter = (String) enumeration.nextElement();
        if (mainIwc.isParameterSet(parameter)) 
          parameterValues.put(parameter, mainIwc.getParameter(parameter));
      }
      entityName = ((EntityBrowserEvent)e).getEntityName();
      this.fireStateChanged();
    }
    

    if(e instanceof ResetPresentationEvent){
      this.reset();
      this.fireStateChanged();
    }


  }


}