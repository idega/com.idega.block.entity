package com.idega.block.entity.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import com.idega.data.EntityAttribute;
import com.idega.data.EntityRepresentation;
import com.idega.data.GenericEntity;
import com.idega.data.IDOEntity;
import com.idega.idegaweb.IWResourceBundle;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class EntityPath {
  
  public final static String SERIALIZATION_DELIMITER = "@";
  
  public final static String SERIALIZATION_NEXT_ENTITY_PATH_DELIMITER = "#";
  
  public final static String SHORT_KEY_NEXT_ENTITY_PATH_DELIMITER = ":";
  
  public final static String SHORT_KEY_ENTITY_NAME_COLUMN_NAME_DELIMITER = ".";
  
  public final static String SHORT_KEY_COLUMN_NAME_DELIMITER = "|";
  
  public final static String DESCRIPTION_NEXT_ENTITY_PATH_DELIMITER = "+";
  
  public final static String DESCRIPTION_COLUMN_DELIMITER = ".";
  
  private static int DEFAULT_SEARCH_DEPTH = 2; 
  
  private Class sourceEntity = null;
  
  private Class targetEntity = null;
  
  private Class classOfValue = null;
  
  private ArrayList pathToEntity = new ArrayList();
  
  private EntityPath nextEntityPath = null;
  
  public EntityPath(Class sourceEntity) {
    this.sourceEntity = sourceEntity;
  }
  
  
  public static SortedMap getInstances(Class sourceEntity) {
    return getInstances(DEFAULT_SEARCH_DEPTH, sourceEntity);
  }
  
  public static SortedMap getInstances(int maximumSearchDepth, Class sourceEntity)  {
    return getAllEntityPathes( new EntityPath(sourceEntity) , sourceEntity, 0 , maximumSearchDepth);
  }
  
  
  public static EntityPath getInstance(String serialization) throws ClassNotFoundException  {
    EntityPath lastEntityPath = null;
    EntityPath firstEntityPath = null;
    StringTokenizer entityPathTokenizer = new StringTokenizer(serialization, SERIALIZATION_NEXT_ENTITY_PATH_DELIMITER);
    while (entityPathTokenizer.hasMoreElements()) {
      // build single entity path
      String singleEntityPathSerialization = entityPathTokenizer.nextToken();
      StringTokenizer tokenizer = new StringTokenizer(singleEntityPathSerialization, SERIALIZATION_DELIMITER);
      List columnNames = new ArrayList();
      while (tokenizer.hasMoreElements()) {
        String columnName = tokenizer.nextToken();
        columnNames.add(columnName);
      }
      int size;
      if ((size = columnNames.size()) < 2)
        throw new ClassNotFoundException("EntityPath could not be created from string: " + serialization); 
      String sourceName = (String) columnNames.get(0);
      Class sourceEntity = Class.forName(sourceName);
      // remove target name 
      columnNames.remove(size-1);
      EntityPath createdEntityPath;
      if (size == 2)
        createdEntityPath = new EntityPath(sourceEntity);
      else
        createdEntityPath = getEntityPath(new EntityPath(sourceEntity) , sourceEntity, 0 , columnNames);
      if (lastEntityPath == null) 
        // remember the first entity path
        firstEntityPath = createdEntityPath;
      else
        // add the child 
        lastEntityPath.add(createdEntityPath);
      lastEntityPath = createdEntityPath;  
    }
    return firstEntityPath;
  }
  
  public Class getSourceEntityClass() {
    return sourceEntity;
  }
  
  public void add(String aColumnName) {
    pathToEntity.add(aColumnName);  
  }
  
  public void add(EntityPath aPath) {
    if (nextEntityPath == null) 
      nextEntityPath = aPath;
    else
      nextEntityPath.add(aPath);
  }
  
  /** @return my next entityPath or null */ 
  public EntityPath getNextEntityPath() {
    return nextEntityPath;
  }
    
  
  
  private void setTargetEntity(Class targetEntityClass)  {
    this.targetEntity = targetEntityClass;
  }
  
  private void setClassOfValue(Class classOfValue)  {
    this.classOfValue = classOfValue;
  }
  
  
  public String getSerialization()  {
    StringBuffer serialization = new StringBuffer();
    getSerialization(serialization);
    return serialization.toString();
  }
    
  
  
  private void getSerialization(StringBuffer stringBuffer) {
    StringBuffer serialization = new StringBuffer();
      serialization
        .append(sourceEntity.getName())
        .append(SERIALIZATION_DELIMITER);
      
    Iterator iterator = pathToEntity.iterator();
    while (iterator.hasNext())  {
      serialization
        .append((String) iterator.next())  
        .append(SERIALIZATION_DELIMITER);
    }
      
    serialization.append(targetEntity.getName());
    stringBuffer.append(serialization);
    if (nextEntityPath == null)
      return;
    // put a delimiter between the two objects  
    stringBuffer.append(SERIALIZATION_NEXT_ENTITY_PATH_DELIMITER);
    nextEntityPath.getSerialization(stringBuffer);
  }
  
  
  
  /** Gets a shortkey that is used as key in the hash map 
   * that is returned by the method getInstances
   * @return shortKey e.g. "com.idega.user.data.User.MIDDLE_NAME"
   * or e.g. "com.idega.user.data.User.FIRST_NAME:com.idega.user.data.User.Last_NAME"
   * (if this path consists of two pathes)
   */  
  public String getShortKey() {
    StringBuffer buffer = new StringBuffer();
    getShortKey(buffer, true);
    return buffer.toString();
  }
    
  /** Gets only the section of the short key of this instance */
  public String getShortKeySection() {
  	StringBuffer buffer = new StringBuffer();
  	getShortKey(buffer, false);
  	return buffer.toString();
  }
  
  private void getShortKey(StringBuffer buffer, boolean getCompletePath) {
    int lastIndex =  pathToEntity.size() - 1;
    if (lastIndex < 0)
      return;
    // add name of the target entity
    buffer
      .append(targetEntity.getName())
      .append(SHORT_KEY_ENTITY_NAME_COLUMN_NAME_DELIMITER);
    // add all column names of this entity path
    Iterator iterator = pathToEntity.iterator();
    boolean notTheFirstTime = false;
    while (iterator.hasNext())  {
      if (notTheFirstTime) 
        // append this delimiter not the very first time
        buffer.append(SHORT_KEY_COLUMN_NAME_DELIMITER);
      else
        notTheFirstTime = true;  
      String columnName = (String) iterator.next();
      buffer.append(columnName); 
    }
    // go to the next entity path
    if (nextEntityPath != null && getCompletePath) {
      buffer.append(SHORT_KEY_NEXT_ENTITY_PATH_DELIMITER);
      nextEntityPath.getShortKey(buffer, true);
    }
  }


  
  /** Gets the original description, do get a localized description use
   *  getLocalizedDescription.   
   *  @return a description of the value that is representated by this instance
   */
  public String getDescription()  {
    StringBuffer buffer = new StringBuffer();
    getDescription(buffer);
    return buffer.toString();
  }    
  
  
  
  private void getDescription(StringBuffer buffer)  {
  	if (targetEntity == null) {
  		return;
  	}
    // add target name (without package path)
    String targetClassName = targetEntity.getName();
    String classNameWithoutPath = targetClassName.substring(targetClassName.lastIndexOf(".") + 1);
    buffer.append(classNameWithoutPath);
    // buffer.append('.').append(pathToEntity.get(pathToEntity.size()-1));
    // add all column names of this entity path
    Iterator iterator = pathToEntity.iterator();
    while (iterator.hasNext())  {
      buffer.append(DESCRIPTION_COLUMN_DELIMITER);
      String columnName = (String) iterator.next();
      buffer.append(columnName); 
    }
    // add describtion of the next entity path
    if (nextEntityPath != null) {
      buffer.append(DESCRIPTION_NEXT_ENTITY_PATH_DELIMITER);
      nextEntityPath.getDescription(buffer);
    }
  }    
  
  /** Gets the localized description.
   * @param resourceBundle the resourceBundle that serves as a source for the localization
   * @return String a localized description of the value that is representated by this
   * instance
   */
  public String getLocalizedDescription(IWResourceBundle resourceBundle)  {
    String description = getDescription();
    return resourceBundle.getLocalizedString( description, description);
  }

  /** Returns an object or null.
   * This is the value of this instance without considering the next entity pathes.
   * Do not use this method in convertes, use rather getValues(EntityRepresentation).
   * @return the value of this entity path without the next entity path
   */
  public Object getValue(EntityRepresentation entity)  {
    Iterator iterator = pathToEntity.iterator();
    // start 
    Object value = entity; 
    // sometimes you can not go down the complete path because some columns are null
    while (iterator.hasNext() && value != null )  {
      String currentColumnName = (String) iterator.next();
      // that is a little bit tricky: the column can be 
      // a reference to another entity or a real value like a String, Integer, Date and so on.
      // If the path is well-defined, there will be no CastException, because the very last value 
      // is not casted to EntityRepresentation.
      value = ((EntityRepresentation) value).getColumnValue(currentColumnName);    
    }
    return value;
  }
  
  /** @return values of this entity path plus all the values of the next
   * entity pathes
   */
  public List getValues(EntityRepresentation entity)  {
    List list = new ArrayList();
    getValues(list, entity);
    return list;
  }
  
  /** Returns the class of the value or if not detectable null (e.g. if this is an artificial path).
   * This is the class of the value of this instance without considering the next entity pathes.
   * Do not use this method in convertes, use rather getClassesOfValues(EntityRepresentation).
   * @return the class of the value or null if the class is not detectable
   */ 
  public Class getClassOfValue()  {
    return classOfValue;
  }
  
  public List getClassesOfValues()  {
    List list = new ArrayList();
    getClassesOfValues(list);
    return list;    
  }
  
  public Object clone() {
    EntityPath entityPath = new EntityPath(this.sourceEntity);
    entityPath.setTargetEntity(this.targetEntity);
    entityPath.setClassOfValue(this.classOfValue);
    Iterator iterator = pathToEntity.iterator();
    while (iterator.hasNext()) 
      entityPath.add((String) iterator.next());
    if (nextEntityPath != null) {
      EntityPath nextClone = (EntityPath) nextEntityPath.clone();
      entityPath.add(nextClone);
    }
        
    return entityPath;
  } 
  
  private void getClassesOfValues(List list) {
    list.add(getClassOfValue());
    if (nextEntityPath == null) {
      return;
    }
    nextEntityPath.getClassesOfValues(list);
  }

  private void getValues(List list, EntityRepresentation entity)  {
    list.add(getValue(entity));
    if (nextEntityPath == null) {
      return;
    }
    nextEntityPath.getValues(list, entity);
  }


   
  private static SortedMap getAllEntityPathes
    ( EntityPath motherEntityPath, 
      Class currentEntityClass,  
      int currentLayer, 
      int maximumSearchDepth) {       
    // entering a new layer....
    currentLayer++;
    //TODO thomas: change this by using IDOEntity and EntityDefinition classes       
    // entering a new layer....
		GenericEntity entity = (GenericEntity) getEntity(currentEntityClass);
    if (entity == null)
      return new TreeMap();
    Collection coll = entity.getAttributes();
    Iterator iterator = coll.iterator();
    TreeMap pathes = new TreeMap();
    while (iterator.hasNext())  {
      EntityAttribute attribute = (EntityAttribute) iterator.next();
      Class anEntityClass = attribute.getRelationShipClass();
      EntityPath currentPath = (EntityPath) motherEntityPath.clone();
      currentPath.add(attribute.getColumnName());
      if (anEntityClass == null) {
        // finished!
        currentPath.setClassOfValue(attribute.getStorageClass());
		currentPath.setTargetEntity(currentEntityClass);
        // use shortKey as key for the returned HashMap
        pathes.put(currentPath.getShortKey(), currentPath);
      }
      else if (currentLayer < maximumSearchDepth) {
        // go further!
        SortedMap PathesOfChild = EntityPath.getAllEntityPathes(currentPath, anEntityClass, currentLayer, maximumSearchDepth);
        pathes.putAll(PathesOfChild);
      }  
    }
    return pathes;
  }

  
  private static EntityPath getEntityPath
    ( EntityPath motherEntityPath, 
      Class currentEntityClass, 
      int currentLayer, 
      List columnNames) {    
    //TODO thomas: change this by using IDOEntity and EntityDefinition classes       
    // entering a new layer....
    currentLayer++;
    GenericEntity entity = (GenericEntity) getEntity(currentEntityClass);
    if (entity == null)
      return null;
    EntityAttribute attribute = entity.getAttribute((String) columnNames.get(currentLayer));
    Class anEntityClass = attribute.getRelationShipClass();
    motherEntityPath.add(attribute.getColumnName());
    if (anEntityClass == null) {
      // finished!
      motherEntityPath.setClassOfValue(attribute.getStorageClass());
      motherEntityPath.setTargetEntity(currentEntityClass);
    }
    else if (currentLayer < columnNames.size()) {
        // go further!
      motherEntityPath = EntityPath.getEntityPath(motherEntityPath, anEntityClass, currentLayer, columnNames);
    }  
    return motherEntityPath;
  }  


  private static IDOEntity getEntity(Class currentEntityClass) {
    return GenericEntity.getStaticInstanceIDO(currentEntityClass);
	}  

}
