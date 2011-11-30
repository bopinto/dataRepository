package pt.utl.ist.datarepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import pt.utl.ist.datarepository.persistency.datamodel.Attribute;
import pt.utl.ist.datarepository.persistency.datamodel.DataMetaModel;
import pt.utl.ist.datarepository.persistency.datamodel.DataModel;
import pt.utl.ist.datarepository.persistency.datamodel.DataModelFactory;
import pt.utl.ist.datarepository.persistency.datamodel.Entity;
import pt.utl.ist.datarepository.persistency.datamodel.MetaEntity;
import pt.utl.ist.datarepository.persistency.datamodel.ParameterMapping;
import pt.utl.ist.datarepository.utils.PasswordEncryptor;
import pt.utl.ist.datarepository.utils.StringUtils;

public class DataRepository {

	private static DataRepository instance = null;
	private static Logger _log = Logger.getLogger(DataRepository.class);
	
	private SessionManager sessionManager = new SessionManager();
	private HashMap<String, DataClient> registeredUsers = new HashMap<String, DataClient>();
	
    private final String OPEN_FAILURE = "<failure><reason>";
    private final String CLOSE_FAILURE = "</reason></failure>";
    private final String SUCCESS = "<success/>";
    
    private HashMap<String, DataMetaModel> dataModels = new HashMap<String, DataMetaModel>();
    
    private DataRepositoryClientInterface clientInterface = new DataRepositoryClientInterface("http://localhost:8080/blendedWorkflowService/ce");
	
	private DataRepository() {
		// init the registered users
		String encriptedpass = PasswordEncryptor.encrypt("bwpass", null);
		DataClient bw = new DataClient("bw", encriptedpass);
		
		String encriptedpass2 = PasswordEncryptor.encrypt("yawlpass", null);
		DataClient yawl = new DataClient("yawl", encriptedpass2);
		
		String encriptedpass3 = PasswordEncryptor.encrypt("goalenginepass", null);
		DataClient goalEngine = new DataClient("goalengine", encriptedpass3);
		
		this.registeredUsers.put(bw.getUsername(), bw);
		this.registeredUsers.put(yawl.getUsername(), yawl);
		this.registeredUsers.put(goalEngine.getUsername(), goalEngine);
		
		
	}
	
	public static DataRepository get() {
		if(instance == null) {
			instance = new DataRepository();
		}
		return instance;
	}
	
	public SessionManager getSessionManager() { return this.sessionManager; }
	public DataMetaModel getDataMetaModel(String dataModelURI) { return this.dataModels.get(dataModelURI); }
	
	public void notifyDataUpdate(String dataModelURI, String dataModelInstanceID, String elementURI) {
		try {
			clientInterface.notifyDataUpdate(dataModelURI, dataModelInstanceID, elementURI);
		} catch (IOException e) {
			_log.error("Could not notity of data change (IOException)");
		}
	}
	
	
	public void loadModel(String XMLmodel) {
		DataModelFactory factory = new DataModelFactory();
		DataMetaModel newDataModel = factory.parseXMLDataModel(XMLmodel);
		if(newDataModel != null) {
			this.dataModels.put(newDataModel.getURI(), newDataModel);
			_log.info("New data model successfully parsed and loaded.");
		} else {
			_log.error("Could not parse the data model.");
		}
		
	}
	
    // connect
    public String connect(String username, String password, long timeout) {
    	if(username == null || password == null) {
			return failMsg("Please submit user and password");
		}
    	
    	String result = validateLogin(username, password);
    	if(result != null) {
    		return failMsg(result);
    	}
    	
    	DataClient client = this.registeredUsers.get(username);
    	result = this.sessionManager.createNewSession(client, timeout);
    	
    	return result;
    }
    
    public String checkConnection(String sessionHandle) {
    	return this.sessionManager.checkConnection(sessionHandle) ? SUCCESS :
    		OPEN_FAILURE + "Invalid or expired session" + CLOSE_FAILURE;
    }
    
    public String createModelInstance(String dataModelURI, String activityModelURI, 
    		String activityModelInstanceID, String goalModelURI, String goalModelInstanceID) {
    	_log.info("Request to generate a new instance of the data model");
    	
    	if(!inputNotNull(dataModelURI)) {
    		return OPEN_FAILURE + "Full info not provided" + CLOSE_FAILURE;
    	}
    	
    	// get the data model
    	DataMetaModel dataMetaModel = this.dataModels.get(dataModelURI);
    	if(dataMetaModel == null) {
    		_log.error("Fail to create data model instance: Requested data model does not exist");
    		return OPEN_FAILURE + "Requested data model does not exist" + CLOSE_FAILURE;
    	}
    	dataMetaModel.setActivityModelURI(activityModelURI);
    	dataMetaModel.setGoalModelURI(goalModelURI);
    	
    	// create instance
    	DataModel dataModel = dataMetaModel.generateDataModel();
    	dataModel.setActivityCaseInstanceID(activityModelInstanceID);
    	dataModel.setGoalCaseInstanceID(goalModelInstanceID);
    	
    	_log.info("Data Model Instance generated successfuly");
    	return dataModel.getInstanceID();
    }
    
    public String getElementData(String dataModelURI, String elementURI, String instanceID) {
    	_log.info("Request to get data from element " + elementURI);
    	
    	if(!inputNotNull(dataModelURI, elementURI, instanceID)) {
    		return OPEN_FAILURE + "Full info not provided" + CLOSE_FAILURE;
    	}
    	
    	// get the data model
    	DataMetaModel dataMetaModel = this.dataModels.get(dataModelURI);
    	if(dataMetaModel == null) {
    		_log.error("Fail to get data element: Requested data model does not exist");
    		return OPEN_FAILURE + "Requested data model does not exist" + CLOSE_FAILURE;
    	}
    	
    	DataModel dataModel = dataMetaModel.getDataModel(instanceID);
    	if(dataModel == null) {
    		_log.error("Fail to get data element: Requested data model does not exist");
    		return OPEN_FAILURE + "Requested data model does not exist" + CLOSE_FAILURE;
    	}
    	    	
    	String elemXML = dataModel.getElementAsXMLString(elementURI);
    	if(elemXML == null) {
    		return failMsg("Invalid element");
    	}
    	return elemXML;
    }
    
    public String getElementInfo(String yawlCaseURI, String yawlCaseInstaceID, String element) { //TODO test
    	
    	if(!inputNotNull(yawlCaseURI, yawlCaseInstaceID, element)) {
    		return failMsg("Please provide all the required data");
    	}
    	DataMetaModel dataMetaModel = this.getMetaModelByActvitityCaseURI(yawlCaseURI);
    	
    	// get the datamodel
    	DataModel dataModel = getDataModelByActivityInstanceID(dataMetaModel, yawlCaseInstaceID);
    	
    	if(dataModel == null) {
    		return failMsg("Could not find the data model instance for the given activity instance ID");
    	}
    	
    	// get the parameter mapping
    	ParameterMapping map = dataMetaModel.getParameterMappings().get(element);
    	
    	// get the element values
    	Element elementInXML = new Element(element);
    	for (String param : map.getMappings()) {
			Document doc = dataModel.getElementAsXML(param);
			Element root = doc.getRootElement(); // Element
			if(root.getChildren("Entity").size() > 0) {
				// it is an entity
				Element entity = (Element) root.getChild("Entity"); //FIXME this only gets the first child
				for (Object child : entity.getChildren("Attribute")) {
					Element attribute = (Element) child;
					String attributeFullName = attribute.getChildText("Name");
					String attributeName = attributeFullName.split("\\.")[1];
					attributeName = attributeName.replace(" ", "");
					Element att = new Element(attributeName);
					att.setText(attribute.getChildText("Value"));
					elementInXML.addContent(att);
				}
			} else {
				// it is an attribute
				Element attribute = (Element) root.getChild("Attribute");
				String attributeValue = attribute.getChildText("Value");
				if(map.getMappings().size() == 1) {
					//it is a single mapping
					elementInXML.setText(attributeValue);
				} else {
					String attributeFullName = attribute.getChildText("Name");
					String attributeName = attributeFullName.split("\\.")[1];
					attributeName = attributeName.replace(" ", "");
					Element att = new Element(attributeName);
					att.setText(attributeValue);
					elementInXML.addContent(att);
				}
			}
		}
    	
    	// return the element
    	return new XMLOutputter().outputString(elementInXML);
    }
    
    public String getParameterMapping(String dataModelURI, String elementURI) {
    	_log.info("Request for parameter mapping " + elementURI);
    	
    	if(!inputNotNull(dataModelURI, elementURI)) {
    		return OPEN_FAILURE + "Full info not provided" + CLOSE_FAILURE; 
    	}
    	
    	DataMetaModel dataMetaModel = this.dataModels.get(dataModelURI);
    	if(dataMetaModel == null) {
    		_log.error("Fail to get data element: Requested data model does not exist");
    		return OPEN_FAILURE + "Requested data model does not exist" + CLOSE_FAILURE;
    	}
    	
    	// get the parameter mapping
    	String[] element = elementURI.split("\\.");
    	
    	ParameterMapping map = null;
    	if(element[0] == null || "null".equals(element[0])) {
    		map = dataMetaModel.getParameterMappings().get(element[1]);
    	} else {
    		map = dataMetaModel.getParameterMappings().get(element[0]);
    	}
    	
    	if(map == null) {
    		return failMsg("No such parameter");
    	}

    	if(map.getMappings().size() == 1) {
    		// see if it is an entity or an entity.attribute
    		String[] mappingName = map.getMappings().get(0).split("\\."); 
    		if(mappingName.length == 2) {
    			return map.getMappings().get(0);
    		} else {
    			//it is an entity
    			MetaEntity entity = dataMetaModel.getEntity(mappingName[0]);
    			if(entity != null && element.length == 2 &&
    					entity.getAttribute(element[1]) != null) {
    				return elementURI;
    			}
    			return failMsg("Please provide a valid entity and attribute");
    		}
    	} else {   
    		for (String mapping : map.getMappings()) {
    			String[] mapSplitted = mapping.split("\\.");
    			if(mapSplitted[1].equals(element[1])) {
    				return mapping;
    			} else {
    				String attributeName = mapSplitted[1].replace(" ", "");
    				if(attributeName.equals(element[1])) {
    					return mapping;
    				}
    			}
    		} 		
    	}
    	return failMsg("No such parameter");
    }
    
    public String submitData(String dataModelURI, String instanceID, 
    		String dataName, String dataType, 
    		String value, String restrictions, boolean isSkipped) {    	
    	_log.info("Request to submit data " + dataName);
    	
    	if(!inputNotNull(dataModelURI, instanceID, dataName, 
    			dataType, value)) {
    		return OPEN_FAILURE + "Full info not provided" + CLOSE_FAILURE;
    	}
    	
    	// get the data model
    	DataMetaModel dataMetaModel = this.dataModels.get(dataModelURI);
    	if(dataMetaModel == null) {
    		_log.error("Fail to get data element: Requested data model does not exist");
    		return OPEN_FAILURE + "Requested data model does not exist" + CLOSE_FAILURE;
    	}
    	
    	DataModel dataModel = dataMetaModel.getDataModel(instanceID);
    	if(dataModel == null) {
    		_log.error("Fail to get data element: Requested data model does not exist");
    		return OPEN_FAILURE + "Requested data model does not exist" + CLOSE_FAILURE;
    	}
    	
    	// validate element uri
    	// validate element
    	if(validateAttributeURI(dataName)) {
    		if(!isSkipped) {
    			dataModel.insertAttributeValue(dataName, value);
    		} else {
    			dataModel.skipAttribute(dataName);
    		}
    	} else {
    		return failMsg("Failed to update attribute value");
    	}
    	_log.info("Succeeded in update data value");
    	return SUCCESS;
    }
    
    public String skipData(String dataModelURI, String dataModelInstanceID, String elementURI) {
    	_log.info("Request to skip data " + elementURI);
    	
    	if(!inputNotNull(dataModelURI, dataModelInstanceID, elementURI)) {
    		return failMsg("Did not provide all the information");
    	}
    	
    	// get the data model
    	DataMetaModel dataMetaModel = this.dataModels.get(dataModelURI);
    	if(dataMetaModel == null) {
    		_log.error("Fail to get data element: Requested data model does not exist");
    		return OPEN_FAILURE + "Requested data model does not exist" + CLOSE_FAILURE;
    	}
    	
    	DataModel dataModel = dataMetaModel.getDataModel(dataModelInstanceID);
    	if(dataModel == null) {
    		_log.error("Fail to get data element: Requested data model does not exist");
    		return OPEN_FAILURE + "Requested data model does not exist" + CLOSE_FAILURE;
    	}
    	
    	dataModel.skipElement(elementURI);
    	
    	return SUCCESS;
    }
    
    public String setElement(String yawlCaseURI, String yawlCaseInstanceID, String elementInXML) { //TODO test
    	_log.info("Request to submit data from activity case " + yawlCaseInstanceID + ":" + yawlCaseURI);
    	
    	if(!inputNotNull(yawlCaseURI, yawlCaseInstanceID, elementInXML)) {
    		return failMsg("Full info not provided.");
    	}
    	
    	DataMetaModel dataMetaModel = getMetaModelByActvitityCaseURI(yawlCaseURI);
    	if(dataMetaModel == null) {
    		return failMsg("Could not find the data model that corresponds to the given activity model.");
    	}
    	
    	DataModel dataModel = getDataModelByActivityInstanceID(dataMetaModel, yawlCaseInstanceID);
    	if(dataModel == null) {
    		return failMsg("Could not find the data model instance that corresponds to the given activity model instance.");
    	}
    	Document doc = StringUtils.stringToDoc(elementInXML);
    	Element root = doc.getRootElement();
    	
    	ParameterMapping paramMap = dataMetaModel.getParameterMappings().get(root.getName());
    	
    	if(paramMap == null) {
    		_log.error("Could not find the given parameter");
    		return failMsg("Could not find the given parameter");
    	}
    	
    	boolean errors = false;
    	for (String param : paramMap.getMappings()) { // for each parameter
    		if(param.split("\\.").length > 1) {  // if it is an attribute
    			//if the element does not have children
    			if(root.getChildren().size() == 0) {
    				// it must be only one mapping. Set the value
    				dataModel.insertAttributeValue(param, root.getText());
    				return SUCCESS;
    			}
    			String attributeName = param.split("\\.")[1];
    			Element att = (Element) root.getChild(attributeName);
    			if(att == null) {
    				attributeName = StringUtils.joinName(attributeName);
    			}
    			att = (Element) root.getChild(attributeName);
    			if(att == null) {
    				_log.error("Could not find the given attribute(" + attributeName + "). Continuing.");
    				errors = true;
    				continue;
    			}
    			// update the attribute value
    			dataModel.insertAttributeValue(param, att.getValue());
    		} else {  // if it is an entity
    			// get the entity
    			Entity entity = dataModel.getEntity(param);
    			// get the attribute
    			for (Object child : root.getChildren()) {
    				Element attXML = (Element) child;
    				Attribute att = entity.getAttribute(entity.getName() + "." + attXML.getName());
    				if(att == null) {
    					String attName = StringUtils.splitName(attXML.getName());
    					att = entity.getAttribute(entity.getName() + "." + attName);
    				}
    				if(att == null) {
    					_log.error("Could not find the given attribute (" + attXML.getName() + "). Continuing.");
    					errors = true;
    					continue;
    				}
    				// update the attribute value
    				att.updateInstanceValue(attXML.getValue());
    			}
    		}
    	
    	}
    	
    	return (!errors) ? SUCCESS : failMsg("There were some errors during the update.");
    }
    
    public String addEntity(String dataModelURI, String instanceID, 
    		String dataName, String relation) {
    	_log.info("Request to add data the data model. Data " + dataName);
    	
    	if(!inputNotNull(instanceID, dataModelURI, dataName, relation)) {
    		return OPEN_FAILURE + "Full info not provided" + CLOSE_FAILURE;
    	}
    	
    	// get the data model
    	DataMetaModel dataMetaModel = this.dataModels.get(dataModelURI);
    	if(dataMetaModel == null) {
    		_log.error("Fail to get data element: Requested data model does not exist");
    		return OPEN_FAILURE + "Requested data model does not exist" + CLOSE_FAILURE;
    	}
    	
    	DataModel dataModel = dataMetaModel.getDataModel(instanceID);
    	if(dataModel == null) {
    		_log.error("Fail to get data element: Requested data model does not exist");
    		return OPEN_FAILURE + "Requested data model does not exist" + CLOSE_FAILURE;
    	}
  
    	AddRelationMessage relationMessage = new AddRelationMessage().fromXMLString(relation);
    	
    	if(relationMessage == null) {
    		return failMsg("Could not parse the relation");
    	}
    	
     	DataModelFactory factory = new DataModelFactory();
     	if(!factory.addEntityToDataModel(dataModel, dataName, relationMessage)) {
     		return failMsg("Could not add the entity " + dataName);
     	}
     		
    	return SUCCESS;
    }
    
    public String addAttribute(String dataModelURI, String instanceID, String attributeURI,
    		String dataType, String isKey) {
    	_log.info("Request to add data the data model. Data " + attributeURI);
    	
    	if(!inputNotNull(instanceID, dataModelURI, attributeURI, dataType, isKey)) {
    		return OPEN_FAILURE + "Full info not provided" + CLOSE_FAILURE;
    	}
    	
    	// get the data model
    	DataMetaModel dataMetaModel = this.dataModels.get(dataModelURI);
    	if(dataMetaModel == null) {
    		_log.error("Fail to get data element: Requested data model does not exist");
    		return OPEN_FAILURE + "Requested data model does not exist" + CLOSE_FAILURE;
    	}
    	
    	DataModel dataModel = dataMetaModel.getDataModel(instanceID);
    	if(dataModel == null) {
    		_log.error("Fail to get data element: Requested data model does not exist");
    		return OPEN_FAILURE + "Requested data model does not exist" + CLOSE_FAILURE;
    	}
    	
    	if(attributeURI.split("\\.").length != 2) {
    		return failMsg("Attribute name not complete");
    	}
    	
    	DataModelFactory factory = new DataModelFactory();
    	if(!factory.addAttributeToEntity(dataModel, attributeURI, dataType, Boolean.parseBoolean(isKey))) {
    		return failMsg("Failed to add attribute " + attributeURI + " to Entity");
    	}
    	return SUCCESS;
    }
    
    public String getDataModelInXML(String dataModelURI, String instanceID) {
    	_log.info("Request of data model");
    	
    	if(!inputNotNull(dataModelURI, instanceID)) {
    		return OPEN_FAILURE + "Full info not provided" + CLOSE_FAILURE;
    	}
    	
    	// get the data model
    	DataMetaModel dataMetaModel = this.dataModels.get(dataModelURI);
    	if(dataMetaModel == null) {
    		_log.error("Fail to get data element: Requested data model does not exist");
    		return OPEN_FAILURE + "Requested data model does not exist" + CLOSE_FAILURE;
    	}
    	
    	DataModel dataModel = dataMetaModel.getDataModel(instanceID);
    	if(dataModel == null) {
    		_log.error("Fail to get data element: Requested data model does not exist");
    		return OPEN_FAILURE + "Requested data model does not exist" + CLOSE_FAILURE;
    	}
    	
    	String dataModelXML = dataModel.toXMLString();
    	
    	if(dataModelXML == null) {
    		_log.error("Could not obtain data model XML");
    		return failMsg("Could not obtain data model XML");
    	}
    	return dataModelXML;
    }

    
	///// PRIVATE METHODS /////
    protected String validateLogin(String username, String password) {
    	DataClient user = this.registeredUsers.get(username);
    	if(user == null) {
    		return "Invalid user";
    	}
    	if(!user.getPassword().equals(password)) {
    		return "Invalid password";
    	}
    	return null;
    }
    
    protected String failMsg(String msg) {
        return OPEN_FAILURE + msg + CLOSE_FAILURE ;
    }
    
    protected boolean inputNotNull(String...strings) {
    	for (String string : strings) {
			if(string == null) {
				return false;
			}
		}
    	return true;
    }
    
    protected boolean validateAttributeURI(String elementURI) {
    	String[] elementArray = elementURI.split("\\.");
    	return (elementArray[0] != null && elementArray[1] != null);
    }
    
    protected DataMetaModel getMetaModelByActvitityCaseURI(String caseURI) {
    	DataMetaModel dataModel = null;
    	Collection<DataMetaModel> metaModels = this.dataModels.values();
    	ArrayList<DataMetaModel> metaModelsArr = new ArrayList<DataMetaModel>(metaModels);
    	for (DataMetaModel dmm : metaModelsArr) {
			if(dmm.getActivityModelURI().equals(caseURI)) {
				dataModel = dmm;
				break;
			}
		}
    	if(dataModel == null) {
    		_log.error("Could not find the given data model");
    	}
    	return dataModel;
    }
    
    private DataModel getDataModelByActivityInstanceID(DataMetaModel metaModel, String activityInstanceID) {
    	DataModel dataModel = null;
    	for (DataModel instance : metaModel.getInstances()) {
    		if(instance.getActivityCaseInstanceID().equals(activityInstanceID)) {
    			dataModel = instance;
    		}
    	}
    	return dataModel;
    }
    
    private DataModel getDataModel(String dataModelURI, String dataModelInstanceID) {
    	DataMetaModel dataMetaModel = this.dataModels.get(dataModelURI);
    	if(dataMetaModel == null) {
    		_log.error("Fail to get data element: Requested data model does not exist");
    		return null;
    	}
    	
    	DataModel dataModel = dataMetaModel.getDataModel(dataModelInstanceID);
    	if(dataModel == null) {
    		_log.error("Fail to get data element: Requested data model does not exist");
    		return null;
    	}
    	
    	return dataModel;
    }
}
