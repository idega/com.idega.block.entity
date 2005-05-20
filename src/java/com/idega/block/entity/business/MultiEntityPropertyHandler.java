package com.idega.block.entity.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.idega.block.entity.data.EntityPath;
import com.idega.idegaweb.IWUserContext;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class MultiEntityPropertyHandler {
  
  private IWUserContext userContext = null;
  
  private Class leadingEntityClass = null;
  
  // needs to be synchronized, do not use Hashtable since null values are used 
  private Map entityClassesHandler = Collections.synchronizedMap(new HashMap());
  
  private SortedMap allEntityPathes = null;
  
  public MultiEntityPropertyHandler(IWUserContext userContext, Class leadingEntityClass) {
    this.userContext = userContext;
    this.leadingEntityClass = leadingEntityClass;
    // add the leading entity class also to the map
    this.addEntity(leadingEntityClass);
  }
 
  public MultiEntityPropertyHandler(IWUserContext userContext, String leadingEntityClassName) throws ClassNotFoundException {
    this(userContext, Class.forName(leadingEntityClassName));
  }
  
  /**
   * This contructor is used if defining a leading entity is impossible.
   * All returned entity pathes are DummyEntityPathes.
   */
	public MultiEntityPropertyHandler(IWUserContext userContext) {	
		this.userContext = userContext;
	}
 
  public void addEntity(String entityClassName) throws ClassNotFoundException {
    Class entityClass = Class.forName(entityClassName);
    addEntity(entityClass); 
  }
 
 
  public void addEntity(Class entityClass) {
    if (entityClassesHandler.containsKey(entityClass))
      return;
    // allEntityPathes is not longer valid
    allEntityPathes = null;
    // set key (value is calculated later if necessary)
    entityClassesHandler.put(entityClass, null);
  }
 
  public void removeEntityClass(Class entityClass)  {
    // it is not allowed to remove the leading class
    if (entityClass == leadingEntityClass)
      return;
    // it does not matter if there was no such entry
    // allEntityPathes is still valid! (it knows more than it needs to know)
    allEntityPathes = null;
    entityClassesHandler.remove(entityClass);
  }
 
  public SortedMap getAllEntityPathes() {
    // use cached value if possible
    if (allEntityPathes != null)
      return allEntityPathes;
    // try do get the value from session
    SortedMap sortedMap = getCachedEntityPathesFromSession();
    if (sortedMap != null)  {
      allEntityPathes = sortedMap;
      return allEntityPathes;
    }
    // okay...then calculate again...
    allEntityPathes = new TreeMap(); 
    Set entries = entityClassesHandler.entrySet();
    Iterator iterator = entries.iterator();
    while (iterator.hasNext())  {
      Map.Entry entry = (Map.Entry) iterator.next();
      EntityPropertyHandler handler = (EntityPropertyHandler) entry.getValue();
      if (handler == null)  {
        handler = new EntityPropertyHandler(userContext, (Class) entry.getKey());
        entry.setValue(handler);        
      }
      allEntityPathes.putAll(handler.getAllEntityPathes());
    }
    // store in session
    setCachedEntityPathesIntoSession();
    return allEntityPathes;
     
  }
  
  private String getKeyForCachedEntityPathes() {
    Set set = entityClassesHandler.keySet();
    // we have to sort entries in order to get a unique key
    SortedSet sortedSet = new TreeSet();
    Iterator iterator = set.iterator();
    // get unique name
    while (iterator.hasNext())  {
      String className = ((Class) iterator.next()).getName();
      sortedSet.add(className);
    }
    StringBuffer buffer = new StringBuffer();
    iterator = sortedSet.iterator();
    while (iterator.hasNext())  {
      buffer.append((String) iterator.next());
      buffer.append(":");
    }
    return buffer.toString();
  }
  
  private SortedMap getCachedEntityPathesFromSession() {
    String key = getKeyForCachedEntityPathes();
    return (SortedMap) userContext.getSessionAttribute(key);
  }
    
  private void setCachedEntityPathesIntoSession() {
    String key = getKeyForCachedEntityPathes();
    userContext.setSessionAttribute(key, allEntityPathes);
  }
  
  
  public String getLeadingEntityClassName() {
    return leadingEntityClass.getName();
  }
  
  public Collection getEntityNames()  {
    Set keys = entityClassesHandler.keySet();
    Collection coll = new ArrayList();
    Iterator iterator = keys.iterator();
    while (iterator.hasNext())  {
      Class entityClass = (Class) iterator.next();
      coll.add(entityClass.getName());
    }
    return coll;
    
  }

  public EntityPath getEntityPath(String shortKey)  {
    Map map = getAllEntityPathes();
    return EntityPropertyHandler.getEntityPath(map, shortKey);
  }


  public int getNumberOfRowsPerPage(String identificationName) {
    return getLeadingEntityPropertyHandler().getNumberOfRowsPerPage(identificationName);
  }
  
  public void setNumberOfRowsPerPage(int numberOfRowsPerPage, String identificationName) {
    getLeadingEntityPropertyHandler().setNumberOfRowsPerPage(numberOfRowsPerPage, identificationName);  
  }
  
  public List getVisibleOrderedEntityPathes(String identificationName) {
    return getLeadingEntityPropertyHandler().getVisibleOrderedEntityPathes(identificationName);
  }  

  public void setVisibleOrderedEntityPathes(List entityPathes, String identificationName)  {
    getLeadingEntityPropertyHandler().setVisibleOrderedEntityPathes(entityPathes, identificationName);    
  }

  private EntityPropertyHandler getLeadingEntityPropertyHandler()  {
    EntityPropertyHandler leadingHandler = (EntityPropertyHandler) 
      entityClassesHandler.get(leadingEntityClass);
    if (leadingHandler == null) {
      leadingHandler = new EntityPropertyHandler(userContext, leadingEntityClass);
      entityClassesHandler.put(leadingEntityClass, leadingHandler);
    }
    return leadingHandler;
  }
      
}      
    



