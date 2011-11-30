package pt.utl.ist.datarepository.persistency.datamodel;

import org.jdom.Element;

public abstract class Data {
	
	public abstract void broadcastChange();

	public abstract String toXMLString();
	
	public abstract Element toXMLElement();
}
