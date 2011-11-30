package pt.utl.ist.datarepository.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import pt.utl.ist.datarepository.persistency.datamodel.DataMetaModel;
import pt.utl.ist.datarepository.persistency.datamodel.DataModelFactory;
import pt.utl.ist.datarepository.utils.StringUtils;

public class DataModelFactoryTest {
	
	@Test
	public void testParseModel() {
		File dataModelFile = new File("/tmp/MedicalEpisodeDataModel.xml");
		
		if(!dataModelFile.exists()) {
			try {
				dataModelFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				fail();
			}
		}
		
		String dataModelString = StringUtils.fileToString(dataModelFile);
		
		DataModelFactory factory = new DataModelFactory();
		DataMetaModel dataModel = factory.parseXMLDataModel(dataModelString);
		
		Assert.assertNotNull(dataModel);
		Assert.assertFalse(dataModel.getEntities().isEmpty());
		Assert.assertFalse(dataModel.getParameterMappings().isEmpty());
		
	}

}
