package com.idega.block.entity.event;

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



  private String parameter = null;
  
  private String entityName = null;


  public void reset(){
    parameter = null;
    super.reset();

  }


  public String getParameter() {
    return parameter;
  }
  
  
  public String getEntityName() {
    return entityName;
  }


  public void actionPerformed(IWPresentationEvent e)throws IWException{
    
    
    if (e instanceof EntityBrowserEvent)  {
      IWContext mainIwc = e.getIWContext();
      parameter = mainIwc.getParameter(EntityBrowser.NEW_SUBSET_KEY);
      entityName = ((EntityBrowserEvent)e).getEntityName();
      // why is this important?
      this.fireStateChanged();
    }
    

    if(e instanceof ResetPresentationEvent){
      this.reset();
      this.fireStateChanged();
    }


  }


}