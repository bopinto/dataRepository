package pt.utl.ist.datarepository.test;

import java.io.File;

import junit.framework.Assert;

import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.datarepository.DataRepository;
import pt.utl.ist.datarepository.persistency.datamodel.Attribute;
import pt.utl.ist.datarepository.persistency.datamodel.DataMetaModel;
import pt.utl.ist.datarepository.persistency.datamodel.DataModel;
import pt.utl.ist.datarepository.persistency.datamodel.Entity;
import pt.utl.ist.datarepository.persistency.datamodel.MetaAttribute.Type;
import pt.utl.ist.datarepository.utils.StringUtils;

public class DataModelTest {
	
	DataModel dataModel = null;
	
	@Before
	public void setUp() {
		dataModel = new DataModel("TestDataModel", "1");
		
		Entity entity = new Entity("Entity1");
		Attribute att = new Attribute(entity, Type.STRING, "Attribute1", true);
		entity.addAttribute(att);
			
		dataModel.addEntity(entity);
	}
	
	@Test
	public void testGetElementAsXMLString() {
		// get entity
		String entityAsXMLResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Element><Entity isSkipped=\"false\" isDefined=\"false\">" +
				"<Name>Entity1</Name>" +
				"<Attribute isSkipped=\"false\" isDefined=\"false\">" +
				"<Name>Entity1.Attribute1</Name>" +
				"<isKey>true</isKey><Type>STRING</Type>" +
				"<Value>N/A</Value>" +
				"</Attribute></Entity></Element>";
		Document doc = StringUtils.stringToDoc(entityAsXMLResult);
		entityAsXMLResult = new XMLOutputter().outputString(doc);
		
		// get attribute
		String attributeAsXMLResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Element>" + 
				"<Attribute isSkipped=\"false\" isDefined=\"false\">" +
				"<Name>Entity1.Attribute1</Name>" +
				"<isKey>true</isKey><Type>STRING</Type><Value>N/A</Value></Attribute>" +
				"</Element>";
		
		doc = StringUtils.stringToDoc(attributeAsXMLResult);
		attributeAsXMLResult = new XMLOutputter().outputString(doc);
		
		String entityAsXML = dataModel.getElementAsXMLString("Entity1");
		String attributeAsXML = dataModel.getElementAsXMLString("Entity1.Attribute1");
		
		Assert.assertEquals(entityAsXMLResult, entityAsXML);
		Assert.assertEquals(attributeAsXMLResult, attributeAsXML);
	}

	@Test
	public void testRealDataModelInstance() {
		File dataModelFile = new File("/tmp/MedicalEpisodeDataModel.xml");
		String dataModelSpec = StringUtils.fileToString(dataModelFile);
		DataRepository.get().loadModel(dataModelSpec);
		DataMetaModel metaModel = DataRepository.get().getDataMetaModel("MedicalEpisodeDataModel");
		DataModel dataModel = metaModel.generateDataModel();
		
		Assert.assertNotNull(dataModel.getInstance());
	}
}
