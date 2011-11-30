package pt.utl.ist.datarepository.persistency.datamodel;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import pt.utl.ist.datarepository.AddRelationMessage;
import pt.utl.ist.datarepository.persistency.datamodel.MetaAttribute.Type;
import pt.utl.ist.datarepository.persistency.datamodel.MetaRelation.Cardinality;
import pt.utl.ist.datarepository.utils.StringUtils;

public class DataModelFactory {
	
	public DataMetaModel parseXMLDataModel(String model) {
    	Document doc = StringUtils.stringToDoc(model);
    	
    	Element root = doc.getRootElement();
    	Namespace dmNamespace = root.getNamespace();
    	
    	Element specInfo = root.getChild("SpecificationInfo", dmNamespace);
    	
    	String dataModelURI = specInfo.getChildText("dataModelURI", dmNamespace);
    	String activityModelURI = specInfo.getChildText("activityModelURI", dmNamespace);
    	String goalModelURI = specInfo.getChildText("goalModelURI", dmNamespace);
    	
    	DataMetaModel metaModel = new DataMetaModel(dataModelURI);
    	metaModel.setActivityModelURI(activityModelURI);
    	metaModel.setGoalModelURI(goalModelURI);
    	
    	// get the mappings
    	Element mappings = (Element) root.getChild("mappings", dmNamespace);
    	for(Object map : mappings.getChildren()) {
    		Element mapping = (Element) map;
    		ParameterMapping paramMap = new ParameterMapping(mapping.getChildText("varName", dmNamespace));
    		for(Object mapTo : mapping.getChildren("mapsTo", dmNamespace)) {
    			Element mapToXML = (Element) mapTo;
    			paramMap.addMapping(mapToXML.getValue());
    		}
    		metaModel.addParameterMapping(paramMap);
    	}
    	
    	List entities = root.getChildren("Entity", dmNamespace);
    	for (Object ent : entities) {
			Element entityXML = (Element) ent;
			String entityID = entityXML.getChildText("id", dmNamespace);
			String entityName = entityXML.getChildText("Name", dmNamespace);
			MetaEntity entity = new MetaEntity(entityName, entityID);
			
			List attributes = entityXML.getChildren("Attribute", dmNamespace);
			for (Object att : attributes) {
				Element attributeXML = (Element) att;
				String attName = attributeXML.getChildText("Name", dmNamespace);
				String attType = attributeXML.getChildText("Type", dmNamespace);
				boolean isKey = Boolean.parseBoolean(attributeXML.getChildText("isKey", dmNamespace));
				MetaAttribute attribute = new MetaAttribute(attName, attType, isKey, entity);
				entity.addAttribute(attribute);
			}
			metaModel.addEntity(entity);
		}
    	
    	List relations = root.getChildren("Relation", dmNamespace);
    	for (Object rel : relations) {
			Element relationXML = (Element) rel;
			MetaRelation metaRel = parseRelationInXML(metaModel, relationXML);
			metaModel.addRelation(metaRel);
		}
    	
    	return metaModel;
    }
	
	public MetaRelation parseRelationInXML(DataMetaModel metaModel, Element relationInXML) {
		Namespace dmNamespace = relationInXML.getNamespace();
		MetaRelation metaRel = new MetaRelation();
		
		Element entityOneXML = relationInXML.getChild("EntityOne", dmNamespace);
		Element entityTwoXML = relationInXML.getChild("EntityTwo", dmNamespace);
		
		metaRel.setEntityOne(metaModel.getEntityByID(entityOneXML.getChildText("EntityID", dmNamespace)));
		metaRel.setCardinalityOne(entityOneXML.getChildText("EntityCardinality", dmNamespace));
		metaRel.isOneKeyEntity(Boolean.parseBoolean(entityOneXML.getChildText("isEntityKey", dmNamespace)));
		
		metaRel.setEntityTwo(metaModel.getEntityByID(entityTwoXML.getChildText("EntityID", dmNamespace)));
		metaRel.setCardinalityTwo(entityTwoXML.getChildText("EntityCardinality", dmNamespace));
		metaRel.isTwoKeyEntity(Boolean.parseBoolean(entityTwoXML.getChildText("isEntityKey", dmNamespace)));
		
		return metaRel;
	}
	
	public boolean addEntityToDataModel(DataModel dataModel, String entityName, AddRelationMessage relation) {  //TODO test
		Entity newEntity = new Entity(entityName);
		
		Relation newRelation = null;
		Cardinality cardinalityOne = parseCardinality(relation.getCardinalityOne());
		Cardinality cardinalityTwo = parseCardinality(relation.getCardinalityTwo());
		boolean isOneKey = relation.isOneKeyEntity();
		boolean isTwoKey = relation.isTwoKeyEntity();
		
		if(relation.getEntityOne().equals(entityName)) {
			Entity otherEntity = dataModel.getEntity(relation.getEntityTwo());
			newRelation = new Relation(newEntity, otherEntity,
					cardinalityOne, cardinalityTwo, isOneKey, isTwoKey); 	
		} else if(relation.getEntityTwo().equals(entityName)) {
			Entity otherEntity = dataModel.getEntity(relation.getEntityOne());
			newRelation = new Relation(otherEntity, newEntity,
					cardinalityOne, cardinalityTwo, isOneKey, isTwoKey);
		} else {
			return false;
		}
		
		dataModel.addEntity(newEntity);
		dataModel.addRelation(newRelation);
		
		return true;
	}
	
	public boolean addAttributeToEntity(DataModel dataModel, String attributeURI, String type, boolean isKey) { //TODO test
		String[] element = attributeURI.split("\\.");
		if(element.length != 2) {
			return false;
		}
		
		Entity entity = dataModel.getEntity(element[0]);
		if(entity == null) {
			return false;
		}
		
		Attribute att = new Attribute(entity, parseAttributeType(type),
				element[1], isKey);
		entity.addAttribute(att);
		
		return true;
	}
	
	private Cardinality parseCardinality(String card) {
		if(card.equals(Cardinality.ONE.toString())) {
			return Cardinality.ONE;
		} else if(card.equals(Cardinality.MANY.toString())) {
			return Cardinality.MANY;
		} else if(card.equals(Cardinality.ZERO_OR_ONE.toString())) {
			return Cardinality.ZERO_OR_ONE;
		}
		return null;
	}
	
	private Type parseAttributeType(String typeInString) {
		if(typeInString.equalsIgnoreCase(Type.BOOLEAN.toString())) {
			return Type.BOOLEAN;
		} else if(typeInString.equalsIgnoreCase(Type.STRING.toString())){
			return Type.STRING;
		} else if(typeInString.equalsIgnoreCase(Type.NUMBER.toString())) {
			return Type.NUMBER;
		}
		return null;
	}

}
