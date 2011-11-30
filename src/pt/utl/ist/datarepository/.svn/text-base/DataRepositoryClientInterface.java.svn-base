package pt.utl.ist.datarepository;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.util.JDOMUtil;

import pt.utl.ist.datarepository.utils.PasswordEncryptor;

public class DataRepositoryClientInterface extends Interface_Client {

	private String backEndURI;
	
	private String sessionHandle;
	private String dataRepUser = "datarepository";
	private String dataRepPass = "datarepositorypass";
	
	private static Logger _log = Logger.getLogger(DataRepositoryClientInterface.class);
	
	public DataRepositoryClientInterface(String backEndURI) {
		this.backEndURI = backEndURI;
	}
	
	// notify info update
	public boolean notifyDataUpdate(String dataModelURI, String dataModelInstanceID, String elementURI) throws IOException {
		if(connected()) {
			Map<String, String> params = prepareParamMap("notifyDataUpdate", this.sessionHandle);
			params.put("dataModelURI", dataModelURI);
			params.put("dataModelInstanceID", dataModelInstanceID);
			params.put("elementURI", elementURI);
			String result = executePost(this.backEndURI, params);
			if(successful(result)) {
				return true;
			}
		}
		return false;
	}


	////// INTERNAL METHODS //////
	protected boolean connected() {
        try {
            // if not connected
             if ((this.sessionHandle == null) || (!checkConnection()))
                this.sessionHandle = connect();
        }
        catch (IOException ioe) {
             _log.error("Exception attempting to connect to engine", ioe);
        }
        if (!successful(this.sessionHandle)) {
            _log.error(JDOMUtil.strip(this.sessionHandle));
        }
        return (successful(this.sessionHandle)) ;
    }
    
    protected boolean checkConnection() throws IOException {
    	Map<String, String> params = prepareParamMap("checkConnection", this.sessionHandle);
    	String msg = executePost(backEndURI, params);
    	return successful(msg);
	}
    
    protected String connect() throws IOException {
    	Map<String, String> params = prepareParamMap("connect", null);
    	params.put("userID", this.dataRepUser);
    	params.put("password", PasswordEncryptor.encrypt(this.dataRepPass, null));
    	return executePost(this.backEndURI, params);
    }
}
