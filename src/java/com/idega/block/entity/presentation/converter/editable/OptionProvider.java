package com.idega.block.entity.presentation.converter.editable;

import java.util.Map;

import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.presentation.IWContext;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: Used to provide the DropDownMenuConverter with a map of options. </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: idega Software</p>
 * @author <a href="thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Jul 9, 2003
 */
public interface OptionProvider {
  
  public Map getOptions(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc);

}
