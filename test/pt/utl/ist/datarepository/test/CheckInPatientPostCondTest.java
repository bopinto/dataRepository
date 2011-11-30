package pt.utl.ist.datarepository.test;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.datarepository.DataRepository;
import pt.utl.ist.datarepository.persistency.datamodel.DataMetaModel;
import pt.utl.ist.datarepository.persistency.datamodel.DataModel;
import pt.utl.ist.datarepository.persistency.datamodel.Entity;
import pt.utl.ist.datarepository.utils.StringUtils;

// because we're having a little bit of trouble putting this to work
public class CheckInPatientPostCondTest {
	
	private DataModel dataModel;
	private String instanceID;
	
	String dataModelURI = "MedicalEpisodeDataModel";
	
	@Before
	public void setUp() {
		File dataModelFile = new File("/tmp/MedicalEpisodeDataModel.xml");
		String dataModel = StringUtils.fileToString(dataModelFile);
		
		DataRepository.get().loadModel(dataModel);
		
		DataMetaModel dataMetaModel = DataRepository.get().getDataMetaModel("MedicalEpisodeDataModel");
		
		this.instanceID = DataRepository.get().createModelInstance(this.dataModelURI, "MedicalEpisode", "0", "Medical_Episode_GSpec_0",
				"0");
		this.dataModel = dataMetaModel.getDataModel(this.instanceID);
	}
	
	@Test
	public void testDefinePatientAndEpisode() {
		DataRepository.get().submitData(this.dataModelURI, this.instanceID, "Patient.Name", "STRING", "John", null, false);
		Entity patient = dataModel.getEntity("Patient");
		
		Assert.assertTrue(patient.getInstances().get(0).isDefined());
		
		Entity episode = dataModel.getEntity("Episode");
		
		Assert.assertTrue(episode.getInstances().get(0).isDefined());
	}

}
