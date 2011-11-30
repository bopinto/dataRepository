package pt.utl.ist.datarepository.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import pt.utl.ist.datarepository.DataRepository;

import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class SpecReceiver implements Receiver, FailedListener, SucceededListener {
	
	protected ByteArrayOutputStream buffer;
	
	protected Logger log;
	
	private Window applicationWindow;
	
	public SpecReceiver(Window parent) {
		this.applicationWindow = parent;
		log = Logger.getLogger(SpecReceiver.class);
	}
	
	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		this.buffer = new ByteArrayOutputStream();
		
		return buffer;
	}

	@Override
	public void uploadSucceeded(SucceededEvent event) {
		// transform the file into a string
		String specInString = StringUtils.bufferToString(this.buffer);
		
		if(specInString == null) {
			this.applicationWindow.showNotification("Submition of " + event.getFilename() + " failed", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
		DataRepository.get().loadModel(specInString);
		
		this.applicationWindow.showNotification("Submition of " + event.getFilename() + " succeeded");
	}

	@Override
	public void uploadFailed(FailedEvent event) {
		this.applicationWindow.showNotification("Upload of " + event.getFilename() + " failed", Notification.TYPE_ERROR_MESSAGE);
	}

}
