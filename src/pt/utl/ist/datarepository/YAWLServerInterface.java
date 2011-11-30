package pt.utl.ist.datarepository;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import pt.utl.ist.datarepository.utils.ServletUtils;
import pt.utl.ist.datarepository.utils.StringUtils;

@SuppressWarnings("serial")
public class YAWLServerInterface extends HttpServlet {
	
	private static Logger _log = Logger.getLogger(DataRepositoryServerInterface.class);
	
	private ArrayList<ActiveCase> activeCases = new ArrayList<ActiveCase>();
	
	private static YAWLServerInterface instance;
	
	public static YAWLServerInterface getInstance() {
		if(instance == null) {
			instance = new YAWLServerInterface();
		}
		 return instance;
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);                       // all gets redirected as posts
    }
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OutputStreamWriter outputWriter = ServletUtils.prepareResponse(response);
        StringBuilder output = new StringBuilder();
        output.append("<response>");
        output.append(processPostQuery(request));
        output.append("</response>");
        ServletUtils.finalizeResponse(outputWriter, output);
    }
	
	private String processPostQuery(HttpServletRequest request) {
        StringBuilder msg = new StringBuilder();
        String sessionHandle = request.getParameter("sessionHandle");
        String action = request.getParameter("action");
        String userID = request.getParameter("userID");
        String password = request.getParameter("password");

        try {
        	if (action != null) { // connect
        		if("connect".equals(action)) {
        			int interval = request.getSession().getMaxInactiveInterval();
        			msg.append(DataRepository.get().connect(userID, password, interval));
        		}
        		else if("checkConnection".equals(action)) { // checkConnection
        			msg.append(DataRepository.get().checkConnection(sessionHandle));
        		}
        		else if("setElement".equals(action)) {   //exclusive for yawl
                	String paramName = request.getParameter("paramName");
                	String elementInXML = request.getParameter("element");
                	
                	ActiveCase activeCase = getCaseFromParam(paramName);
                	
                	if(activeCase == null) {
                		msg.append("<failure>There is no case with the given parameter</failure>");
                	} else {
                		msg.append(DataRepository.get().setElement(activeCase.yawlCaseURI, activeCase.yawlCaseInstanceID, elementInXML));
                	}
                }
                else if("getElementInfo".equals(action)) { // exclusive for yawl
                	String yawlCaseURI = request.getParameter("yawlCaseURI");
                	String elementURI = request.getParameter("elemURI");
                	String yawlCaseInstanceID = request.getParameter("instanceID");
                	String elementInfo = DataRepository.get().getElementInfo(yawlCaseURI, yawlCaseInstanceID, elementURI);
                	msg.append(elementInfo);
                	
                }
                else if("registerYAWLActiveCase".equals(action)) {
                	String caseInstanceID = request.getParameter("caseInstanceID");
                	String caseURI = request.getParameter("caseURI");
                	String caseUUID = request.getParameter("caseUUID");
                	ArrayList<String> varList = xmlToArrayList(request.getParameter("varList"));

                	//create a buffer here
                	ActiveCase activeCase = new ActiveCase();
            		activeCase.yawlCaseInstanceID = caseInstanceID;
            		activeCase.yawlCaseURI = caseURI;
            		activeCase.yawlCaseUUID = caseUUID;
            		activeCase.parameters = varList;
            		this.activeCases.add(activeCase);
            		msg.append("<success/>");
                }
                else if("unregisterYAWLActiveCase".equals(action)) {
                	String caseInstanceID = request.getParameter("caseInstanceID");
                	String caseUUID = request.getParameter("caseUUID");
                	
                	ActiveCase activeCase = null;
            		for (ActiveCase actCase : this.activeCases) {
            			if(actCase.yawlCaseInstanceID.equals(caseInstanceID) &&
            					actCase.yawlCaseUUID.equals(caseUUID)) {
            				activeCase = actCase;
            			}
            		}
            		if(activeCase == null) {
            			msg.append("<failure>There is no case with the given parameter</failure>");
            		} else {
            			this.activeCases.remove(activeCase);
            			msg.append("<success/>");
            		}
                }
                else {
                	msg.append("<failure>Invalid action</failure>");
                }
        	}

        }
        catch (Exception e) {
            _log.error("Exception in Interface B with action: " + action, e);
        }
        if (msg.length() == 0) {
            msg.append("<failure><reason>Invalid action or exception was thrown." +
                       "</reason></failure>");
        }
        return msg.toString();
	}
	
	private ArrayList<String> xmlToArrayList(String listInXML) {
		ArrayList<String> array = new ArrayList<String>();
		
		Document doc = StringUtils.stringToDoc(listInXML);
		Element listXML = doc.getRootElement();
		for (Object child : listXML.getChildren()) {
			Element item = (Element) child;
			array.add(item.getText());
		}
		
		return array;
	}
	
	private ActiveCase getCaseFromParam(String paramName, String caseURI, String caseID) {
		ActiveCase activeCase = null;
		
		for (ActiveCase actCase : this.activeCases) {
			if(caseURI.equals(actCase.yawlCaseURI) && 
					caseID.equals(actCase.yawlCaseInstanceID)) {
				for(String activeParam : actCase.parameters) {
					if(activeParam.equals(paramName)) {
						activeCase = actCase;
					}
				}
			}
		}
		return activeCase;
	}
	
	private ActiveCase getCaseFromParam(String paramName) {
		ActiveCase activeCase = null;
		
		for (ActiveCase actCase : this.activeCases) {
			for(String activeParam : actCase.parameters) {
				if(activeParam.equals(paramName)) {
					activeCase = actCase;
				}
			}
		}
		return activeCase;
	}

}
