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
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.user.app.UserApplication;
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
  
  private final static String NEXT_SUBSET = "next";
  
  private final static String PREVIOUS_SUBSET = "previous";
 
  private final static String VIEW_ACTION = "view_action";
  
  private static final String SET_ENTIY_METHOD_IDENTIFIER = 
    ":method:1:implied:void:setLeadingEntity:java.lang.String:";
    
  private static final String SET_DEFAULT_COLUMNS_METHOD_IDENTIFIER = 
    ":method:1:implied:void:setDefaultColumns:int:java.lang.String:";  
  
  // some important default settings for the view
  private int defaultNumberOfRowsPerPage = 1;
  private int defaultNumberOfLinksPreviousToCurrentSet = 4;
  private int defaultNumberOfLinksAfterCurrentSet = 4;
  
  private int pageLimitForShowingBottomNavigation = 15;
  private boolean showHeaderNavigation = true;
  private boolean showBottomNavigation = true;
  
  private final static String STYLE = 
        "font-family:arial; font-size:9pt; text-align: justify;";
  private final static String FONT_STYLE_FOR_LINK = STYLE;
  
  private String leadingEntityName = null;
  
  // foreign entities
  private List entityNames = null;
  
  private int xAnchorPosition = 0;
  
  private int yAnchorPosition = 0;

  // collection of entities that serves as source of the content  
  private Collection entities = null;
  
  // an unique key for the collection 
  // (only important if you use more than one entityBrowser on a web site)  
  private String keyForEntityCollection = "";
  
  private IWPresentationState presentationState = null;
  
  // map of converters
  private HashMap entityToPresentationConverters = null;

  // the default converter just shows a text  
  private EntityToPresentationObjectConverter defaultConverter = null;
  
  // use tree map because of the order of the elements
  private TreeMap defaultColumns = new TreeMap();
  private TreeMap mandatoryColumns = new TreeMap();

  
  private boolean useExternalForm = false;
  private boolean useEventSystem = true;
  
  private boolean showSettingButton = true;
  private String colorForEvenRows = null;
  private String colorForOddRows = null;
  
  private Text defaultTextProxy = new Text();
  private Text columnTextProxy = new Text();
  
  private int currentRow = -1;
  private int currentColumn = -1;
  
  private String myId = null;
  
  
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

  /**
   * Sets the collection of entities that this browser should show. You have to 
   * set a non empty collection otherwise this browser will show nothing.
   * 
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
    //model.setEntityName(entityName);
    String id = IWMainApplication.getEncryptedClassName(UserApplication.Top.class);
    id = PresentationObject.COMPOUNDID_COMPONENT_DELIMITER + id;
    model.setController(id);
    return model;
  }
    
  public void main(IWContext iwc) throws Exception { 
    super.main(iwc);
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
        // sometimes entities is a collection of collections
        Class objectClass;
        Object object = entities.iterator().next();
        if (object instanceof Collection) {
          Collection coll = (Collection) object;
          if  (! coll.isEmpty())  {
            objectClass = coll.iterator().next().getClass();
          }
          else  {
            setErrorContent(resourceBundle);
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
      System.err.println("[EntityBrowser] Class was not recognized: " + leadingEntityName + " Message was: " +
      e.getMessage());
      // e.printStackTrace(System.err);
      setErrorContent(resourceBundle);
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
    parseAndDoAction(iwc, formerStateOfIterator, entityIterator);
    // set size of table
    int necessaryRows = entityIterator.getQuantity();
    int necessaryColumns = visibleOrderedEntityPathes.size();
    // we need at least on column for buttons
    necessaryColumns = (necessaryColumns == 0) ? 1 : necessaryColumns;
    // plus rows for header and buttons
    necessaryRows += 3;
    setSize(necessaryColumns, necessaryRows);
            
    // get now the table    
    fillEntityTable(resourceBundle, visibleOrderedEntityPathes , entityIterator, iwc);

    boolean enableForward = entityIterator.hasNextSet();
    boolean enableBack = entityIterator.hasPreviousSet(); 
    
    // get current subset position
    String currentStateOfIterator = entityIterator.getStateAsString();
    // store state in session
    entityIterator.storeStateInSession(iwc, keyForEntityCollection, getMyId());
    
    if (showHeaderNavigation && (enableBack || enableForward)) 
      setNavigationPanel( HEADER_FORM_KEY, 
                          iwc, 
                          resourceBundle, 
                          entityIterator, 
                          currentStateOfIterator, 
                          enableBack, 
                          enableForward,
                          1,
                          necessaryColumns);
    if (showBottomNavigation 
        && (enableBack || enableForward) 
        && ( (! showHeaderNavigation) || 
             (entityIterator.getIncrement() > pageLimitForShowingBottomNavigation)) )                      
    setNavigationPanel( BOTTOM_FORM_KEY, 
                        iwc, 
                        resourceBundle, 
                        entityIterator, 
                        currentStateOfIterator, 
                        enableBack, 
                        enableForward,
                        necessaryRows,
                        necessaryColumns);                       
    
  }

  private void setNavigationPanel(
      String formKey, 
      IWContext iwc, 
      IWResourceBundle resourceBundle, 
      SetIterator entityIterator, 
      String currentStateOfIterator, 
      boolean enableBack, 
      boolean enableForward,
      int bottomRightCornerX,
      int bottomRightCornerY)  {
    // get the desired row and merge it
    int panelBeginxpos = xAnchorPosition + 1;
    int panelBeginypos = yAnchorPosition + bottomRightCornerX;
    int panelEndxpos = xAnchorPosition + bottomRightCornerY;
    int panelEndypos = panelBeginypos;
    // merge cell
    mergeCells(panelBeginxpos, panelBeginypos, panelEndxpos, panelEndypos);
    // create table
    Table panelTable = new Table(2,1);
    // add settings 
    panelTable.add(getSettingsButton(resourceBundle),2,1);
    // get links
    Table goAndBackButtonPanel = 
      getForwardAndBackButtons(formKey,iwc, resourceBundle, entityIterator, currentStateOfIterator, enableBack, enableForward);   
    panelTable.add(goAndBackButtonPanel,1,1);
    // add form 
    PresentationObject panel;
    if (! useExternalForm)  {
      
      Form panelForm = new Form();
      if (useEventSystem)
        panelForm.addEventModel(getPresentationEvent(),iwc);
      panelForm.add(panelTable);
      panel = panelForm;
    }
    else  {
      panel = panelTable;
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
      Text text = (Text) columnTextProxy.clone();
      text.setText(columnName);               
      add(text, xAnchorPosition + i , yAnchorPosition + 2);
      i++;
    }
    
    // fill table  
    
    int y = 3;
    while (entitySetIterator.hasNextInSet()) {
      Object genericEntity = entitySetIterator.next();
      Iterator visibleOrderedEntityPathesIterator = visibleOrderedEntityPathes.iterator();
      // set color of rows
			setColorForRow(y);
      int x = 1;
      // fill columns
      while (visibleOrderedEntityPathesIterator.hasNext())  {
        currentColumn = xAnchorPosition + x;
        currentRow = yAnchorPosition + y;
        EntityPath path = (EntityPath) visibleOrderedEntityPathesIterator.next();
        EntityToPresentationObjectConverter converter = getEntityToPresentationConverter(path); 
        PresentationObject presentation = converter.getPresentationObject(genericEntity, path, this, iwc);
        add(presentation, currentColumn, currentRow);
        // next column
        x++; 
      }
      // next row
      y++;
    }
    currentColumn = -1;
    currentRow = -1;
  }


	private void setColorForRow(int rowNumber) {
		boolean oddRow = ((rowNumber % 2) == 0);
		if (colorForOddRows != null && oddRow)
		  setRowColor(rowNumber, colorForOddRows);
		else if (colorForEvenRows != null && (! oddRow)) 
		  setRowColor(rowNumber, colorForEvenRows);
	}
  
  private void parseAndDoAction(IWContext iwc, String formerStateOfIterator, SetIterator setIterator)  {
    // event system
    EntityBrowserPS state = (EntityBrowserPS) getPresentationState((IWUserContext) iwc);
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
		if (state.isParameterSet(keySubmit))
		  parameter = state.getParameter(keySubmit);
		
		if (parameter != null && parameter.length() != 0)   
		  action = parameter;       
		else if (iwc.isParameterSet(keySubmit))  
		  action = iwc.getParameter(keySubmit);
		return action;
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
    Link goBackLink = new Link(resourceBundle.getLocalizedString("back","Back"));
    Link goForwardLink = new Link(resourceBundle.getLocalizedString("forward","Forward"));
    goBackLink.setFontStyle(FONT_STYLE_FOR_LINK);
    goForwardLink.setFontStyle(FONT_STYLE_FOR_LINK);
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
 
    Iterator iterator = getLinksToPage(formKey, iwc, resourceBundle, setIterator, currentStateOfIterator).iterator();
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
    while (number < size) {
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
      IWResourceBundle resourceBundle, 
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
      Link link = new Link(buffer.toString());
      link.setFontStyle(FONT_STYLE_FOR_LINK);
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
    // sometimes setting button is not desired
    if (!showSettingButton)
      return new Table();
    String settings = resourceBundle.getLocalizedString("Settings","Settings");
    Link link = new Link(settings);
    link.setWindowToOpen(EntityBrowserSettingsWindow.class);
    link.addParameter(EntityBrowserSettingsWindow.LEADING_ENTITY_NAME_KEY, leadingEntityName);
    EntityBrowserSettingsWindow.setParameters(link, entityNames, defaultColumns.values(), defaultNumberOfRowsPerPage );
        
    link.setAsImageButton(true);
    Table table = new Table();
    table.add(link);
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
        object = object.getParentObject();
      }
      while (id == 0 && object != null);
      myId = (id != 0) ? Integer.toString(id) : getCompoundId();
    }
    return myId;
  }
  
  private void setErrorContent(IWResourceBundle resourceBundle)  {
    // show nothing
    //// String message = resourceBundle.getLocalizedString("Blank table", "Blank table");
    //// add(message);
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
  
  private int getNumberOfRowsPerPage(MultiEntityPropertyHandler multiPropertyHandler) {
    int rows = multiPropertyHandler.getNumberOfRowsPerPage();
    if (rows == EntityPropertyHandler.NUMBER_OF_ROWS_PER_PAGE_NOT_SET)
      return defaultNumberOfRowsPerPage;
    return rows;
  }
  
  
  public EntityToPresentationObjectConverter getDefaultConverter()  {
    if (defaultConverter == null) 
      defaultConverter = 
      new EntityToPresentationObjectConverter() {
            
        public PresentationObject getPresentationObject(Object genericEntity, EntityPath path, EntityBrowser browser, IWContext iwc)  {
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
	public void setShowSettingButton(boolean showSettingButton) {
		this.showSettingButton = showSettingButton;
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
	 * Returns the currentColumn.
   * Returns -1 if the browser is not drawing a cell at the moment.
	 * @return int
	 */
	public int getCurrentColumn() {
		return currentColumn;
	}

	/**
	 * Returns the currentRow.
   * Returns -1 if the browser is not drawing a cell at the moment.
	 * @return int
	 */
	public int getCurrentRow() {
		return currentRow;
	}

	/**
	 * Sets the pageLimitForShowingBottomNavigation.
	 * @param pageLimitForShowingBottomNavigation The pageLimitForShowingBottomNavigation to set
	 */
	public void setPageLimitForShowingBottomNavigation(int pageLimitForShowingBottomNavigation) {
		this.pageLimitForShowingBottomNavigation = pageLimitForShowingBottomNavigation;
	}

  public void setShowNavigation(boolean showHeaderNavigation, boolean showBottomNavigation) {
    this.showHeaderNavigation = showHeaderNavigation;
    this.showBottomNavigation = showBottomNavigation;
  }
}
