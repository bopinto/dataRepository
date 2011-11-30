package pt.utl.ist.datarepository.persistency.datamodel;

import java.util.ArrayList;

public class ParameterMapping {
	
	private String parameterName;
	private ArrayList<String> mappings = new ArrayList<String>();
	
	public ParameterMapping(String name) {
		this.parameterName = name;
	}
	
	////// GETTERS //////
	public String getParameterName() {
		return this.parameterName;
	}
	
	public ArrayList<String> getMappings() {
		return this.mappings;
	}
	///////////////////
	
	/////// SETTERS ///////
	public void addMapping(String map) {
		this.mappings.add(map);
	}

}
