package com.idega.block.entity.presentation;


import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.ejb.CreateException;

import com.idega.core.data.Country;
import com.idega.data.IDOHome;
import com.idega.data.IDOLegacyEntity;
import com.idega.data.IDOLookup;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;


/**
 * 
 * Only for testing.
 * 
 * 
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class AllCountries extends Block {
  
  
  private final static String IW_BUNDLE_IDENTIFIER="com.idega.block.entity";
  
  public String getBundleIdentifier(){
    return this.IW_BUNDLE_IDENTIFIER;
  }
  
  public void main(IWContext iwc) throws Exception {  
    Collection coll = getAll(Country.class);
    EntityBrowser browser = new EntityBrowser();
    browser.setLeadingEntity((Country.class).getName());
    browser.setEntities("contries", coll);
    add(browser);
  }
     
  public List getAll(Class entityClass)  {
    IDOHome home;
    IDOLegacyEntity idoEntity;
    try {
      home = IDOLookup.getHome(entityClass);
    }
    catch (RemoteException e) {
      return null;
    }
    try {
      idoEntity = (IDOLegacyEntity) home.createIDO();
    }
    catch (CreateException e) {
      System.err.println("Create did not work."+ e.getMessage());
      e.printStackTrace(System.err);
      return null;      
    }
    IDOLegacyEntity[] entities = null;
		try {
			entities = idoEntity.findAll();
		}
		catch (SQLException e) {
		}
    return new ArrayList(Arrays.asList(entities));
  }       
     
      
  
}
