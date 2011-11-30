package pt.utl.ist.datarepository.persistency.datamodel;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import pt.utl.ist.datarepository.persistency.datamodel.MetaAttribute.Type;


public class AttributeInstance extends Data {

	private String value = null;
	private Attribute parent;
	private EntityInstance entity;
	private boolean isSkipped = false;
	
	private DataModelInstance dataModel = null;
	
	public AttributeInstance(Attribute parent, EntityInstance entity, DataModelInstance dataModel) {
		this.parent = parent;
		this.entity = entity;
		this.dataModel = dataModel;
	}
	
	public Attribute getParent() {
		return this.parent;
	}
	
	public void setValue(String val){
		if(val == null || "".equals(val) || "N/A".equals(val)) {
			return;
		}
		
		this.value = val;
		this.isSkipped = false;
		broadcastChange();
	}
	
	public String getValue() {
		if(this.value == null) {
			if(parent.getType() == Type.BOOLEAN) {
				return "false";
			}
			return "N/A";
		}
		return this.value;
	}
	
	public boolean isDefined() {
		if(this.value == null || "".equals(value)) {
			return false;
		}
		return true;
	}

	public boolean isSkipped() {
		return this.isSkipped;
	}
	
	public boolean isKeyAttribute() {
		return parent.isKeyAttribute();
	}
	
	public String getName() {
		return parent.getName();
	}
	
	public MetaAttribute.Type getType() {
		return parent.getType();
	}
	
	public void skip() {
		isSkipped = true;
		broadcastChange();
	}

	public void broadcastChange() {
		entity.update();
		this.dataModel.broadcastChange(getName());
	}

	@Override
	public String toXMLString() {
		return new XMLOutputter().outputString(this.toXMLElement());
	}

	@Override
	public Element toXMLElement() {
		Element attribute = new Element("Attribute");
		attribute.setAttribute("isSkipped", Boolean.toString(this.isSkipped));
		attribute.setAttribute("isDefined", (this.value == null) ? "false": "true");
		attribute.addContent(new Element("Name").setText(parent.getName()));
		attribute.addContent(new Element("isKey").setText(Boolean.toString(parent.isKeyAttribute())));
		attribute.addContent(new Element("Type").setText(parent.getType().toString()));
		attribute.addContent(new Element("Value").setText(this.getValue()));
		
		return attribute;
	}
	
}
