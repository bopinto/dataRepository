package pt.utl.ist.datarepository.test;

import java.io.File;

import junit.framework.Assert;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.datarepository.DataRepository;
import pt.utl.ist.datarepository.persistency.datamodel.Attribute;
import pt.utl.ist.datarepository.persistency.datamodel.DataMetaModel;
import pt.utl.ist.datarepository.persistency.datamodel.DataModel;
import pt.utl.ist.datarepository.persistency.datamodel.Entity;
import pt.utl.ist.datarepository.persistency.datamodel.MetaAttribute;
import pt.utl.ist.datarepository.utils.StringUtils;

public class DataRepositoryTest {
	
	private String dataModel;
	private DataMetaModel dataMetaModel = null;
	
	private String dataModelURI = "MedicalEpisodeDataModel";
	private String instanceID = "0";
	
	@Before
	public void setUp() {
		File dataModelFile = new File("/tmp/MedicalEpisodeDataModel.xml");
		dataModel = StringUtils.fileToString(dataModelFile);
		
		DataRepository.get().loadModel(dataModel);
		
		this.dataMetaModel = DataRepository.get().getDataMetaModel("MedicalEpisodeDataModel");
		this.instanceID = DataRepository.get().createModelInstance(this.dataModelURI, "MedicalEpisode", "0", "Medical_Episode_GSpec_0",
				"0");
	}
	
	@Test
	public void testSetElementWithSimpleAttribute() {
		
		Element patientNameXML = new Element("PatientName");
		patientNameXML.setText("John");
		String patientName = new XMLOutputter().outputString(patientNameXML);
	
		DataRepository.get().setElement(this.dataModelURI, this.instanceID, patientName);
		
		DataModel dataModel = dataMetaModel.getDataModel(instanceID);
		Assert.assertNotNull(dataModel);
		
		Entity entity = dataModel.getEntity("Patient");
		Attribute attribute = entity.getAttribute("Patient.Name");
		
		Assert.assertEquals("John", attribute.getInstance().getValue());
	}
	
	@Test
	public void testSetElementWithMultipleAtt() {
		setNurseInfo();
		
		DataModel dataModel = dataMetaModel.getDataModel(instanceID);
		Assert.assertNotNull(dataModel);
		
		Entity patientData = dataModel.getEntity("Patient Data");
		Attribute physExam = patientData.getAttribute("Patient Data.Physical Examination");
		Attribute physReport = patientData.getAttribute("Patient Data.Physical Report");
		
		Assert.assertEquals("asd", physExam.getInstance().getValue());
		Assert.assertEquals("abc", physReport.getInstance().getValue());
	}
	
	@Test
	public void testSetElementWithPatient() {
		setPatient();
		
		DataModel dataModel = dataMetaModel.getDataModel(instanceID);
		Assert.assertNotNull(dataModel);
		
		Entity patientEnt = dataModel.getEntity("Patient");
		Attribute name = patientEnt.getAttribute("Patient.Name");
		Attribute address = patientEnt.getAttribute("Patient.Address");
		Attribute phone = patientEnt.getAttribute("Patient.PhoneNumber");
		Attribute gender = patientEnt.getAttribute("Patient.Gender");
		
		Assert.assertEquals("John", name.getInstance().getValue());
		Assert.assertEquals("1st Street", address.getInstance().getValue());
		Assert.assertEquals("1234", phone.getInstance().getValue());
		Assert.assertEquals("Male", gender.getInstance().getValue());
	}
	
	@Test
	public void testGetElementInfoWithPatient() {
		setPatient();
		
		String patientInXML = DataRepository.get().getElementInfo(dataModelURI, instanceID, "Patient");
		Document patientDoc = StringUtils.stringToDoc(patientInXML);
		Element pat = patientDoc.getRootElement();
		
		// validate the element
		Assert.assertNotNull(pat);
		Assert.assertEquals("Patient", pat.getName());
		Assert.assertNotNull(pat.getChild("Name"));
		Assert.assertNotNull(pat.getChild("Address"));
		Assert.assertNotNull(pat.getChild("PhoneNumber"));
		Assert.assertNotNull(pat.getChild("Gender"));
		
		Assert.assertEquals("John", pat.getChildText("Name"));
		Assert.assertEquals("1st Street", pat.getChildText("Address"));
		Assert.assertEquals("1234", pat.getChildText("PhoneNumber"));
		Assert.assertEquals("Male", pat.getChildText("Gender"));
	}
	
	@Test
	public void testGetElementNurseInfo() {
		setNurseInfo();
		
		String nurseInfoInXML = DataRepository.get().getElementInfo(dataModelURI, instanceID, "NursePatientData");
		Document nurseInfo = StringUtils.stringToDoc(nurseInfoInXML);
		Element info = nurseInfo.getRootElement();
		
		Assert.assertNotNull(info);
		Assert.assertEquals("NursePatientData", info.getName());
		Assert.assertNotNull(info.getChild("PhysicalReport"));
		Assert.assertNotNull(info.getChild("PhysicalExamination"));
		
		Assert.assertEquals("abc", info.getChildText("PhysicalReport"));
		Assert.assertEquals("asd", info.getChildText("PhysicalExamination"));
	}
	
	protected void setPatient() {
		Element patient = new Element("Patient");
		patient.addContent(new Element("Name").setText("John"));
		patient.addContent(new Element("Address").setText("1st Street"));
		patient.addContent(new Element("PhoneNumber").setText("1234"));
		patient.addContent(new Element("Gender").setText("Male"));
		String patientString = new XMLOutputter().outputString(patient);
		
		DataRepository.get().setElement(this.dataModelURI, this.instanceID, patientString);
	}
	
	protected void setNurseInfo() {
		Element nursePatDataXML = new Element("NursePatientData");
		nursePatDataXML.addContent(new Element("PhysicalReport").setText("abc"));
		nursePatDataXML.addContent(new Element("PhysicalExamination").setText("asd"));
		String nursePatData = new XMLOutputter().outputString(nursePatDataXML);
		
		DataRepository.get().setElement(this.dataModelURI, this.instanceID, nursePatData);
	}

}
