package pt.utl.ist.datarepository;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("serial")
public class SessionManager extends ConcurrentHashMap<String, DataSession>{

	public String createNewSession(DataClient client, long timeout) {
		DataSession dataSession = new DataSession(client, timeout);
		String handle = dataSession.getHandle();
		this.put(handle, dataSession);
		return handle;
	}
	
	public boolean checkConnection(String handle) {
		boolean result = false;
		if(handle != null) {
			DataSession dataSession = this.get(handle);
			if(dataSession != null) {
				dataSession.resetTimer();
				result = true;
			}
		}
		return result;
	}
}
