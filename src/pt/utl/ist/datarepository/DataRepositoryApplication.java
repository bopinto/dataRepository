package pt.utl.ist.datarepository;

import pt.utl.ist.datarepository.utils.SpecReceiver;

import com.vaadin.Application;
import com.vaadin.ui.*;

@SuppressWarnings("serial")
public class DataRepositoryApplication extends Application {
	
	private SpecReceiver specReceiver = null;
	
	@Override
	public void init() {
		Window mainWindow = new Window("Data Repository");
		Label label = new Label("This is the data repository");
		mainWindow.addComponent(label);
		
		specReceiver = new SpecReceiver(mainWindow);
		
		Upload uploadDataModel = new Upload("Upload the Data Model here", specReceiver);
		uploadDataModel.addListener((Upload.SucceededListener) specReceiver);
		uploadDataModel.addListener((Upload.FailedListener) specReceiver);
		
		mainWindow.addComponent(uploadDataModel);
		
		setMainWindow(mainWindow);
	}

}
