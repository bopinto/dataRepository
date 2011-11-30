package pt.utl.ist.datarepository.persistency.datamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.collection.UnmodifiableCollection;
import org.hibernate.mapping.Array;

public class DataMetaModel {
	
	private String dataModelURI; 
	private String activityModelURI;
	private String goalModelURI;
	
	private int instanceCounter = 0;
	
	private HashMap<String, MetaEntity> entities = new HashMap<String, MetaEntity>();
	private HashMap<String, MetaAttribute> attributes = new HashMap<String, MetaAttribute>();
	private ArrayList<MetaRelation> relations = new ArrayList<MetaRelation>();
	
	private HashMap<String, DataModel> dataModels = new HashMap<String, DataModel>();
	
	private HashMap<String, ParameterMapping> parameterMappings = new HashMap<String, ParameterMapping>(); 
	
	public DataMetaModel(String uri) {
		this.dataModelURI = uri;
	}
	
	public DataModel generateDataModel() {
		DataModel newDataModel = new DataModel(this.dataModelURI, "" + instanceCounter++);
 
		Collection<MetaEntity> metaEnts = this.entities.values();
		ArrayList<MetaEntity> metaEntsArray = new ArrayList<MetaEntity>(metaEnts);
		
		// add entities (and attributes)
		for (MetaEntity metaEntity : metaEntsArray) {
			newDataModel.addEntity(metaEntity.generateEntity());
		}
		
		// add relations
		for (MetaRelation metaRelation : this.relations) {
			Entity entOne = newDataModel.getEntity(metaRelation.getEntityOne().getName());
			Entity entTwo = newDataModel.getEntity(metaRelation.getEntityTwo().getName());
			newDataModel.addRelation(metaRelation.generateRelation(entOne, entTwo));
		}
		
		this.dataModels.put(newDataModel.getInstanceID(), newDataModel);
		return newDataModel;
	}
	
	public String getActivityModelURI() { return this.activityModelURI; }
	public void setActivityModelURI(String uri) { this.activityModelURI = uri; }
	
	public String getGoalModelURI() { return this.goalModelURI; }
	public void setGoalModelURI(String uri) { this.goalModelURI = uri; }
	
	public HashMap<String, ParameterMapping> getParameterMappings() { return this.parameterMappings; }
	public void addParameterMapping(ParameterMapping param) { this.parameterMappings.put(param.getParameterName(), param); }
	
	public DataModel getDataModel(String instanceID) {
		return this.dataModels.get(instanceID);
	}
	public ArrayList<DataModel> getInstances() {
		Collection<DataModel> instances = this.dataModels.values();
		ArrayList<DataModel> instancesArr = new ArrayList<DataModel>(instances);
		return instancesArr;
	}
	
	public void addEntity(MetaEntity ent) {
		this.entities.put(ent.getName(), ent);
		for (MetaAttribute att : ent.getAttributes()) {
			addAttribute(att);
		}
	}
	
	public MetaEntity getEntity(String entityName) {
		return this.entities.get(entityName);
	}
	
	public Map<String, MetaEntity> getEntities() {
		return Collections.unmodifiableMap(this.entities);
	}
	public List<MetaRelation> getRelations() {
		return Collections.unmodifiableList(this.relations);
	}
	
	public MetaEntity getEntityByID(String id) {
		Collection<MetaEntity> entities = this.entities.values();
		ArrayList<MetaEntity> entityArr = new ArrayList<MetaEntity>(entities);
		
		for (MetaEntity metaEntity : entityArr) {
			if(metaEntity.getID().equals(id)) {
				return metaEntity;
			}
		}
		return null;
	}
	
	public MetaAttribute getAttribute(String attributeName) {
		return this.attributes.get(attributeName);
	}
	
	public void addAttribute(MetaAttribute att) {
		this.attributes.put(att.getName(), att);
	}
	
	public void addRelation(MetaRelation rel) {
		this.relations.add(rel);
	}
	
	public String getURI() { return this.dataModelURI; }

}
