package com.idega.block.entity.data;

import java.util.HashMap;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 *
 *
 *
 *
 */
public class EntityPropertyDefaultValues {
  
  
  static HashMap dictionary;
  
  static { 
    dictionary = new HashMap();
    
    /*
    String next = EntityPath.SHORT_KEY_NEXT_ENTITY_PATH_DELIMITER; 
    String endOfName = EntityPath.SHORT_KEY_ENTITY_NAME_COLUMN_NAME_DELIMITER;
    String c = EntityPath.SHORT_KEY_COLUMN_NAME_DELIMITER;
    */
   
    String[] user = new String[] {};
                    
    dictionary.put("com.idega.user.data.User", user);                
  }
  
  public static String[] getVirtualShortKeys(String entityName) {
    return (String[]) dictionary.get(entityName);
  }
 
}
