package com.idega.block.entity.presentation.converters;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.FinderException;

import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.business.IBOLookup;
import com.idega.core.business.AddressBusiness;
import com.idega.core.data.Country;
import com.idega.core.data.PostalCode;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.ui.Form;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: idega Software</p>
 * @author <a href="thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Jul 25, 2003
 */
public class DropDownPostalCodeConverter extends DropDownMenuConverter {
  
  private Country country = null;
  private String countryName = null;
  private boolean countryHasChanged = false;
  
  public DropDownPostalCodeConverter(Form externalForm) {
    super(externalForm);
    initialize();
  }
  
  private void initialize() {
    // define an option provider
    
    OptionProvider optionProvider = new OptionProvider() {
      
      Map optionMap = null;
      
      public Map getOptions(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
        if (optionMap == null || countryHasChanged) {
          optionMap = new HashMap();
          try {
            if( countryName!=null && country == null) {
              country = getAddressBusiness(iwc).getCountryHome().findByCountryName(countryName);      
            }
      
            if( country!=null ){
              Collection postals = getAddressBusiness(iwc).getPostalCodeHome().findAllByCountryIdOrderedByPostalCode(((Integer)country.getPrimaryKey()).intValue());
              Iterator iter = postals.iterator();
              while (iter.hasNext()) {
                PostalCode element = (PostalCode) iter.next();
                int id = ((Integer)element.getPrimaryKey()).intValue();
                String code = element.getPostalAddress();
                if( code!=null ) {
                  optionMap.put(new Integer(id),code);
                }           
              }
            }
            countryHasChanged = false;
          }
          catch (RemoteException ex) {
            System.err.println(
              "[DropDownPostalCodeConverter]: Can't retrieve AddressBusiness. Message is: "
                + ex.getMessage());
            ex.printStackTrace(System.err);
            throw new RuntimeException("[DropDownPostalCodeConverter]: Can't retrieve AddressBusiness.");
          }
          catch (FinderException ex)  { 
            System.err.println(
              "[DropDownPostalCodeConverter]: Can't find postalcodes or country. Message is: "
                + ex.getMessage());
            ex.printStackTrace(System.err);
          }
        }
        return optionMap;
      }
        
      
      private AddressBusiness getAddressBusiness(IWApplicationContext iwc) throws RemoteException {
        return (AddressBusiness) IBOLookup.getServiceInstance(iwc,AddressBusiness.class);
      }
      
    }; 
    
    // set this option provider
    setOptionProvider(optionProvider);    
  }
  
  public void setCountry(Country country) {
    if (this.country == null || (! this.country.equals(country)))  {
      this.country = country;
      try {
        this.countryName = country.getName();
      }
      catch (RemoteException ex) {
        System.err.println(
          "[DropDownPostalCodeConverter]: Can't retrieve name of country. Message is: "
            + ex.getMessage());
        ex.printStackTrace(System.err);
        throw new RuntimeException("[DropDownPostalCodeConverter]: Can't retrieve name of country.");
      }
      countryHasChanged = true;
    }
    
  }
  
  public void setCountry(String countryName)  {
    if (this.countryName == null || (! this.countryName.equals(countryName))) {
      this.countryName = countryName;
      this.country = null;
      countryHasChanged = true;
    }
  }
}
  
