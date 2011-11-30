package pt.utl.ist.datarepository.persistency.datamodel;

import java.util.ArrayList;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

public class EntityInstance extends Data {

	private ArrayList<AttributeInstance> attributeInstances = new ArrayList<AttributeInstance>();
	private ArrayList<RelationInstance> relations = new ArrayList<RelationInstance>();
	private Entity parent;
	
	private DataModelInstance dataModelInstance = null;
	
	private boolean isSkipped = false;
	private boolean isDefined = false;
	
	public EntityInstance(DataModelInstance modelInstance, Entity parent, ArrayList<AttributeInstance> arrayList, int id) {
		this.parent = parent;
		this.attributeInstances.addAll(arrayList);
		this.dataModelInstance = modelInstance;
	}
	
	public DataModelInstance getDataModelInstance() { return this.dataModelInstance; }
	
	public ArrayList<AttributeInstance> getAttributeInstaces() {
		return this.attributeInstances;
	}
	
	public void setAttributeInstances(ArrayList<AttributeInstance> instances) {
		this.attributeInstances = instances;
	}
	
	public void addAttributeInstance(AttributeInstance instance) {
		this.attributeInstances.add(instance);
	}
	
	public void addRelation(RelationInstance rel) {
		this.relations.add(rel);
	}

	public boolean isSkipped() {
		return this.isSkipped;
	}
	
	public void skip() {
		isSkipped = true;
		for(AttributeInstance attInstance : this.attributeInstances) {
			attInstance.skip();
		}
		broadcastChange();
	}
	
	public boolean isDefined() {
		return this.isDefined;
	}
	
	public void update() {
		if(this.isDefined) {
			return;
		}
		
		this.isDefined = allKeyDataDefined();
		
		if(isDefined) {
			isSkipped = false;
			broadcastChange();
			// create new empty instance
			if(!parent.isSingleInstance()) {
				parent.createEmptyInstance(this.dataModelInstance);
			}
			return;
		}
		if(allDataSkipped() && !isSkipped) {
			skip();
		}
	}
	
	private boolean allDataSkipped() {
		// check if there are attributes that are not skipped
		for (AttributeInstance attribute : this.attributeInstances) {
			if(!attribute.isSkipped()) {
				return false;
			}
		}
		
		// check if there are entities that are not skipped
		for(RelationInstance relation : this.relations) {
			if(isOtherInRelationKey(relation) && !getOtherInRelation(relation).isSkipped) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean allKeyDataDefined() {
		boolean defined;
		// check if the key attributes are defined
		for (AttributeInstance attribute : this.attributeInstances) {
			if(attribute.isKeyAttribute() && !attribute.isDefined()) {
				defined = false;
				return defined;
			}
		}
		
		// check if the key entities are defined
		defined = this.isKeyEntitiesDefined();
		return defined;
	}
	
	private boolean isKeyEntitiesDefined() {
		for(RelationInstance relation : this.relations) {
			if(!getOtherInRelation(relation).isDefined && isOtherInRelationKey(relation)) {
				return false;
			}
		}
		
		return true;
	}
	
	public String getName() {
		return this.parent.getName();
	}
	
	public void broadcastChange() {
		this.dataModelInstance.broadcastChange(getName());
		for(RelationInstance relation : this.relations) {
			getOtherInRelation(relation).update();
		}
	}
	
	public boolean hasKeyAttibutes() {
		return parent.hasKeyAttibutes();
	}
	
	public ArrayList<Data> getKeyData() {
		ArrayList<Data> data = new ArrayList<Data>();
		// add entities
		for (RelationInstance relation : this.relations) {
			if(isOtherInRelationKey(relation)) {
				data.add(getOtherInRelation(relation));
			}
		}
		//add attributes
		for (AttributeInstance attribute : this.attributeInstances) {
			if(attribute.isKeyAttribute()) {
				data.add(attribute);
			}
		}
		
		return data;
	}
	
	@Override
	public String toXMLString() {
		return new XMLOutputter().outputString(toXMLElement());
	}

	@Override
	public Element toXMLElement() {
		Element entity = new Element("Entity");
		entity.setAttribute("isSkipped", Boolean.toString(this.isSkipped));
		entity.setAttribute("isDefined", Boolean.toString(this.isDefined));
		entity.addContent(new Element("Name").setText(this.getName()));
		
		for (AttributeInstance attrInst : this.attributeInstances) {
			entity.addContent(attrInst.toXMLElement());
		}
		return entity;
	}
	
	
	///// INTERNAL METHODS /////
	protected EntityInstance getOtherInRelation(RelationInstance rel) {
		if(rel.getEntityOne().equals(this)) {
			return rel.getEntityTwo();
		} else {
			return rel.getEntityOne();
		}
	}
	
	protected boolean isOtherInRelationKey(RelationInstance rel) {
		if(rel.getEntityOne().equals(this)) {
			return rel.isTwoKeyEntity();
		} else {
			return rel.isOneKeyEntity();
		}
	}
	
}
