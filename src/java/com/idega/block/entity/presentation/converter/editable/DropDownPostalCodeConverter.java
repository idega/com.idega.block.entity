package com.idega.block.entity.presentation.converter.editable;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.ejb.FinderException;

import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.block.entity.presentation.converter.ConverterConstants;
import com.idega.business.IBOLookup;
import com.idega.core.location.business.AddressBusiness;
import com.idega.core.location.data.Country;
import com.idega.core.location.data.PostalCode;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: 
 * Special drop down menu converter for postal codes.
 * This class is used without setting an option provider, just set a country or a country name to 
 * define the list of postal codes that is shown.
 * Note: Calling setOptionProvider(OptionProvider) does nothing.
 * </p>
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
  
  private Map postalCodeNumberIdMap;
  private SortedMap idPostalAddressMap;
  
  public DropDownPostalCodeConverter(Form externalForm) {
    super(externalForm);
  }
  
  private void initializePostalCodeMaps(IWContext iwc) {
    if (this.idPostalAddressMap == null || this.countryHasChanged) {
      this.idPostalAddressMap = new TreeMap();
      this.postalCodeNumberIdMap = new HashMap();
      // add null value
      this.postalCodeNumberIdMap.put("", ConverterConstants.NULL_ENTITY_ID);
      this.idPostalAddressMap.put(ConverterConstants.NULL_ENTITY_ID, "");
      try {
        if( this.countryName!=null && this.country == null) {
          this.country = getAddressBusiness(iwc).getCountryHome().findByCountryName(this.countryName);      
        }
        if( this.country!=null ){
          Collection postals = getAddressBusiness(iwc).getPostalCodeHome().findAllByCountryIdOrderedByPostalCode(((Integer)this.country.getPrimaryKey()).intValue());
          Iterator iter = postals.iterator();
          while (iter.hasNext()) {
            PostalCode element = (PostalCode) iter.next();
            Integer id = (Integer) element.getPrimaryKey();
            String code = element.getPostalAddress();
            String postalCodeNumber = element.getPostalCode();
            if( code!=null && postalCodeNumber != null) {
              this.idPostalAddressMap.put(id,code);
              this.postalCodeNumberIdMap.put(postalCodeNumber, id);
            }           
          }
        }
        this.countryHasChanged = false;
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
  }
        
      
  private AddressBusiness getAddressBusiness(IWApplicationContext iwc) throws RemoteException {
   return (AddressBusiness) IBOLookup.getServiceInstance(iwc,AddressBusiness.class);
  }
      
  /** overwrites super method */
  protected DropdownMenu getDropdownMenu(
      Object preselection, 
      String name,
      Object entity,
      EntityPath path,
      EntityBrowser browser,
      IWContext iwc)  {
    initializePostalCodeMaps(iwc);
    DropdownMenu dropdownMenu = new DropdownMenu(name);
    Iterator iterator = this.idPostalAddressMap.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry option = (Map.Entry) iterator.next();
      String value = option.getKey().toString();
      String display = option.getValue().toString();
      dropdownMenu.addMenuElement(value, display);
    }
    // set preselection
    if (preselection != null) {
      // The preselection is the postal code number, but the value used in the drop down menu is 
      // the id of the postal code. Therefore get the corresponding id of the postal code element first and
      // then set the preselection 
      //String preselectionAsString = preselection.toString();
      Integer id = (Integer) this.postalCodeNumberIdMap.get(preselection);
      if (id != null) {
        dropdownMenu.setSelectedElement(id.toString());
      }
    }
    else {
      dropdownMenu.setSelectedElement("");
    }
    return dropdownMenu;
  }
  
  /** Overwritten method does nothing */
  public void setOptionProvider(OptionProvider optionProvider)  {
    // do nothing
  }
  
  /** Overwritten method */
  protected Object getValueForLink(
      Object entity,
      EntityPath path,
      EntityBrowser browser,
      IWContext iwc)  {
    Object value = getValue(entity, path, browser, iwc);
    if (value != null)  {
      String display = value.toString();
      if (display.length() > 0) {
        return display;
      }
    }
    return "_";
  }

  
  public void setCountry(Country country) {
    if (this.country == null || (! this.country.equals(country)))  {
      this.country = country;
//      try {
        this.countryName = country.getName();
/*      }
      catch (RemoteException ex) {
        System.err.println(
          "[DropDownPostalCodeConverter]: Can't retrieve name of country. Message is: "
            + ex.getMessage());
        ex.printStackTrace(System.err);
        throw new RuntimeException("[DropDownPostalCodeConverter]: Can't retrieve name of country.");
      }
*/      this.countryHasChanged = true;
    }
    
  }
  
  public void setCountry(String countryName)  {
    if (this.countryName == null || (! this.countryName.equals(countryName))) {
      this.countryName = countryName;
      this.country = null;
      this.countryHasChanged = true;
    }
  }
}
  
