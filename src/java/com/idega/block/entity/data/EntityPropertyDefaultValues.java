package com.idega.block.entity.data;

import java.util.HashMap;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class EntityPropertyDefaultValues {
  
  
  static HashMap dictionary;
  
  static { 
    dictionary = new HashMap();
    
    String d = EntityPath.NEXT_SHORT_KEY_DELIMITER; 
   
    String[] user = new String[] {
      "com.idega.user.data.User.MIDDLE_NAME" + d + "com.idega.user.data.User.LAST_NAME" };
                    
    dictionary.put("com.idega.user.data.User", user);                
  }
        
    

  
  public static String[] getVirtualShortKeys(String entityName) {
    return (String[]) dictionary.get(entityName);
  }
    

    
 
}
