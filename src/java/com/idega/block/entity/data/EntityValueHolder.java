package com.idega.block.entity.data;

import java.util.HashMap;
import java.util.Map;

import com.idega.data.EntityRepresentation;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: idega Software</p>
 * @author <a href="thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Jul 15, 2003
 */
public class EntityValueHolder implements EntityRepresentation {
  
  private static final String DEFAULT_VALUE = "";
  
  private Map columnValueMap = null; 
  
  private Integer primaryKey = new Integer(-1);

  /* (non-Javadoc)
   * @see com.idega.data.EntityRepresentation#getColumnValue(java.lang.String)
   */
  public Object getColumnValue(String columnName) {
    Object value = (this.columnValueMap == null) ? null : this.columnValueMap.get(columnName); 
    return (value == null) ? DEFAULT_VALUE : value;
  }
  
  public void setColumnValue(String columnName, Object value) {
    if (this.columnValueMap == null)   {
      this.columnValueMap = new HashMap();
    }
    this.columnValueMap.put(columnName, value);
  }
    
  
  public Object getPrimaryKey() {
    return this.primaryKey;
  }

}
