package pt.utl.ist.datarepository.persistency.datamodel;

public class MetaAttribute {
	
	public enum Type {STRING, NUMBER, BOOLEAN};
	
	private String name;
	private MetaAttribute.Type type;
	private boolean isKeyAttribute;
	
	private MetaEntity entity;
	
	public MetaAttribute(String name, Type type, boolean isKeyAttribute, MetaEntity entity) {
		this.name = name;
		this.type = type;
		this.entity = entity;
		this.isKeyAttribute = isKeyAttribute;
	}
	
	public MetaAttribute(String attName, String attType, boolean isKey,
			MetaEntity entity) {
		Type t;
		if(attType.equals(Type.STRING.toString())){
			t = Type.STRING;
		} else if(attType.equals(Type.BOOLEAN.toString())) {
			t = Type.BOOLEAN;
		} else if(attType.equals(Type.NUMBER.toString())) {
			t = Type.NUMBER;
		} else {
			return;
		}
		this.name = attName;
		this.type = t;
		this.isKeyAttribute = isKey;
		this.entity = entity;
	}

	public String getName() { return this.name; }
	public Type getType() { return this.type; }
	
	public Attribute generateAttribute(Entity ent) {
		Attribute att = new Attribute(ent, this.type, this.name, this.isKeyAttribute);
		return att;
	}
	
	public MetaEntity getEntity() {
		return this.entity;
	}
}
