package com.idega.block.entity.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.idega.data.EntityRepresentation;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationState;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.Table;
import com.idega.presentation.TableType;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Parameter;
import com.idega.presentation.ui.ScrollTable;
import com.idega.util.SetIterator;
/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */

public class EntityBrowser extends Table implements SpecifiedChoiceProvider, StatefullPresentation {
  
  public final static String IW_BUNDLE_IDENTIFIER = "com.idega.block.entity";
  

  private final static String NEW_SUBSET_KEY = "new_subset_key";
  
  private final static String NEW_SUBSET_FROM_LIST_KEY = "new_subset_from_list_key";
  
  private final static String HEADER_FORM_KEY = "header_form_key";
  
  private final static String BOTTOM_FORM_KEY = "bottom_form_key";
  
  private final static String EXTERNAL_FORM_KEY = "external_form_key";
  
  private final static String SHOW_ALL_KEY = "show_all_key";
  
  private final static String All_ENTITIES_WERE_SHOWN = "all_entities_were_shown";
  
  public final static String REQUEST_KEY = "re_key";
  
  public final static String REQUEST_FROM_HEADER_FORM_KEY = HEADER_FORM_KEY + REQUEST_KEY;
  public final static String REQUEST_FROM_BOTTOM_FORM_KEY = BOTTOM_FORM_KEY + REQUEST_KEY;
  public final static String REQUEST_FROM_EXTERNAL_FORM_SHOW_ALL_ENTITIES_KEY
     = EXTERNAL_FORM_KEY + REQUEST_KEY; 
     
  public final static String EXTERNAL_FORM_SHOW_ALL_ENTITIES_KEY = "req_show_all_entities";
  
  // this parameter enables the entity browser to remove the 
  // stored state in the session
  private final static String LAST_USED_MY_ID_KEY = "last_my_id_key";
  private final static String HEADER_FORM_LAST_USED_MY_ID_KEY = HEADER_FORM_KEY + LAST_USED_MY_ID_KEY;
  private final static String BOTTOM_FORM_LAST_USED_MY_ID_KEY = BOTTOM_FORM_KEY + LAST_USED_MY_ID_KEY;
  
  private final static String NEXT_SUBSET = "next";
  
  private final static String PREVIOUS_SUBSET = "previous";
 
  private static final String SET_ENTIY_METHOD_IDENTIFIER = 
    ":method:1:implied:void:setLeadingEntity:java.lang.String:";
    
  private static final String SET_DEFAULT_COLUMNS_METHOD_IDENTIFIER = 
    ":method:1:implied:void:setDefaultColumns:int:java.lang.String:";  
  
  // some important default settings for the view
  private int defaultNumberOfRowsPerPage = 1;
  private int MAX_ROWS_PER_PAGE = 1000;
  
  // this flag is set by a checkbox 
  private boolean showAllEntities = false;
  
  private int defaultNumberOfLinksPreviousToCurrentSet = 4;
  private int defaultNumberOfLinksAfterCurrentSet = 4;
  
  private int rowLimitForShowingBottomNavigation = 10;
  private boolean showHeaderNavigation = true;
  private boolean showBottomNavigation = true;
  
  private final static String STYLE = 
        "font-family:arial; font-size:9pt; text-align: justify;";

  private String styledLink = "styledLinkGeneral";
  
  private String leadingEntityName = null;
  
  // foreign entities
  private List entityNames = null;
  
  private int xAnchorPosition = 0;
  
  private int yAnchorPosition = 0;

  // collection of entities that serves as source of the content  
  private Collection entities = null;
  
  // collection of presentation objects that are added to the last row of the entity browser (e.g. delete buttons)
  private Collection additionalPresentationObjects = null;
  
  // an unique key for the collection 
  // (only important if you use more than one entityBrowser on a web site)  
  private String keyForEntityCollection = "";
  
  private IWPresentationState presentationState = null;
  
  // map of converters
  private HashMap entityToPresentationConverters = null;

  // the default converter just shows a text  
  private EntityToPresentationObjectConverter defaultConverter = null;
  
  // use tree map because of the order of the elements
  private TreeMap defaultColumns = null;
  private TreeMap mandatoryColumns = null;
  private TreeMap optionColumns = null;
  
  // map that assign a color to each column
  // set this variable to null because in most cases this feature is not used
  private Map entityPathShortKeyColorMap = null;
  private Map entityPathShortKeyAlignmentMap = null;
  
  private boolean useExternalForm = false;
  private boolean useEventSystem = false;
  
  /** this flag indicates if the browser should use and accept the settings of the user
  * If this flag is set to false the user settings button will not be shown even if the
  * showSettingButton flag is set to true.
  */
  private boolean acceptUserSettings = true;
  private boolean showSettingsButton = true;
  private boolean showMirroredView = false;
  private boolean leadingEntityIsUndefined = false;
  private boolean useScrollbars = false;
  private int heightScrollTable = 100;
  private int widthScrollTable = 100;
  
  protected String nullValueForNumbers = "";
  
  private String colorForEvenRows = null;
  private String colorForOddRows = "#EFEFEF";
  private String colorForHeader= "#DFDFDF";
  
  protected Text defaultTextProxy = new Text();
  protected Text columnTextProxy = new Text();
  
  private int currentRow = -1;
  private int currentColumn = -1;
  private int currentIndexOfEntities= -1;
  
  private String myId = null;
  
  private Collection mandatoryParameters = null;
  
  /**
   * Returns an EntityBrowser using an own form. 
   * The event system isn't used.
   * @return
   */
  public static EntityBrowser getInstance() {
		return new EntityBrowser();
	}
	
  	/** Returns an EntityBrowser that doesn't use an own form.
   * The caller has to add the browser to a form.
   * The event system isn't used.
   * @return
   */
	public static EntityBrowser getInstanceUsingExternalForm() {
		EntityBrowser browser = EntityBrowser.getInstance();
		browser.setUseExternalForm(true);
		return browser;
	}

	/**
   * Returns an EntityBrowser using an own form. 
   * The event system is used.
   * @return
   */
	public static EntityBrowser getInstanceUsingEventSystem() {
		EntityBrowser browser = EntityBrowser.getInstance();
		browser.setUseEventSystem(true);
		return browser;
	}
	
	/**
   * Returns an EntityBrowser that doesn't use a form.
   * The caller has to add the browser to a form.
   * The event system is used.
   * @return
   */
	public static EntityBrowser getInstanceUsingEventSystemAndExternalForm() {
		EntityBrowser browser = EntityBrowser.getInstanceUsingExternalForm();
		browser.setUseEventSystem(true);
		return browser;
	}

	private EntityBrowser() {
		super();
        setCellspacing(0);
        setCellpadding(0);
	}

  public static void releaseBrowser(IWContext iwc) {
    if (iwc.isParameterSet(HEADER_FORM_LAST_USED_MY_ID_KEY))  {
      String id = iwc.getParameter(HEADER_FORM_LAST_USED_MY_ID_KEY);
      SetIterator.releaseStoredState(iwc, id);
    } 
    if (iwc.isParameterSet(BOTTOM_FORM_LAST_USED_MY_ID_KEY)) {
      String id = iwc.getParameter(BOTTOM_FORM_LAST_USED_MY_ID_KEY);
      SetIterator.releaseStoredState(iwc, id);
    }  
  }
  
  public Text getDefaultTextProxy() {
		return defaultTextProxy;
  }
  
  public void setDefaultNumberOfRows(int defaultNumberOfRows) {
    this.defaultNumberOfRowsPerPage = defaultNumberOfRows;
  }
  
 
  /** if you change the name of this method please change the 
   *  corresponding method identifier variable SET_ENTIY_METHOD_IDENTIFIER */
  public void setLeadingEntity(String leadingEntityName)  {
  	leadingEntityIsUndefined = false;
    this.leadingEntityName = leadingEntityName;
  }
  
  public void setLeadingEntity(Class leadingEntityClass)  {
  	leadingEntityIsUndefined = false;
    this.leadingEntityName = leadingEntityClass.getName();
  }
  
  public void setLeadingEntityIsUndefined()	{
  	leadingEntityIsUndefined = true;
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
  
  public void setUseEventSystem(boolean useEventSystem) {
    this.useEventSystem = useEventSystem;
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
    Collection entitiesTemp = pathes.values();
    List list = new ArrayList();
    Iterator iterator = entitiesTemp.iterator();
    while (iterator.hasNext())  {
      String shortKey = ((EntityPath) iterator.next()).getShortKey();
      list.add(shortKey);
    }
    return list;
  }
  
  /**Sets an option column, that is the column is shown in the settings window
  *  
  *  Set  a  shortKey of an entity path with an order number. It is not
  * necessary to have order numbers that covers a complete range of integers
  * like e.g. 0 to 10. The order numbers are only used to determine the order of
  * the columns. Therefore it is well defined if the numbers are spread e.g. 2,
  * 7, 122 and 412 (example with four columns).
   */
  public void setOptionColumn(int orderNumber, String entityPathShortKey)  {
  	if (optionColumns == null) {
  		optionColumns = new TreeMap();
  	}
  	optionColumns.put(new Integer(orderNumber), entityPathShortKey);    
  }  
  
  
  /**Sets the default columns, that is these columns are only shown if the user has not chosen
   * any columns yet.
   * 
  *  Set  a  shortKey of an entity path with an order number. It is not
  * necessary to have order numbers that covers a complete range of integers
  * like e.g. 0 to 10. The order numbers are only used to determine the order of
  * the columns. Therefore it is well defined if the numbers are spread e.g. 2,
  * 7, 122 and 412 (example with four columns).
   */
  public void setDefaultColumn(int orderNumber, String entityPathShortKey)  {
  	if (defaultColumns == null) {
  		defaultColumns = new TreeMap();
  	}
    defaultColumns.put(new Integer(orderNumber), entityPathShortKey);    
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
  	if (mandatoryColumns == null) {
  		mandatoryColumns = new TreeMap();
  	}
    mandatoryColumns.put(new Integer(orderNumber), entityPathShortKey);  
  }

	public void setMandatoryColumnWithConverter(int orderNumber, String entityPathShortKey, EntityToPresentationObjectConverter converter)	{
		setMandatoryColumn(orderNumber, entityPathShortKey);
		if (converter != null) {
			setEntityToPresentationConverter(entityPathShortKey, converter);
		}
	} 


  /**
   * Sets the collection of entities that this browser should show. You have to 
   * set a non empty collection otherwise this browser shows nothing.
   * @param keyForEntityCollection  
   * very important if you use more than one entityBrowser on a web site, because
   * this key is used to identify the different states of the collections in the session.
   * Just use an unique string like "havannna", "paris" or "w123".
   * 
   * @param a collection of entities
   */
  public void setEntities(String keyForEntityCollection, Collection entities)  {
    this.keyForEntityCollection = keyForEntityCollection;
    this.entities = entities;
  }    

  /**
   * Sets the collection of entities that this browser should show. You have to 
   * set a non empty collection otherwise this browser shows nothing. 
   * All entities are shown within a single page.
   * After calling this method you can change the property how many 
   * rows should appear per page by invoking the method
   * <code>setDefaultNumberOfRows(int)<code>.
   * 
   * @param keyForEntityCollection  
   * very important if you use more than one entityBrowser on a web site, because
   * this key is used to identify the different states of the collections in the session.
   * Just use an unique string like "havannna", "paris" or "w123".
   * 
   * @param a collection of entities
   */
  public void setShowAllEntities(String keyForEntityCollection, Collection entities)	{
  	setEntities(keyForEntityCollection, entities);
  	if( entities!=null && !entities.isEmpty()) { 
  		setDefaultNumberOfRows(entities.size());
  	}
  }

	  /**
   * Sets the collection of entities that this browser should show. You have to 
   * set a non empty collection otherwise this browser shows nothing.
   * @param keyForEntityCollection  
   * very important if you use more than one entityBrowser on a web site, because
   * this key is used to identify the different states of the collections in the session.
   * Just use an unique string like "havannna", "paris" or "w123".
   * 
   * @param entities - a collection of entities
   * @param defaultNumberOfrowsPerPage - number of rows per page
   */
  public void setEntities(String keyForEntityCollection, Collection entities, int defaultNumberOfRowsPerPage)	{
  	setEntities(keyForEntityCollection, entities);
  	setDefaultNumberOfRows(defaultNumberOfRowsPerPage);
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
        return getDefaultConverter(); 
    return converter;
  }


  public IWPresentationEvent getPresentationEvent() { 
    EntityBrowserEvent model = new  EntityBrowserEvent();
    model.setSource(this);
    return model;
  }
  
  public void addPresentationObjectToBottom(PresentationObject presentationObject) {
    if (additionalPresentationObjects == null)  {
      additionalPresentationObjects = new ArrayList();
    }
    additionalPresentationObjects.add(presentationObject);
  }
    
  public void main(IWContext iwc) throws Exception { 
    super.main(iwc);
    // event model stuff
    EntityBrowserPS state = (EntityBrowserPS) getPresentationState( iwc);
    this.addActionListener(state);
   
    // get resource bundle
    IWResourceBundle resourceBundle = getResourceBundle(iwc);
    
    // get entity name from one element of the entity collection
    // if the entity name is not set and the collection is not empty
    if (leadingEntityIsUndefined)	{
    	leadingEntityName = "leading entity name is undefined";
    }
    else if ( leadingEntityName == null || leadingEntityName.length() == 0 )  {
      if (entities == null || entities.isEmpty()) {
        setErrorContent();
        return;
      }
    // sometimes entities is a collection of collections
      Class objectClass;
      Object object = entities.iterator().next();
      if (object instanceof Collection) {
        Collection coll = (Collection) object;
        if  (! coll.isEmpty())  {
          objectClass = coll.iterator().next().getClass();
        }
        else  {
          setErrorContent();
          return;
        }
      }
      else  {
        objectClass = object.getClass();
      }
      Class[] interfaces = objectClass.getInterfaces();
      if (interfaces.length > 0)  {
        Class firstInterfaceClass = interfaces[0];
        leadingEntityName = firstInterfaceClass.getName();
      }
      else {
        setErrorContent();
        return;
      }
    }
   
    // get user properties, set MultiPropertyhandler
    MultiEntityPropertyHandler multiPropertyHandler;
    try {
      multiPropertyHandler = (leadingEntityIsUndefined) ?
      	new MultiEntityPropertyHandler(iwc) : 
      	new MultiEntityPropertyHandler(iwc, leadingEntityName);
    }
    catch (ClassNotFoundException e)  {
      System.err.println("[EntityBrowser] Class was not recognized: " + leadingEntityName + " Message was: " +
      e.getMessage());
      // e.printStackTrace(System.err);
      setErrorContent();
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
          System.err.println("[EntityBrowser] Class was not recognized: " + leadingEntityName + " Message was: " +
          e.getMessage());
          //e.printStackTrace(System.err);
        }
      }
    }
             
    List visibleOrderedEntityPathes = getVisibleOrderedEntityPathes(multiPropertyHandler);
    // check if all entities should be shown
    parseAndDoActionNumberOfRowsPerPage(iwc, state);
    int numberOfRowsPerPage = getNumberOfRowsPerPage(multiPropertyHandler);
    
    // get and save the state of the former iterator BEFORE changing the iterator
    SetIterator entityIterator = retrieveSetIterator(iwc, entities);
    String formerStateOfIterator = entityIterator.getStateAsString();
    // set properties (it does not matter if they have change or not)
    entityIterator.setIncrement(numberOfRowsPerPage);
    entityIterator.setQuantity(numberOfRowsPerPage);
    
    // parse action use as key the state of the iterator:
    // If there is a reload of the page the action "next subset" or
    // "previous subset" will no be performed again.
    parseAndDoAction(iwc, state, formerStateOfIterator, entityIterator);
    // set size of table
    int necessaryRows = entityIterator.sizeSet();
    int necessaryColumns = visibleOrderedEntityPathes.size();
    if (showMirroredView) {
      int temp = necessaryColumns;
      necessaryColumns = necessaryRows;
      necessaryRows = temp;
    }
    if (showMirroredView) {
      // we need at least on column for buttons plus headers
      necessaryColumns = (necessaryColumns == 0) ? 2 : necessaryColumns;
      // plus rows for buttons
    }
    else if (useScrollbars) {
    	// use one cell to put the scrallable table into it
    	necessaryColumns = 1;
    	necessaryRows = 3;
    }
    else {  
      // we need at least on column for buttons 
      necessaryColumns = (necessaryColumns == 0) ? 1 : necessaryColumns;
      // plus rows for header and buttons
      necessaryRows += 3;
    }
    setSize(necessaryColumns, necessaryRows);
            
    // get now the table    
    if (useScrollbars) {
    	fillScrollableEntityTable(visibleOrderedEntityPathes, entityIterator, iwc);
    }
    else {
    	fillEntityTable(visibleOrderedEntityPathes , entityIterator, iwc);
    }
    
    //TODO: thi: enable the mirrored view to work together with the navigation panel
    if (showMirroredView) {
      return;
    }
    
    boolean enableForward = entityIterator.hasNextSet();
    boolean enableBack = entityIterator.hasPreviousSet(); 
    
    // get current subset position
    String currentStateOfIterator = entityIterator.getStateAsString();
    // store state in session
    entityIterator.storeStateInSession(iwc, keyForEntityCollection, getMyId());
    // set hidden input (important for releasing)
    if (useExternalForm)  {     
      HiddenInput hiddenInputLastUsedMyId = new HiddenInput(HEADER_FORM_LAST_USED_MY_ID_KEY, getMyId());
      HiddenInput hiddenAllEntitiesWereShown = 
        new HiddenInput(REQUEST_FROM_EXTERNAL_FORM_SHOW_ALL_ENTITIES_KEY, (new Boolean(showAllEntities)).toString());
      add(hiddenInputLastUsedMyId);
      add(hiddenAllEntitiesWereShown);
    }
    boolean showHeaderNavigationPanel = 
      (showHeaderNavigation && (enableBack || enableForward));
    if (showHeaderNavigationPanel)  {  
      setNavigationPanel( HEADER_FORM_KEY, 
                          iwc, 
                          resourceBundle, 
                          entityIterator, 
                          currentStateOfIterator, 
                          enableBack, 
                          enableForward,
                          1,
                          necessaryColumns,
                          false);
    }
    boolean showBottomNavigationPanel =
      (showBottomNavigation 
            && (enableBack || enableForward) 
            && ( (! showHeaderNavigation) || 
                 (entityIterator.getIncrement() > rowLimitForShowingBottomNavigation)) );
    if (showBottomNavigationPanel)  {                       
      setNavigationPanel( BOTTOM_FORM_KEY, 
                        iwc, 
                        resourceBundle, 
                        entityIterator, 
                        currentStateOfIterator, 
                        enableBack, 
                        enableForward,
                        necessaryRows,
                        necessaryColumns,
                        true);
    }
    // special case:
    // if both panel were not set, set the settings button now...
    if (!showHeaderNavigationPanel && !showBottomNavigationPanel) {
    	if (entityIterator.getIncrement() > rowLimitForShowingBottomNavigation) {
    		setOnlySettingsButtonHeader(HEADER_FORM_KEY, enableBack, enableForward, resourceBundle, 1, necessaryColumns);
    	}
      setOnlySettingsButton(BOTTOM_FORM_KEY, enableBack, enableForward, resourceBundle,necessaryRows,necessaryColumns);
    }
    // special case:
    // if only the header panel was set, set the additional presentationObjects now
    if (showHeaderNavigationPanel && !showBottomNavigationPanel)  {
      setOnlyAdditionalPresentationObjects(necessaryRows, necessaryColumns);
    }
  }
  
  private Table getAdditionalPresentationObjects() {
    if (additionalPresentationObjects == null)  {
      return null;
    }
    int size = additionalPresentationObjects.size();
    Table table = new Table(size, 1);
    Iterator iterator = additionalPresentationObjects.iterator();
    int x = 1;
    while (iterator.hasNext())  {
      PresentationObject presentationObject = (PresentationObject) iterator.next();
      table.add(presentationObject, x++ , 1);
    }
    return table;
  }
  
  private void setOnlyAdditionalPresentationObjects(int bottomRightCornerY, int bottomRightCornerX)  {
    // create table
    Table panelTable = getAdditionalPresentationObjects();
    if (panelTable == null) {
      return;
    }
    // get the desired row and merge it
    int panelBeginxpos = xAnchorPosition + 1;
    int panelBeginypos = yAnchorPosition + bottomRightCornerY;
    int panelEndxpos = xAnchorPosition + bottomRightCornerX;
    int panelEndypos = panelBeginypos;
    // merge cell
    mergeCells(panelBeginxpos, panelBeginypos, panelEndxpos, panelEndypos);
    // now add the table in the row that was created by merging the cells of the last row
    add(panelTable, panelBeginxpos, panelBeginypos);
  }
 
  private void setOnlySettingsButtonHeader(
      String formKey,
      boolean enableBack,
      boolean enableForward, 
      IWResourceBundle resourceBundle, 
      int bottomRightCornerY, 
      int bottomRightCornerX) {
    // get the desired row and merge it
    int panelBeginxpos = xAnchorPosition + 1;
    int panelBeginypos = yAnchorPosition + bottomRightCornerY;
    int panelEndxpos = xAnchorPosition + bottomRightCornerX;
    int panelEndypos = panelBeginypos;
    // merge cell

    // create table
    Table panelTable = new Table(3,1);
    // add settings 
    // panelTable.add(getSettingsButton(resourceBundle),2,1);
    // add show all check box
    Table showAllTable = getShowAllCheckBox(formKey, enableBack, enableForward, resourceBundle);
    if (showAllTable != null) {
      panelTable.add(showAllTable,1,1);   
      mergeCells(panelBeginxpos, panelBeginypos, panelEndxpos, panelEndypos);
    
    // add additional presentation objects
    //Table table = getAdditionalPresentationObjects();
    //panelTable.add(table , 3, 1);
    // now add the table in the row that was created by merging the cells of the last row
    	add(panelTable, panelBeginxpos, panelBeginypos);
    	if (useExternalForm)	{
    	  	HiddenInput hiddenInputRequestFrom = new HiddenInput(formKey + REQUEST_KEY);
    	  	add(hiddenInputRequestFrom);
    	}
    }
  }     
    
  
  private void setOnlySettingsButton(
      String formKey,
      boolean enableBack,
      boolean enableForward, 
      IWResourceBundle resourceBundle, 
      int bottomRightCornerY, 
      int bottomRightCornerX) {
    // get the desired row and merge it
    int panelBeginxpos = xAnchorPosition + 1;
    int panelBeginypos = yAnchorPosition + bottomRightCornerY;
    int panelEndxpos = xAnchorPosition + bottomRightCornerX;
    int panelEndypos = panelBeginypos;
    // merge cell
    mergeCells(panelBeginxpos, panelBeginypos, panelEndxpos, panelEndypos);
    // create table
    Table panelTable = new Table(3,1);
    // add settings 
    panelTable.add(getSettingsButton(resourceBundle),2,1);
    // add show all check box
    Table showAllTable = getShowAllCheckBox(formKey, enableBack, enableForward, resourceBundle);
    if (showAllTable != null) {
      panelTable.add(showAllTable,1,1);   
    }
    // add additional presentation objects
    Table table = getAdditionalPresentationObjects();
    panelTable.add(table , 3, 1);
    // now add the table in the row that was created by merging the cells of the last row
    add(panelTable, panelBeginxpos, panelBeginypos);
    if (useExternalForm)	{
    	  HiddenInput hiddenInputRequestFrom = new HiddenInput(formKey + REQUEST_KEY);
    	  add(hiddenInputRequestFrom);
    }
  }     
    
    

  private void setNavigationPanel(
      String formKey, 
      IWContext iwc, 
      IWResourceBundle resourceBundle, 
      SetIterator entityIterator, 
      String currentStateOfIterator, 
      boolean enableBack, 
      boolean enableForward,
      int bottomRightCornerY,
      int bottomRightCornerX,
      boolean showAdditionalPresentationObject)  {
    // get the desired row and merge it
    int panelBeginxpos = xAnchorPosition + 1;
    int panelBeginypos = yAnchorPosition + bottomRightCornerY;
    int panelEndxpos = xAnchorPosition + bottomRightCornerX;
    int panelEndypos = panelBeginypos;
    // merge cell
    mergeCells(panelBeginxpos, panelBeginypos, panelEndxpos, panelEndypos);
    // create table
    Table panelTable = new Table(4,1);
    // add settings 
    panelTable.add(getSettingsButton(resourceBundle),3,1);
    // add show all check box
    Table showAllTable = getShowAllCheckBox(formKey , enableBack, enableForward, resourceBundle);
    if (showAllTable != null) {
      panelTable.add(showAllTable,2,1);
    }
    // get links
    Table goAndBackButtonPanel = 
      getForwardAndBackButtons(formKey,iwc, resourceBundle, entityIterator, currentStateOfIterator, enableBack, enableForward);   
    panelTable.add(goAndBackButtonPanel,1,1);
    // add additional presentation objects
    if (showAdditionalPresentationObject) {
      Table table = getAdditionalPresentationObjects();
      if (table != null)  {
        panelTable.add(table , 4, 1); 
      }
    }
    // add form 
    PresentationObject panel;
    HiddenInput hiddenInputRequestFrom = new HiddenInput(formKey + REQUEST_KEY);
    if (! useExternalForm)  {
      HiddenInput hiddenInputLastUsedMyId = new HiddenInput(formKey + LAST_USED_MY_ID_KEY, getMyId());
      Form panelForm = new Form();
      panelForm.add(hiddenInputLastUsedMyId);
      panelForm.add(hiddenInputRequestFrom);
      if (useEventSystem)
        panelForm.addEventModel(getPresentationEvent(),iwc);
      panelForm.add(panelTable);
      panel = panelForm;
    }
    else  {
      panel = panelTable;
      add(hiddenInputRequestFrom);
    }
    // now add the table in the row that was created by merging the cells of the last row
    add(panel, panelBeginxpos, panelBeginypos);
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
      List visibleOrderedEntityPathes, 
      SetIterator entitySetIterator,
      IWContext iwc)  
    {
    // build table

    Iterator iterator = visibleOrderedEntityPathes.iterator();
    
    // set header row of the table
    
    // set color for header
    int i = 1;
    if (colorForHeader != null) {
      if (showMirroredView) {
        setColumnColor(xAnchorPosition + 1, colorForHeader);
      }
      else {
        setRowColor(yAnchorPosition + 2, colorForHeader);
      }
    }
    while (iterator.hasNext())  {
      EntityPath entityPath = (EntityPath) iterator.next();
      EntityToPresentationObjectConverter converter = getEntityToPresentationConverter(entityPath); 
      PresentationObject presentation = converter.getHeaderPresentationObject(entityPath, this, iwc);
      if (showMirroredView) {
        add(presentation, xAnchorPosition + 1, yAnchorPosition + i);
      }
      else {
        add(presentation, xAnchorPosition + i , yAnchorPosition + 2);
      } 
      i++;    
    }
    
    // fill table
    boolean colorForOddRowsIsSet = (colorForOddRows != null);
    boolean colorForEvenRowsIsSet = (colorForEvenRows != null);
    int y = 3;
    while (entitySetIterator.hasNextInSet()) {
      Object genericEntity = entitySetIterator.next();
      Iterator visibleOrderedEntityPathesIterator = visibleOrderedEntityPathes.iterator();
      // set color of rows
      if (showMirroredView) {
        currentColumn = xAnchorPosition + y - 1;
      }
      else {
        currentRow = yAnchorPosition + y;
      }
      setColorForRow(this, currentColumn, currentRow, colorForOddRowsIsSet, colorForEvenRowsIsSet);
      int x = 1;
      // fill columns
      currentIndexOfEntities = entitySetIterator.currentIndexRelativeToZero();
      while (visibleOrderedEntityPathesIterator.hasNext())  {
        if (showMirroredView) {
          currentRow = yAnchorPosition + x;
        }
        else {
          currentColumn = xAnchorPosition + x;
        }
        EntityPath path = (EntityPath) visibleOrderedEntityPathesIterator.next();
        EntityToPresentationObjectConverter converter = getEntityToPresentationConverter(path); 
        PresentationObject presentation = converter.getPresentationObject(genericEntity, path, this, iwc);
        add(presentation, currentColumn, currentRow);
        // set some settings for the current column
        String shortKey = path.getShortKey();
        setColorForColumn(this, shortKey, currentColumn, currentRow);
        setAlignmentForColumn(this, shortKey, currentColumn, currentRow);
        // next column
        x++;
      }
      // nextRow
      y++;
    }
    currentColumn = -1;
    currentRow = -1;
    currentIndexOfEntities = -1;
  }
  
  private  void fillScrollableEntityTable(
        List visibleOrderedEntityPathes, 
        SetIterator entitySetIterator,
        IWContext iwc)  
      {
      // build table
  	
  	ScrollTable scrollTable = new ScrollTable();
  	scrollTable.setCellpadding(0);
  	scrollTable.setCellspacing(0);
  	scrollTable.setNumberOfHeaderRows(1);
	scrollTable.setScrollLayerHeaderRowThickness(30);  // prior 47
	scrollTable.setWidth(widthScrollTable);
	scrollTable.setHeight(heightScrollTable);
  	
      Iterator iterator = visibleOrderedEntityPathes.iterator();
      
      // set header row of the table
      
      // set color for header
      int i = 1;
      if (colorForHeader != null) {
        if (showMirroredView) {
          scrollTable.setColumnColor(1, colorForHeader);
        }
        else {
          scrollTable.setRowColor(1, colorForHeader);
        }
      }
      while (iterator.hasNext())  {
        EntityPath entityPath = (EntityPath) iterator.next();
        EntityToPresentationObjectConverter converter = getEntityToPresentationConverter(entityPath); 
        PresentationObject presentation = converter.getHeaderPresentationObject(entityPath, this, iwc);
        if (showMirroredView) {
          scrollTable.add(presentation, 1, i);
          if (colorForHeader != null) {
            scrollTable.setColor(1, i,colorForHeader);
          }

        }
        else {
          scrollTable.add(presentation, i , 1);
          if (colorForHeader != null) {
            scrollTable.setColor(i, 1,colorForHeader);
          }
        } 
        i++;    
      }
      
      // fill table
      boolean colorForOddRowsIsSet = (colorForOddRows != null);
      boolean colorForEvenRowsIsSet = (colorForEvenRows != null);
      int y = 2;
      while (entitySetIterator.hasNextInSet()) {
        Object genericEntity = entitySetIterator.next();
        Iterator visibleOrderedEntityPathesIterator = visibleOrderedEntityPathes.iterator();
        // set color of rows
        if (showMirroredView) {
          currentColumn = y;
        }
        else {
          currentRow = y;
        }
        setColorForRow(scrollTable, currentColumn, currentRow, colorForOddRowsIsSet, colorForEvenRowsIsSet);
        int x = 1;
        // fill columns
        currentIndexOfEntities = entitySetIterator.currentIndexRelativeToZero();
        while (visibleOrderedEntityPathesIterator.hasNext())  {
          if (showMirroredView) {
            currentRow = x;
          }
          else {
            currentColumn = x;
          }
          EntityPath path = (EntityPath) visibleOrderedEntityPathesIterator.next();
          EntityToPresentationObjectConverter converter = getEntityToPresentationConverter(path); 
          PresentationObject presentation = converter.getPresentationObject(genericEntity, path, this, iwc);
          scrollTable.add(presentation, currentColumn, currentRow);
          // set some settings for the current column
          String shortKey = path.getShortKey();
          setColorForColumn(scrollTable, shortKey, currentColumn, currentRow);
          setAlignmentForColumn(scrollTable, shortKey, currentColumn, currentRow);
          // next column
          x++;
        }
        // nextRow
        y++;
      }
      currentColumn = -1;
      currentRow = -1;
      currentIndexOfEntities = -1;
      add(scrollTable, xAnchorPosition + 1, yAnchorPosition + 2);
    }
  
  private void setAlignmentForColumn(TableType table, String entityPathShortKey, int column, int row) {
    if (entityPathShortKeyAlignmentMap == null) {
      return;
    }
    String alignment = (String) entityPathShortKeyAlignmentMap.get(entityPathShortKey);
    if (alignment == null)  {
      return;
    }
    table.setAlignment(column, row, alignment);
  }

  private void setColorForColumn(TableType table, String entityPathShortKey, int column, int row) {
    if (entityPathShortKeyColorMap == null) {
      return;
    }
    String color = (String) entityPathShortKeyColorMap.get(entityPathShortKey);
    if (color == null)  {
      return; 
    }
    if (showMirroredView) {
      table.setRowColor(row, color);
    }
    else {
      table.setColumnColor(column, color);
    }
  }

  private void setColorForRow(TableType table, int column, int row, boolean colorForOddRowsIsSet, boolean colorForEvenRowsIsSet) {
  	if (showMirroredView)  {
      boolean oddColumn = ((column % 2) == 0);
      if (colorForOddRowsIsSet && oddColumn) { 
        table.setColumnColor(column, colorForOddRows);
      }
      else if (colorForEvenRowsIsSet && !oddColumn)  {
        table.setColumnColor(column, colorForEvenRows);
      } 
    }
    else {
      boolean oddRow = ((row % 2) == 0);
  	  if (colorForOddRowsIsSet && oddRow) {
        table.setRowColor(row, colorForOddRows);
      }
      else if (colorForEvenRowsIsSet && (! oddRow))  {
        table.setRowColor(row, colorForEvenRows);
      }
    }
  }
  
  private void parseAndDoActionNumberOfRowsPerPage(IWContext iwc, EntityBrowserPS state)  {
    String allEntitiesWereShownFromRequest;
    String allEntitiesString;
    if ((allEntitiesString = getAction(iwc, state, EXTERNAL_FORM_SHOW_ALL_ENTITIES_KEY)) != null) {
        showAllEntities = (new Boolean(allEntitiesString)).booleanValue();
    }
    // handle external form
    else if ((allEntitiesWereShownFromRequest = getAction(iwc, state, REQUEST_FROM_EXTERNAL_FORM_SHOW_ALL_ENTITIES_KEY)) != null)  {
      boolean allEntitiesWereShown = new Boolean(allEntitiesWereShownFromRequest).booleanValue();
      boolean showAllEntitiesHeader = (getAction(iwc, state, HEADER_FORM_KEY + SHOW_ALL_KEY) != null);
      boolean showAllEntitiesBottom = (getAction(iwc, state, BOTTOM_FORM_KEY + SHOW_ALL_KEY) != null);
      boolean headerFormExists = (getAction(iwc, state,REQUEST_FROM_HEADER_FORM_KEY) != null);
      boolean bottomFormExists = (getAction(iwc, state,REQUEST_FROM_BOTTOM_FORM_KEY) != null);
      if (headerFormExists && !bottomFormExists && showAllEntitiesHeader) {
        showAllEntities = true;
      }
      else if (!headerFormExists && bottomFormExists && showAllEntitiesBottom)  {
        showAllEntities = true;
      }
      else if (!allEntitiesWereShown && (showAllEntitiesHeader || showAllEntitiesBottom)) {
        showAllEntities = true;
      }
      else if (allEntitiesWereShown && showAllEntitiesHeader && showAllEntitiesBottom) {
        showAllEntities = true;
      }
      else if( allEntitiesWereShown && !headerFormExists && showAllEntitiesBottom) {
      	showAllEntities = true;
      }
      else {
        showAllEntities = false;
      }
    }
    else if (getAction(iwc, state,REQUEST_FROM_HEADER_FORM_KEY) != null)  {
      String allEntitiesWereShown = getAction(iwc, state, HEADER_FORM_KEY + SHOW_ALL_KEY);
      showAllEntities = (allEntitiesWereShown != null);
    }
    else if (getAction(iwc, state,REQUEST_FROM_BOTTOM_FORM_KEY) != null)  {
      String allEntitiesWereShown = getAction(iwc, state, BOTTOM_FORM_KEY + SHOW_ALL_KEY);
      showAllEntities = (allEntitiesWereShown != null);
    }
    else {
      String showAllEntitiesString = getAction(iwc, state, All_ENTITIES_WERE_SHOWN);
      if (showAllEntitiesString != null)  {
        showAllEntities = new Boolean(showAllEntitiesString).booleanValue();
      }
    }
  } 

  
  private void parseAndDoAction(IWContext iwc, EntityBrowserPS state, String formerStateOfIterator, SetIterator setIterator)  {
    if (! parseAndDoActionForForm(HEADER_FORM_KEY,iwc,state,formerStateOfIterator,setIterator))
      parseAndDoActionForForm(BOTTOM_FORM_KEY,iwc,state,formerStateOfIterator,setIterator);
    }

  private boolean parseAndDoActionForForm(
      String formKey, 
      IWContext iwc, 
      EntityBrowserPS state,
      String formerStateOfIterator, 
      SetIterator setIterator)  {    

    String keyForwardBack = getUniqueKeyForSubmitButton(formKey,NEW_SUBSET_KEY, formerStateOfIterator);
    String keySelectionFromList = getUniqueKeyForSubmitButton(formKey ,NEW_SUBSET_FROM_LIST_KEY, formerStateOfIterator);
    
		String forwardBackAction = getAction(iwc, state, keyForwardBack);
    String selectionFromListAction = getAction(iwc, state, keySelectionFromList); 

    // action from list
    // current subset has always subset number zero
    int i;
    if (selectionFromListAction != null && 
         (i = Integer.parseInt(selectionFromListAction)) != 0)  {
      setIterator.goToSetRelativeToCurrentSet(i);
    }  
    // action from buttons  
    else if ( (NEXT_SUBSET).equals(forwardBackAction))
      setIterator.nextSet();
    else if ( (PREVIOUS_SUBSET).equals(forwardBackAction))
      setIterator.previousSet();
    else  {
      setIterator.currentSet();
      // there was no action
      return false;
    }
    return true;
   }


	private String getAction(IWContext iwc, EntityBrowserPS state, String keySubmit) {
		String action = null;
		String parameter = null;
		if (state.isParameterSet(keySubmit))  {
		  parameter = state.getParameter(keySubmit);
    }
		if (parameter != null && parameter.length() != 0) {
		  action = parameter;       
    }
		else if (iwc.isParameterSet(keySubmit)) {  
		  action = iwc.getParameter(keySubmit);
    }
		return action;
	}

  private SetIterator retrieveSetIterator(IWContext iwc, Collection entityColl) {    
    // initialize setIterator
    List entityList;
    // EntityList has not implemented the toArray() method that is used 
    // during the execution of new ArrayList(Collection coll) method
    // therefore we simply try to get a list by casting.
    if (entityColl instanceof List)
      entityList = (List) entityColl;
    else if (entityColl == null)
      entityList = new ArrayList();
    else 
      entityList = new ArrayList(entityColl);
    
    SetIterator setIterator = new SetIterator(entityList);
    // retrieve old state of setIterator, use session
    setIterator.retrieveStateFromSession(iwc, keyForEntityCollection, getMyId());
    return setIterator;
  }

  private Table getForwardAndBackButtons(
      String formKey,
      IWContext iwc,
      IWResourceBundle resourceBundle, 
      SetIterator setIterator,
      String currentStateOfIterator, 
      boolean enableBack, 
      boolean enableForward) {
 
    // if the list is completely shown do not show forward and backward buttons
    if ((! enableBack) && (! enableForward))
      return new Table();
    // show buttons  
    String uniqueKey = getUniqueKeyForSubmitButton(formKey, NEW_SUBSET_KEY, currentStateOfIterator);
    // use links
    Link goBackLink = getLinkInstanceWithMandatoryParameters(resourceBundle.getLocalizedString("back","Back"));
    Link goForwardLink = getLinkInstanceWithMandatoryParameters(resourceBundle.getLocalizedString("forward","Forward"));
//    goBackLink.setFontStyle(FONT_STYLE_FOR_LINK);
//    goForwardLink.setFontStyle(FONT_STYLE_FOR_LINK);
    if (useEventSystem) {
      goBackLink.addEventModel(getPresentationEvent(),iwc);
      goForwardLink.addEventModel(getPresentationEvent(),iwc);
    }
    goBackLink.addParameter(uniqueKey, PREVIOUS_SUBSET);
    goForwardLink.addParameter(uniqueKey,NEXT_SUBSET); 
    /* use this if you like to display buttons
        SubmitButton goBackButton = 
        new SubmitButton(resourceBundle.getLocalizedImageButton("back","Back"), uniqueKey, PREVIOUS_SUBSET);
        SubmitButton goForwardButton = 
        new SubmitButton(resourceBundle.getLocalizedImageButton("forward","Forward"), uniqueKey, NEXT_SUBSET);  
        goForwardButton.setDisabled(! enableForward);
        oBackButton.setDisabled(! enableBack);
    */
    Table table = new Table(2,1);
    table.setHeight(7);
    if (enableBack) 
      table.add(goBackLink,1,1);
 
    Iterator iterator = getLinksToPage(formKey, iwc, setIterator, currentStateOfIterator).iterator();
    while (iterator.hasNext())  {
      Link link = (Link) iterator.next();
      table.add(Text.getNonBrakingSpace(),1,1); 
      table.add(link,1,1);
      table.setAlignment(2,1,Table.VERTICAL_ALIGN_TOP); 
    }
    if (enableForward)  {
      table.add(Text.getNonBrakingSpace(),1,1); 
      table.add(goForwardLink,1,1);
    }
    table.add(getPageList(formKey, resourceBundle, setIterator, currentStateOfIterator),2,1);
    return table;
  }    
  
  private DropdownMenu getPageList(String formKey, IWResourceBundle resourceBundle, SetIterator setIterator, String currentStateOfIterator) {
    String key = getUniqueKeyForSubmitButton(formKey, NEW_SUBSET_FROM_LIST_KEY, currentStateOfIterator);
    DropdownMenu menu = new DropdownMenu(key);
    int size = setIterator.size();
    int increment = setIterator.getIncrement();
    int quantity = setIterator.getQuantity();
    int setNumber = setIterator.getNegativeNumberOfPreviousSetsRelativeToCurrentSet();
    int number = 1;
    while (number <= size) {
      StringBuffer buffer = new StringBuffer();
      // first index of subset is value
      buffer.append(number);
      int lastNumberOfSubset = number + quantity; 
      if (quantity > 1)  {
        buffer.append(" - ");
        lastNumberOfSubset = (lastNumberOfSubset > size) ? size : lastNumberOfSubset - 1; 
        buffer.append(lastNumberOfSubset);
      }
      // add special string (e.g."of 12234") at the current shown subset
      // current subset has always subset number zero
      if (setNumber == 0) {
        buffer
          .append(" ")
          .append(resourceBundle.getLocalizedString("of","of"))
          .append(" ")
          .append(size);
      }
      menu.addMenuElement(setNumber, buffer.toString());
      // count sets
      number += increment;
      setNumber++;
    }
    // set selected element
    menu.setSelectedElement(0);
    // set to submit
    menu.setToSubmit();
    menu.setStyleAttribute(STYLE);
    return menu;
  }

  private List getLinksToPage(
      String formKey,
      IWContext iwc, 
      SetIterator setIterator, 
      String currentStateOfIterator)  {
    List listOfLinks = new ArrayList();
    // trick: use the same key as the drop down menu
    String key = getUniqueKeyForSubmitButton(formKey, NEW_SUBSET_FROM_LIST_KEY, currentStateOfIterator);    
    int size = setIterator.size();
    int increment = setIterator.getIncrement();
    int quantity = setIterator.getQuantity();
    int preNumber = setIterator.getNegativeNumberOfPreviousSetsRelativeToCurrentSet();
    int afterNumber = setIterator.getPositiveNumberOfNextSetsRelativeToCurrentSet();
    // adjust number of possible links previous and after current set
    int numberOfLinksAfterCurrentSet = defaultNumberOfLinksAfterCurrentSet;
    int numberOfLinksPreviousToCurrentSet = -defaultNumberOfLinksPreviousToCurrentSet;
    //  prenumber is negative
    if (preNumber >  -defaultNumberOfLinksPreviousToCurrentSet)
      numberOfLinksAfterCurrentSet += ( defaultNumberOfLinksPreviousToCurrentSet + preNumber);
    if (afterNumber < defaultNumberOfLinksAfterCurrentSet)
      numberOfLinksPreviousToCurrentSet -= (defaultNumberOfLinksAfterCurrentSet - afterNumber);
    // prenumber is negative 
    preNumber = (preNumber <  numberOfLinksPreviousToCurrentSet) ? 
      numberOfLinksPreviousToCurrentSet : preNumber; 
    afterNumber = (afterNumber > numberOfLinksAfterCurrentSet) ?
      numberOfLinksAfterCurrentSet : afterNumber;
    int number = setIterator.currentFirstIndexSetRelativeToZero() + 1; // plus one because it starts with zero
    // prenumber is negative
    number += (preNumber * increment);
    while (preNumber <= afterNumber)  {
      StringBuffer buffer = new StringBuffer();
      buffer.append(number);
      int lastNumberOfSubset = number + quantity;
      if (quantity > 1)  {
        buffer.append("-");
        lastNumberOfSubset = (lastNumberOfSubset > size) ? size : lastNumberOfSubset - 1; 
        buffer.append(lastNumberOfSubset);
      }
      Link link = getLinkInstanceWithMandatoryParameters(buffer.toString());
//      link.setFontStyle(FONT_STYLE_FOR_LINK);
      if (preNumber == 0)
        link.setBold();
      if (useEventSystem)
        link.addEventModel(getPresentationEvent(),iwc);
      link.addParameter(key, preNumber);
      listOfLinks.add(link);
      number += increment;
      preNumber++;
    }
    return listOfLinks;
  }

  /** 
   * Get settings button
	*/
  private Table getSettingsButton(IWResourceBundle resourceBundle) {
    // sometimes settings button is not desired
    if (!showSettingsButton)
      return new Table();
    String settings = resourceBundle.getLocalizedString("Settings","Settings");
    Link link = new Link(settings);
    link.setWindowToOpen(EntityBrowserSettingsWindow.class);
    link.addParameter(EntityBrowserSettingsWindow.LEADING_ENTITY_NAME_KEY, leadingEntityName);
    Collection defaultColumnValues = (defaultColumns == null) ? null : defaultColumns.values();
    Collection optionColumnValues = (optionColumns == null) ? null : optionColumns.values();
    EntityBrowserSettingsWindow.setParameters(link, entityNames, defaultColumnValues , optionColumnValues, defaultNumberOfRowsPerPage );
        
    link.setAsImageButton(true);
    Table table = new Table();
    table.add(link);
    return table;
  }
  
  private Table getShowAllCheckBox(String formKey, boolean enableBack, boolean enableForward, IWResourceBundle resourceBundle)  {
    if ( (! enableBack) && (!enableForward) && (!showAllEntities) ) {
      // all entities are shown, it makes no sense to offer the checkbox
      return null;
    } 
    Text showAll = new Text(resourceBundle.getLocalizedString("eb_show_all","show all"));
//	changed to set the font style to match the new style:
		showAll.setStyleClass(styledLink);
 //   showAll.setFontStyle(FONT_STYLE_FOR_LINK);
    StringBuffer buffer = new StringBuffer(formKey);
    buffer.append(SHOW_ALL_KEY);
    CheckBox showAllCheckBox = new CheckBox(buffer.toString());
    showAllCheckBox.setChecked(showAllEntities);
    showAllCheckBox.setToSubmit();
    Table table = new Table(2,1);
    table.add(showAllCheckBox, 1,1);
    table.add(showAll,2,1);
    return table;
  }
    
  private String getUniqueKeyForSubmitButton(String formKey, String prefix, String stateOfIterator)  {
    StringBuffer buffer = new StringBuffer();
    buffer
      .append(formKey)
      .append(prefix)
      .append(getMyId())
      .append(stateOfIterator);
    return buffer.toString();  
  }

  private String getMyId() {
    // use object instance id if possible else compoundId
    if (myId == null) {
      int id;
      PresentationObject object = this;
      do {
        id = object.getICObjectInstanceID();
        try{
        		object = (PresentationObject)object.getParent();
        }
        catch(ClassCastException cce){
        		object=null;
        }
       }
      while (id == 0 && object != null);
      myId = (id != 0) ? Integer.toString(id) : getCompoundId();
    }
    return myId;
  }
  
  private void setErrorContent()  {
    // show nothing
    //// String message = resourceBundle.getLocalizedString("Blank table", "Blank table");
    //// add(message);
  }
  
  private List getVisibleOrderedEntityPathes(MultiEntityPropertyHandler multiPropertyHandler)  {
    // if the user settings should not be accepted set the columns 
    // that are set by the user to an empty collection
    List columnsSetByUserList = (acceptUserSettings) ? multiPropertyHandler.getVisibleOrderedEntityPathes() : null;
    // use arrayList because the returned collection of a tree map does not support add operations
     List tempMandatoryColumns = (mandatoryColumns == null) ? null : new ArrayList(mandatoryColumns.values());
    // columnsSetByUserList is empty...  
    // there are no visible columns set by the user, therefore show the default columns  
    // default columns is a tree map values returns an ordered collection
    if ( (columnsSetByUserList == null || columnsSetByUserList.isEmpty())
					&& defaultColumns != null) {
      Collection tempDefaultColumns = defaultColumns.values();
      if (tempMandatoryColumns == null) {
      	tempMandatoryColumns = new ArrayList(tempDefaultColumns);
      }
      else {
      	tempMandatoryColumns.addAll(tempDefaultColumns);
      }
    }
    List list = new ArrayList();
    if (tempMandatoryColumns != null) {
    	Iterator mandatoryColumnsIterator = tempMandatoryColumns.iterator();
    	while (mandatoryColumnsIterator.hasNext()) {
    		String shortKey = (String) mandatoryColumnsIterator.next();
    		EntityPath path = multiPropertyHandler.getEntityPath(shortKey);
    		if (path != null)
    			list.add(path);
    	}
    }
    if (columnsSetByUserList != null) {
    	list.addAll(columnsSetByUserList);
    }
    return list;
  }           
  
  private int getNumberOfRowsPerPage(MultiEntityPropertyHandler multiPropertyHandler) {
    if (showAllEntities && entities != null)  {
      int rowsPerPage;
      return ((rowsPerPage = entities.size()) > MAX_ROWS_PER_PAGE) ? MAX_ROWS_PER_PAGE : rowsPerPage;
    }
    if (! acceptUserSettings) {
      return defaultNumberOfRowsPerPage;
    }
    int rowsTemp = multiPropertyHandler.getNumberOfRowsPerPage();
    if (rowsTemp == EntityPropertyHandler.NUMBER_OF_ROWS_PER_PAGE_NOT_SET)
      return defaultNumberOfRowsPerPage;
    return rowsTemp;
  }
  
  
  public EntityToPresentationObjectConverter getDefaultConverter()  {
    if (defaultConverter == null) 
      defaultConverter = 
      new EntityToPresentationObjectConverter() {
        
        IWResourceBundle resourceBundle = null;
        
        public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc)  {
          // get resource bundle
          if (resourceBundle == null) {
            resourceBundle = getResourceBundle(iwc);
          }
          String columnName = entityPath.getLocalizedDescription(resourceBundle);
          Text text = (Text) columnTextProxy.clone();
          text.setText(columnName); 
          text.setBold();
          return text;
        }              
            
        public PresentationObject getPresentationObject(Object genericEntity, EntityPath path, EntityBrowser browser, IWContext iwc)  {
          StringBuffer displayValues = new StringBuffer();
          List list = path.getValues((EntityRepresentation) genericEntity);
          List classes = path.getClassesOfValues();
          Iterator valueIterator = list.iterator();
          Iterator classIterator = classes.iterator();
          while (valueIterator.hasNext()) {
            Object object = valueIterator.next();
            Class valueClass = (Class) classIterator.next();
            // if there is no entry the object is null
            if (object == null) {
              // if the column is a number show zero if desired
              if (valueClass != null && Number.class.isAssignableFrom(valueClass)) {
                object = nullValueForNumbers;
              }
              else {
                object = "";
              }
            }
            displayValues.append(object.toString());
            // append white space
            displayValues.append(' ');  
          }
          Text text = (Text) defaultTextProxy.clone();
          text.setText(displayValues.toString());               
          return text;
        }
      }; 
    return defaultConverter;
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
  
  /** Sets the alignment for the specified column.
   * 
   * @param entityPathShortKey
   * @param alignment
   */
  public void setAlignmentForColumn(String entityPathShortKey, String alignment)  {
    if (entityPathShortKeyAlignmentMap == null) {
      entityPathShortKeyAlignmentMap = new HashMap();
    }
    entityPathShortKeyAlignmentMap.put(entityPathShortKey, alignment);
  }
  
  /** Sets the color for the specified column.
   * 
   * @param colorForColumnr
   */
  public void setColorForColumn(String entityPathShortKey, String color)  {
    if (entityPathShortKeyColorMap == null) {
      entityPathShortKeyColorMap = new HashMap();
    }
    entityPathShortKeyColorMap.put(entityPathShortKey, color);
  }
  
  /** Sets the color for the specified columns.
   * 
   * @param entityPathShortKeyColorMap - map, uses entity path short keys as keys and color strings as value
   */
  
  public void setColorForColumns(Map entityPathShortKeyColorMap)  {
    if (this.entityPathShortKeyColorMap == null) {
      this.entityPathShortKeyColorMap = new HashMap();
    }
    this.entityPathShortKeyColorMap.putAll(entityPathShortKeyColorMap);

  }

  /** Sets the color for the header row
   * @param colorForHeader
   */
  public void setColorForHeader(String colorForHeader)  {
    this.colorForHeader = colorForHeader;
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
  
  /**
   * Sets the showSettingButton.
   * @param showSettingButton The showSettingButton to set
   */
  public void setAcceptUserSettingsShowUserSettingsButton(boolean acceptUserSettings, boolean showSettingButton) {
  	this.acceptUserSettings = acceptUserSettings;
    // if acceptUserSettings is false do never show the user settings button!
    this.showSettingsButton = (acceptUserSettings) ? showSettingButton : false; 
  }
  
  /** Changes the view: shows the columns as rows and vice versa.
   * @param showMirroredView - default value is false.
   */
  public void setShowMirroredView(boolean showMirroredView) {
    this.showMirroredView = showMirroredView;
  }
  
	/**
	 * Sets the columnTextProxy.
	 * @param columnTextProxy The columnTextProxy to set
	 */
	public void setColumnTextProxy(Text columnTextProxy) {
		this.columnTextProxy = columnTextProxy;
	}

	/**
	 * Sets the defaultTextProxy.
	 * @param defaultTextProxy The defaultTextProxy to set
	 */
	public void setDefaultTextProxy(Text defaultTextProxy) {
		this.defaultTextProxy = defaultTextProxy;
	}
  
    /** 
     * Sets the value that is shown if a number column is null.
     * The default value is an empty string.
     * @param value 
     */  
    public void setNullValueForNumbers(String nullValueForNumbers)  {
      this.nullValueForNumbers = nullValueForNumbers;
    }

	/**
	 * Returns the currentColumn.
   * Used within EntityToPresentationObjectConverters during printing a cell.
   * Returns -1 if the browser is not printing the table.
	 * @return int
	 */
	public int getCurrentColumn() {
		return currentColumn;
	}

	/**
	 * Returns the currentRow.
   * Used within EntityToPresentationObjectConverters during printing a cell.
   * Returns -1 if the browser is not printing the table.
	 * @return int
	 */
	public int getCurrentRow() {
		return currentRow;
	}
  
  /** 
   * Returns the current index of the current entity within the specified collection of entities.
   * Used within EntityToPresentationObjectConverters during printing a cell.
   * First index is zero.
   * Returns -1 if the browser is not printing the table.
   */
  public int getCurrentIndexOfEntities()  {
    return currentIndexOfEntities;
  }

	/**
	 * Sets the rowLimitForShowingBottomNavigation.
	 * @param rowLimitForShowingBottomNavigation The rowLimitForShowingBottomNavigation to set
	 */
	public void setPageLimitForShowingBottomNavigation(int rowLimitForShowingBottomNavigation) {
		this.rowLimitForShowingBottomNavigation = rowLimitForShowingBottomNavigation;
	}

  public void setShowNavigation(boolean showHeaderNavigation, boolean showBottomNavigation) {
    this.showHeaderNavigation = showHeaderNavigation;
    this.showBottomNavigation = showBottomNavigation;
  }
  
  /** Sets a parameter that is send with every request
   *  
   * @param parameter
   */ 
  public void addMandatoryParameter(Parameter parameter)  {
    if (mandatoryParameters == null)  {
      mandatoryParameters = new ArrayList();
    }
    mandatoryParameters.add(parameter);
  }
  
  public void addMandatoryParameter(String parameterName, String parameterValue)  {
    addMandatoryParameter(new Parameter(parameterName, parameterValue));
  }
  
  public void removeAllMandatoryParameters()  {
    mandatoryParameters = null;
  }
  
  public void addMandatoryParameters(Collection parameters) {
    if (mandatoryParameters == null)  {
      mandatoryParameters = new ArrayList(parameters);
    }
    mandatoryParameters.addAll(parameters);
  }
  
  public Parameter getShowAllEntriesParameter() {
    Parameter parameter = new Parameter(EXTERNAL_FORM_SHOW_ALL_ENTITIES_KEY, String.valueOf(showAllEntities));
    return parameter;
  }
  
  public void setScrollableWithHeightAndWidth(int height, int width) {
  	heightScrollTable = height;
  	widthScrollTable = width;
  	useScrollbars = true;
  }
      
  
  private Link getLinkInstanceWithMandatoryParameters(String text) {
    Link link = new Link(text);
    //added to match styled Links
    link.setStyleClass(styledLink);
    link.addParameter(HEADER_FORM_LAST_USED_MY_ID_KEY, getMyId());
    link.addParameter(All_ENTITIES_WERE_SHOWN, (new Boolean(showAllEntities)).toString());
    if (mandatoryParameters == null)  {
       return link;
    }
    Iterator iterator = mandatoryParameters.iterator();
    while (iterator.hasNext())  {
      Parameter parameter = (Parameter) iterator.next();
      link.addParameter(parameter);
    }
    return link;
  }
    
     
}
