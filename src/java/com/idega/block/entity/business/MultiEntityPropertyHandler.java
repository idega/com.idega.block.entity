package com.idega.block.entity.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.idega.block.entity.data.EntityPath;
import com.idega.idegaweb.IWUserContext;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class MultiEntityPropertyHandler {
  
  private IWUserContext userContext = null;
  
  private Class leadingEntityClass = null;
  
  private HashMap entityClassesHandler = new HashMap();
  
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
    entityClassesHandler.remove(entityClass);
  }
 
  public SortedMap getAllEntityPathes() {
    // use cached value if possible
    if (allEntityPathes != null)
      return allEntityPathes;

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
    return allEntityPathes;
     
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


  public int getNumberOfRowsPerPage() {
    return getLeadingEntityPropertyHandler().getNumberOfRowsPerPage();
  }
  
  public void setNumberOfRowsPerPage(int numberOfRowsPerPage) {
    getLeadingEntityPropertyHandler().setNumberOfRowsPerPage(numberOfRowsPerPage);  
  }
  
  public List getVisibleOrderedEntityPathes() {
    return getLeadingEntityPropertyHandler().getVisibleOrderedEntityPathes();
  }  

  public void setVisibleOrderedEntityPathes(List entityPathes)  {
    getLeadingEntityPropertyHandler().setVisibleOrderedEntityPathes(entityPathes);    
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
    



