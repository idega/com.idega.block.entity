package com.idega.block.entity.data;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: 
 * This class serves as a container for a value of an entity. 
 * The entity and the value is defined by using the shortkey of an entity path
 * and holding the identifier.
 * (e.g. shortkey: "com.idega.user.data.User.FIRST_NAME")
 * This class is used for handling form data.
 * </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: idega Software</p>
 * @author <a href="thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Jul 4, 2003
 */
public class EntityPathValueContainer {

  private String entityPathShortKey;
  // this flag indicates if the value was set or not:
  // keep in mind that a valid value could be null, an empty string or something else, that is why 
  // this flag is needed.
  private boolean valueIsSet = false;
  private Object value;
  private Object previousValue = null;
  private Object entityId;
  
  public EntityPathValueContainer() {
    this(null);
  }
  
  public EntityPathValueContainer(String entityPathShortKey)  {
    this(entityPathShortKey, null);
  }
  
  public EntityPathValueContainer(String entityPathShortKey, Object entityId)  {
    this.entityPathShortKey = entityPathShortKey;
    this.entityId = entityId;
    this.valueIsSet = false;
  }

  public EntityPathValueContainer(String entityPathShortKey, Object entityId, Object value)  {
    this(entityPathShortKey, entityId, value, null);  
  }
  
  public EntityPathValueContainer(String entityPathShortKey, Object entityId, Object value, Object previousValue)  {
    this.entityPathShortKey = entityPathShortKey;
    this.entityId = entityId;
    this.value = value;
    this.previousValue = previousValue;
    this.valueIsSet = true;
  }
  
  /**
   * Previous value needn't to be set.
   */
  public boolean isValid()  {
    if (! this.valueIsSet) {
      return false;
    }
    if (this.entityPathShortKey == null || this.entityPathShortKey.length() == 0) {
      return false;
    }
    // if the entity is new, the id should be set to -1
    if (this.entityId == null)  {
      return false;
    }
    return true;
  }
  
  /**
   * @return
   */
  public Object getEntityId() {
    return this.entityId;
  }
  
  public Integer getEntityIdConvertToInteger()	throws NumberFormatException {
  	String id = this.entityId.toString();
 		return new Integer(id);
  }  		

  /**
   * @return
   */
  public String getEntityPathShortKey() {
    return this.entityPathShortKey;
  }

  /**
   * @return
   */
  public Object getValue() {
    return this.value;
  }

	public void setEntityId(Object entityId)	{
		this.entityId = entityId;
	} 


  /**
   * @param integer
   */
  public void setEntityId(Integer integer) {
    this.entityId = integer;
  }
  
  public void setEntityId(String integerAsString) {
    try {
      this.entityId = new Integer(integerAsString);
    }
    catch (NumberFormatException ex)  {
      this.entityId = null;
    }
  }

  /**
   * @param string
   */
  public void setEntityPathShortKey(String string) {
    this.entityPathShortKey = string;
  }

  /**
   * @param object
   */
  public void setValue(Object object) {
    this.valueIsSet = true;
    this.value = object;
  }

  /**
   * @return
   */
  public Object getPreviousValue() {
    return this.previousValue;
  }

  /**
   * @param object
   */
  public void setPreviousValue(Object object) {
    this.previousValue = object;
  }

  /**
   * @param b
   */
  public void setValueIsSet(boolean b) {
    this.valueIsSet = b;
  }

}
