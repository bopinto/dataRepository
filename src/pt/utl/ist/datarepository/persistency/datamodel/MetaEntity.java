package pt.utl.ist.datarepository.persistency.datamodel;

import java.util.ArrayList;

public class MetaEntity {
	
	private String name;
	private String id;
	
	private ArrayList<MetaAttribute> attributes = new ArrayList<MetaAttribute>();
	private ArrayList<MetaRelation> relations = new ArrayList<MetaRelation>();

	public MetaEntity(String name, String id) {
		this.name = name;
		this.id = id;
	}
	
	public String getName() { return this.name; }
	public String getID() { return this.id; }	
	
	public void addAttribute(MetaAttribute attribute) {
		this.attributes.add(attribute);
	}
	
	public void addRelation(MetaRelation relation) {
		this.relations.add(relation);
	}
	
	public Entity generateEntity() {
		Entity ent = new Entity(this.name);
		for (MetaAttribute att : this.attributes) {
			ent.addAttribute(att.generateAttribute(ent));
		}
		
		return ent;
	}
	
	public ArrayList<MetaAttribute> getAttributes() {
		return this.attributes;
	}
	
	public MetaAttribute getAttribute(String attributeName) {
		for (MetaAttribute att : this.attributes) {
			if(att.getName().equals(attributeName)) {
				return att;
			}
		}
		return null;
	}
}
