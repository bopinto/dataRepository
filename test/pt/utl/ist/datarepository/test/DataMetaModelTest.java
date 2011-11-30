package pt.utl.ist.datarepository.test;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.datarepository.DataRepository;
import pt.utl.ist.datarepository.persistency.datamodel.DataMetaModel;
import pt.utl.ist.datarepository.persistency.datamodel.DataModel;
import pt.utl.ist.datarepository.persistency.datamodel.MetaRelation;
import pt.utl.ist.datarepository.utils.StringUtils;

public class DataMetaModelTest {

	private DataMetaModel metaModel;
	
	@Before
	public void setUp() {
		File dataModelFile = new File("/tmp/MedicalEpisodeDataModel.xml");
		String dataModel = StringUtils.fileToString(dataModelFile);
		DataRepository.get().loadModel(dataModel);
		metaModel = DataRepository.get().getDataMetaModel("MedicalEpisodeDataModel");
	}
	
	@Test
	public void testGetEntities() {
		Assert.assertNotNull(metaModel.getEntities());
		Assert.assertNotNull(metaModel.getEntity("Patient"));
		Assert.assertNotNull(metaModel.getEntity("Episode"));
		Assert.assertNotNull(metaModel.getEntity("Patient Data"));
		Assert.assertNotNull(metaModel.getEntity("Prescription"));
		Assert.assertNotNull(metaModel.getEntity("Medical Report"));
	}
	
	@Test
	public void testGetRelations() {
		Assert.assertNotNull(metaModel.getRelations());
		Assert.assertEquals(4, metaModel.getRelations().size());
	}
	
	@Test
	public void testRelationOneCompleted() {
		MetaRelation rel = metaModel.getRelations().get(0);
		Assert.assertNotNull(rel.getEntityOne());
		Assert.assertNotNull(rel.getEntityTwo());
		Assert.assertNotNull(rel.getCardinalityOne());
		Assert.assertNotNull(rel.getCardinalityTwo());
	}
	
	@Test
	public void testRelationTwoCompleted() {
		MetaRelation rel = metaModel.getRelations().get(1);
		Assert.assertNotNull(rel.getEntityOne());
		Assert.assertNotNull(rel.getEntityTwo());
		Assert.assertNotNull(rel.getCardinalityOne());
		Assert.assertNotNull(rel.getCardinalityTwo());
	}
	
	@Test
	public void testRelationThreeCompleted() {
		MetaRelation rel = metaModel.getRelations().get(2);
		Assert.assertNotNull(rel.getEntityOne());
		Assert.assertNotNull(rel.getEntityTwo());
		Assert.assertNotNull(rel.getCardinalityOne());
		Assert.assertNotNull(rel.getCardinalityTwo());
	}
	
	@Test
	public void testRelationFourCompleted() {
		MetaRelation rel = metaModel.getRelations().get(3);
		Assert.assertNotNull(rel.getEntityOne());
		Assert.assertNotNull(rel.getEntityTwo());
		Assert.assertNotNull(rel.getCardinalityOne());
		Assert.assertNotNull(rel.getCardinalityTwo());
	}
	
	@Test
	public void testGenerateDataModel() {
		DataModel dataModel = metaModel.generateDataModel();
		Assert.assertNotNull(dataModel);
		
		Assert.assertNotNull(metaModel.getDataModel(dataModel.getInstanceID()));
	}
}
