package com.idega.block.entity.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap; 



import com.idega.block.entity.business.EntityPropertyHandler;
import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.business.MultiEntityPropertyHandler;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.event.EntityBrowserEvent;
import com.idega.block.entity.event.EntityBrowserPS;
import com.idega.builder.business.IBPropertyHandler;
import com.idega.builder.handler.SpecifiedChoiceProvider;
import com.idega.business.IBOLookup;
import com.idega.data.GenericEntity;
import com.idega.event.IWActionListener;

import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationState;
import com.idega.event.IWStateMachine;

import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.app.UserApplication;
import com.idega.util.SetIterator;
/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */

public class EntityBrowser extends Table implements SpecifiedChoiceProvider, StatefullPresentation {
  
  public final static String IW_BUNDLE_IDENTIFIER = "com.idega.block.entity";

  public final static String NEW_SUBSET_KEY = "new_subset_key";
  
  private final static String NEXT_SUBSET_KEY = "next_subset_key";
  
  private final static String NEXT_SUBSET = "next";
  
  private final static String PREVIOUS_SUBSET = "previous";
  
  private final static String NEXT_SUBSET_ACTION = "next_subset";
  
  private final static String PREVIOUS_SUBSET_ACTION = "previous_subset";
  
  private final static String VIEW_ACTION = "view_action";
  
  private static final String SET_ENTIY_METHOD_IDENTIFIER = 
    ":method:1:implied:void:setLeadingEntity:java.lang.String:";
    
  private static final String SET_DEFAULT_COLUMNS_METHOD_IDENTIFIER = 
    ":method:1:implied:void:setDefaultColumns:int:java.lang.String:";  

  private String leadingEntityName = null;
  
  // foreign entities
  private List entityNames = null;
  
  private int xAnchorPosition = 0;
  
  private int yAnchorPosition = 0;

  // collection of entities that serves as source of the content  
  private Collection entities = null;
  
  private IWPresentationState presentationState = null;
  
  // map of converters
  private HashMap entityToPresentationConverters = null;

  // the default converter just shows a text  
  private EntityToPresentationObjectConverter defaultConverter = null;
  
  // use tree map because of the order of the elements
  private TreeMap defaultColumns = new TreeMap();
  
  private TreeMap mandatoryColumns = new TreeMap();
  
  private int defaultNumberOfRowsPerPage = 1;
  
  private boolean useExternalForm = false;
  
  private String colorForEvenRows = null;
  
  private String colorForOddRows = null;
  
  
  public void setDefaultNumberOfRows(int defaultNumberOfRows) {
    this.defaultNumberOfRowsPerPage = defaultNumberOfRows;
  }
  
 
  /** if you change the name of this method please change the 
   *  corresponding method identifier variable SET_ENTIY_METHOD_IDENTIFIER */
  public void setLeadingEntity(String leadingEntityName)  {
    this.leadingEntityName = leadingEntityName;
  }
  
  /**
   * Usually this class uses its own form to handle the forward and the backward
   * buttons. In this case this flag is set to false.
   * 
   * But if you want - for example - to add buttons to this table instance you
   * just can switch off the inherent form by setting this value to true and put
   * the whole object into an external form. Nothing else is to do, the forward
   * and backward buttons will further work.
   * 
   * @param useExternalForm
   */
  public void setUseExternalForm(boolean useExternalForm) {
    this.useExternalForm = useExternalForm;
  }
  
  public void addEntity(String entityName)  {
    if (entityNames == null)
      entityNames = new ArrayList();
    if (entityNames.contains(entityName))
      return;
    entityNames.add(entityName);
  }
  
  public void removeEntity(String entityName) {
    if (entityNames == null)
      return;
    // it does not matter if it does not exist
    entityNames.remove(entityName);
  }
  
  /** this method should only be used by the IWPropertyHandler 
   *  It provides the drop down menu in the property handler 
   *  that is used to set the default columns with a collection 
   *  of appropriate columnnames.
   *  To do this this method fetches the current value of the 
   *  entity name property from the specified property handler.
  
   */  
  public Collection getSpecifiedChoice(
      IWContext iwc, 
      String ICObjectInstanceID, 
      String methodIdentifier, 
      IBPropertyHandler propertyHandler)  {
      Class entityClass;
      String anEntityClassName;
    // is the method correct?
    if (SET_DEFAULT_COLUMNS_METHOD_IDENTIFIER.equals(methodIdentifier)) {
      // get the set property value from the handler
      anEntityClassName = propertyHandler.getPropertyValue(iwc, ICObjectInstanceID, SET_ENTIY_METHOD_IDENTIFIER);
    }
    else
      // the specified method is not handled here
      return new ArrayList();
    // create the desired collection   
    try {
      entityClass = Class.forName(anEntityClassName);

    }
    catch (ClassNotFoundException e)  {
      System.err.println("[EntityBrowser] The class with the specified name: "
      + anEntityClassName +
        " was not found. Message is " + e.getMessage());
      e.printStackTrace(System.err);
      return new ArrayList();
    }
    SortedMap pathes = EntityPropertyHandler.getAllEntityPathes(entityClass);
    Collection entities = pathes.values();
    List list = new ArrayList();
    Iterator iterator = entities.iterator();
    while (iterator.hasNext())  {
      String shortKey = ((EntityPath) iterator.next()).getShortKey();
      list.add(shortKey);
    }
    return list;
  }
  
  
  
  /**Sets the default columns, that is these columns are only shown if the user has not chosen
   * any columns yet.
   * 
  *  Set  a  shortKey of an entity path with an order number. It is not
  * necessary to have order numbers that covers a complete range of integers
  * like e.g. 0 to 10. The order numbers are only used to determine the order of
  * the columns. Therefore it is well defined if the numbers are spread e.g. 2,
  * 7, 122 and 412 (example with four columns).
   * 
   * if you change the name of this method please change the 
   * corresponding method identifier variable SET_DEFAULT_COLUMNS_METHOD_IDENTIFIER 
   */ 
  public void setDefaultColumn(int orderNumber, String entityPathShortKey)  {
    defaultColumns.put(Integer.toString(orderNumber), entityPathShortKey);    
  }  


  /** Sets the mandatory columns, that is these columns are always shown indepedent 
   * of the columns that the user has chosen. These columns are shown at the
   * beginning of the table.
   * 
   * Set a shortKey of an entity path with an order
   * number. It is not necessary to have order numbers that covers a complete
   * range of integers like e.g. 0 to 10. The order numbers are only used to
   * determine the order of the columns. Therefore it is well defined if the
   * numbers are spread e.g. 2, 7, 122 and 412 (example with four columns).
   */
  public void setMandatoryColumn(int orderNumber, String entityPathShortKey) {
    mandatoryColumns.put(Integer.toString(orderNumber), entityPathShortKey);  
  }


    
  public void setEntities(Collection entities)  {
    this.entities = entities;
  }    
  
  public void setLeftUpperCorner(int xAnchorPosition, int yAnchorPosition)  {
    this.xAnchorPosition = xAnchorPosition;
    this.yAnchorPosition = yAnchorPosition;
  }
  
    
  public String getBundleIdentifier(){
    return EntityBrowser.IW_BUNDLE_IDENTIFIER;
  }
  
  public void setEntityToPresentationConverter(String pathShortKey, EntityToPresentationObjectConverter converter) {
    // lazy initialization (in most cases you do not need this map)
    if (entityToPresentationConverters == null)
      entityToPresentationConverters = new HashMap();
    entityToPresentationConverters.put(pathShortKey, converter);
  }
  
  private EntityToPresentationObjectConverter getEntityToPresentationConverter(EntityPath path)  {
    EntityToPresentationObjectConverter converter;
    if  (  
      // are there any converters at all?  
           entityToPresentationConverters == null 
      // use the shortkey to find a suitable converter
        || ((( converter = (EntityToPresentationObjectConverter) 
           entityToPresentationConverters.get(path.getShortKey())) == null ) 
      // the key was not found, try to get a converter for the class
        && (( converter = (EntityToPresentationObjectConverter)
           entityToPresentationConverters.get(path.getSourceEntityClass().getName())) == null )))
      // okay we give up! return default converter
        return getMyDefaultConverter(); 
    return converter;
  }


  public IWPresentationEvent getPresentationEvent() { 
    EntityBrowserEvent model = new  EntityBrowserEvent();
    //EntityBrowserEvent model = (EntityBrowserEvent)initEvent(iwc,EntityBrowserEvent.class);
    // necessary: set source!
    //////model.setSource(myLocation);
    /////model.setSource(presentationState.getLocation());
    //model.setSource(getLocation());
    model.setSource(this);
    //model.setEntityName(entityName);
    String id = IWMainApplication.getEncryptedClassName(UserApplication.Top.class);
    id = PresentationObject.COMPOUNDID_COMPONENT_DELIMITER + id;
    model.setController(id);
    return model;
  }
    
  public void main(IWContext iwc) throws Exception { 

    // event model stuff
    EntityBrowserPS state = (EntityBrowserPS) getPresentationState((IWUserContext) iwc);
    this.addActionListener( (IWActionListener) state);
   
    // get resource bundle
    IWResourceBundle resourceBundle = getResourceBundle(iwc);
    
    // get entity name from one element of the entity collection
    // if the entity name is not set and the collection is not empty
    if ( leadingEntityName == null || leadingEntityName.length() == 0 )  {
      if (entities == null || entities.isEmpty()) {
        setErrorContent(resourceBundle);
        return;
      }
      else {
        Class objectClass = (entities.iterator()).next().getClass();
        Class[] interfaces = objectClass.getInterfaces();
        if (interfaces.length > 0)  {
          Class firstInterfaceClass = interfaces[0];
          leadingEntityName = firstInterfaceClass.getName();
        }
        else {
          setErrorContent(resourceBundle);
          return;
        }
      }
    }
   
    // get user properties, set MultiPropertyhandler
    MultiEntityPropertyHandler multiPropertyHandler;
    try {
      multiPropertyHandler = new MultiEntityPropertyHandler(iwc, leadingEntityName);
    }
    catch (ClassNotFoundException e)  {
      System.out.println("[EntityBrowser] Class was not recognized: " + leadingEntityName + " Message was: " +
        e.getMessage());
      System.err.println(e.getStackTrace());      setErrorContent(resourceBundle);
      return;
    }
    if (entityNames != null)  {
      Iterator iterator = entityNames.iterator();
      while (iterator.hasNext())  {
        try {
          multiPropertyHandler.addEntity((String) iterator.next());
        }
        catch (ClassNotFoundException e)  {
        // do not show the error content, continue!
          System.out.println("[EntityBrowser] Class was not recognized: " + leadingEntityName + " Message was: " +
          e.getMessage());
          System.err.println(e.getStackTrace());
        }
      }
    }
            
    List visibleOrderedEntityPathes = getVisibleOrderedEntityPathes(multiPropertyHandler);
    int numberOfRowsPerPage = multiPropertyHandler.getNumberOfRowsPerPage();
    
    // get the state of the former iterator
    SetIterator entityIterator = retrieveSetIterator(iwc, entities);
    String formerStateOfIterator = entityIterator.getStateAsString();
    // set properties (it does not matter if they have change or not)
    entityIterator.setIncrement(numberOfRowsPerPage);
    entityIterator.setQuantity(numberOfRowsPerPage);
    
    // parse action use as key the state of the iterator:
    // If there is a reload of the page the action "next subset" or
    // "previous subset" will no be performed again.
    String action = parseAction(iwc, formerStateOfIterator);
    
    // let iterator point to a new subset   
    if (NEXT_SUBSET_ACTION.equals(action) && entityIterator.hasNextSet())
      entityIterator.nextSet();
    else if (PREVIOUS_SUBSET_ACTION.equals(action) && entityIterator.hasPreviousSet())
      entityIterator.previousSet();
    else 
      entityIterator.currentSet();  
    
    // set size of table
    int necessaryRows = entityIterator.getQuantity();
    int necessaryColumns = visibleOrderedEntityPathes.size();
    // we need at least on column for buttons
    necessaryColumns = (necessaryColumns == 0) ? 1 : necessaryColumns;
    // plus rows for header and buttons
    necessaryRows += 2;
    setSize(necessaryColumns, necessaryRows);
            
    // get now the table    
    fillEntityTable(resourceBundle, visibleOrderedEntityPathes , entityIterator, iwc);
    
    // get the last row and merge it
    int beginxpos = xAnchorPosition + 1;
    // rows of the data plus header row 
    int beginypos = yAnchorPosition + necessaryRows;
    int endypos = beginypos;
    int endxpos = xAnchorPosition + necessaryColumns;
    mergeCells(beginxpos, beginypos, endxpos, endypos);
    // put settings button, info text, forward and back button in one table
    Table table = new Table(4, 1);
    table.add(getSettingsButton(resourceBundle), 1, 1);
    table.add(getInfo(resourceBundle, entityIterator),2,1);
     
    // get current subset position
    String currentStateOfIterator = entityIterator.getStateAsString();
    // store state in session
    entityIterator.storeStateInSession(iwc, getMyId());
    // get back and forward buttons
    Table goAndBackButton = getForwardAndBackButtons(resourceBundle, currentStateOfIterator, entityIterator.hasNextSet(), entityIterator.hasPreviousSet());   
    // create Form
    // add parameters for event handling (if the event model is used)
    if (! useExternalForm)  {
      Form form = new Form();
      form.addEventModel(getPresentationEvent(), iwc);
      form.add(goAndBackButton);
      table.add(form, 3, 1);
    }
    else  {
      table.add(goAndBackButton, 3, 1); 
    }
    
    // now add the table in the row that was created by merging the cells of the last row
    add(table, beginxpos, beginypos);
  }


	private void setSize(int columns, int rows ) {
    // add the anchor positions
    columns += xAnchorPosition;
    rows += yAnchorPosition;
		// set height and width if necessary
		if (columns > getColumns())
		  setColumns(columns);
		if (rows > getRows())
		  setRows(rows);
	}
    
  private  void fillEntityTable(
      IWResourceBundle resourceBundle, 
      List visibleOrderedEntityPathes, 
      SetIterator entitySetIterator,
      IWContext iwc)  
    {
    // build table

    Iterator iterator = visibleOrderedEntityPathes.iterator();
    
    // set header row of the table
    
    int i = 1;
    while (iterator.hasNext())  {
      EntityPath entityPath = (EntityPath) iterator.next();
      String columnName = entityPath.getLocalizedDescription(resourceBundle);
      add(columnName, xAnchorPosition + i , yAnchorPosition + 1);
      i++;
    }
    
    // fill table  
    
    int y = 2;
    while (entitySetIterator.hasNextInSet()) {
      GenericEntity genericEntity = (GenericEntity) entitySetIterator.next();
      Iterator visibleOrderedEntityPathesIterator = visibleOrderedEntityPathes.iterator();
      // set color of rows
			setColorForRow(y);
      int x = 1;
      // fill columns
      while (visibleOrderedEntityPathesIterator.hasNext())  {
        EntityPath path = (EntityPath) visibleOrderedEntityPathesIterator.next();

        EntityToPresentationObjectConverter converter = getEntityToPresentationConverter(path); 
        PresentationObject presentation = converter.getPresentationObject(genericEntity, path, iwc);
        add(presentation, xAnchorPosition + x, yAnchorPosition + y);
        // next column
        x++; 
      }
      // next row
      y++;
    }
  }


	private void setColorForRow(int rowNumber) {
		boolean oddRow = ((rowNumber % 2) == 0);
		if (colorForOddRows != null && oddRow)
		  setRowColor(rowNumber, colorForOddRows);
		else if (colorForEvenRows != null && (! oddRow)) 
		  setRowColor(rowNumber, colorForEvenRows);
	}
  
  private String parseAction(IWContext iwc, String currentStateOfIterator)  {
    // event system
    EntityBrowserPS state = (EntityBrowserPS) getPresentationState((IWUserContext) iwc);
    state.hashCode();
    
    String uniqueKey = getUniqueKeyForSubmitButton(currentStateOfIterator);
    
    String action = null;
    String parameter = state.getParameter();
    
    if (parameter != null && parameter.length() != 0)   
      action = parameter;       
    else if (iwc.isParameterSet(NEW_SUBSET_KEY))  
      action = iwc.getParameter(NEW_SUBSET_KEY);
      
    if ( (NEXT_SUBSET + uniqueKey).equals(action))
      return NEXT_SUBSET_ACTION;
    else if ( (PREVIOUS_SUBSET + uniqueKey).equals(action))
      return PREVIOUS_SUBSET_ACTION;
    else  
      return VIEW_ACTION;
  }

  private SetIterator retrieveSetIterator(IWContext iwc, Collection entities) {    
    // initialize setIterator
    List entityList;
    // EntityList has not implemented the toArray() method that is used 
    // during the execution of new ArrayList(Collection coll) method
    // therefore we simply try to get a list by casting.
    if (entities instanceof List)
      entityList = (List) entities;
    else if (entities == null)
      entityList = new ArrayList();
    else 
      entityList = new ArrayList(entities);
    
    SetIterator setIterator = new SetIterator(entityList);
    // retrieve old state of setIterator, use session
    setIterator.retrieveStateFromSession(iwc, getMyId());
    return setIterator;
  }

  private Table getForwardAndBackButtons(IWResourceBundle resourceBundle, String formerStateOfIterator, boolean enableForward, boolean enableBack) {
 
    String uniqueKey = getUniqueKeyForSubmitButton(formerStateOfIterator);
    SubmitButton goBackButton = 
      new SubmitButton(resourceBundle.getLocalizedImageButton("back","BACK"), NEW_SUBSET_KEY, PREVIOUS_SUBSET + uniqueKey);
    SubmitButton goForwardButton = 
      new SubmitButton(resourceBundle.getLocalizedImageButton("forward","FORWARD"), NEW_SUBSET_KEY, NEXT_SUBSET + uniqueKey);  
    goForwardButton.setDisabled(! enableForward);
    goBackButton.setDisabled(! enableBack);
    Table table = new Table(2,1);
    table.add(goBackButton, 1,1);
    table.add(goForwardButton, 2, 1);
    return table;
  }    
  
  private Table getInfo(IWResourceBundle resourceBundle, SetIterator setIterator)  {
    int firstIndex = setIterator.currentFirstIndexSet() + 1;
    int lastIndex = setIterator.currentLastIndexSet() + 1;
    int size = setIterator.size();
    Table table;
    int columnIndex = 1;
    // firstIndex is larger then lastIndex when the iterator is empty
    if (firstIndex >= lastIndex)  {
      // show e.g. "1 of 12"
      table = new Table(3,1);
    }
    else  {
      // show e.g. "1 - 4 of 12"
      table = new Table(5,1);
      table.add(Integer.toString(firstIndex), columnIndex++, 1);
      table.add("-", columnIndex++, 1);
    }
    table.add(Integer.toString(lastIndex), columnIndex++, 1);
    table.add(resourceBundle.getLocalizedString("of","of"), columnIndex++, 1);
    table.add(Integer.toString(size), columnIndex, 1);  
    table.setCellpadding(5);

    return table;
  }

  /** 
   * Get settings button
	*/
  private Table getSettingsButton(IWResourceBundle resourceBundle) {
    String settings = resourceBundle.getLocalizedString("Settings","Settings");
    Link link = new Link(settings);
    link.setWindowToOpen(EntityBrowserSettingsWindow.class);
    link.addParameter(EntityBrowserSettingsWindow.LEADING_ENTITY_NAME_KEY, leadingEntityName);
    EntityBrowserSettingsWindow.setParameters(link, entityNames, defaultColumns.values());
        
    link.setAsImageButton(true);
    Table table = new Table();
    table.add(link);
    return table;
  }
    
  private String getUniqueKeyForSubmitButton(String stateOfIterator)  {
    StringBuffer buffer = new StringBuffer();
    buffer
    //  .append(NEW_SUBSET_KEY)
      .append(getMyId())
      .append(stateOfIterator);
    return buffer.toString();  
  }

  private int getMyId() {
    int id = getICObjectInstanceID();
    return (id == 0) ? getParentObjectInstanceID() : id;
  }
  
  private void setErrorContent(IWResourceBundle resourceBundle)  {
    String message = resourceBundle.getLocalizedString("Blank table", "Blank table");
    add(message);
  }
  
  private List getVisibleOrderedEntityPathes(MultiEntityPropertyHandler multiPropertyHandler)  {
    List columnsSetByUserList = multiPropertyHandler.getVisibleOrderedEntityPathes();
    // use arrayList because the returned collection of a tree map does not support add operations
    List mandatoryColumns = new ArrayList(this.mandatoryColumns.values());
    // columnsSetByUserList is empty...  
    // there are no visible columns set by the user, therefore show the default columns  
    // default columns is a tree map values returns an ordered collection
    if (columnsSetByUserList.isEmpty()) {
      Collection defaultColumns = this.defaultColumns.values();
      mandatoryColumns.addAll(defaultColumns);
    }
    List list = new ArrayList();
    Iterator mandatoryColumnsIterator = mandatoryColumns.iterator();
    while (mandatoryColumnsIterator.hasNext()) {
      String shortKey = (String) mandatoryColumnsIterator.next();
      EntityPath path = multiPropertyHandler.getEntityPath(shortKey);
      if (path != null)
        list.add(path);
    }
    list.addAll(columnsSetByUserList);
    return list;
  }           
  
  private int getNumberOfRowsPerPage(EntityPropertyHandler propertyHandler) {
    int rows = propertyHandler.getNumberOfRowsPerPage();
    if (rows != EntityPropertyHandler.DEFAULT_NUMBER_OF_ROWS_PER_PAGE)
      return rows;
    // rows was not set
    // there are no rows set by the user, therefore show the default size
    return defaultNumberOfRowsPerPage;
  }
  
  
  private EntityToPresentationObjectConverter getMyDefaultConverter()  {
    if (defaultConverter == null) 
      defaultConverter = EntityBrowser.getDefaultConverter(); 
    return defaultConverter;
  }
    

  public static EntityToPresentationObjectConverter getDefaultConverter() {
    return new EntityToPresentationObjectConverter() {
            
      public PresentationObject getPresentationObject(GenericEntity genericEntity, EntityPath path, IWContext iwc)  {
        StringBuffer displayValues = new StringBuffer();
        List list = path.getValues((GenericEntity) genericEntity);
        Iterator valueIterator = list.iterator();
        while (valueIterator.hasNext()) {
          Object object = valueIterator.next();
          // if there is no entry the object is null
          object = (object == null) ? "" : object;  
          displayValues.append(object.toString());
          // append white space
          displayValues.append(' ');  
        }          
        return new Text(displayValues.toString());
      }
    };        
  }


  
  
  /** this method is used for the event model */
  public IWPresentationState getPresentationState(IWUserContext iwuc){
    if(presentationState == null){
      try {
        IWStateMachine stateMachine = (IWStateMachine)IBOLookup.getSessionInstance(iwuc,IWStateMachine.class);
        presentationState = (EntityBrowserPS)stateMachine.getStateFor(getCompoundId(),EntityBrowserPS.class);
      }
      catch (RemoteException re) {
        throw new RuntimeException(re.getMessage());
      }
    }
    return presentationState;
  }


  /** mandatory method for interface StatefullPresentation 
   * 
	*/
  
  public Class getPresentationStateClass()  {
    return EntityBrowserPS.class;
  }

  
	/**
	 * Sets the colorForEvenRows.
	 * @param colorForEvenRows The colorForEvenRows to set
	 */
	public void setColorForEvenRows(String colorForEvenRows) {
		this.colorForEvenRows = colorForEvenRows;
	}

	/**
	 * Sets the colorForOddRows.
	 * @param colorForOddRows The colorForOddRows to set
	 */
	public void setColorForOddRows(String colorForOddRows) {
		this.colorForOddRows = colorForOddRows;
	}

}
