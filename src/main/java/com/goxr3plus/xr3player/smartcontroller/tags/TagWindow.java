package main.java.com.goxr3plus.xr3player.smartcontroller.tags;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.jfoenix.controls.JFXTabPane;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import main.java.com.goxr3plus.xr3player.application.tools.ActionTool;
import main.java.com.goxr3plus.xr3player.application.tools.InfoTool;
import main.java.com.goxr3plus.xr3player.application.tools.NotificationType;
import main.java.com.goxr3plus.xr3player.smartcontroller.enums.Genre;
import main.java.com.goxr3plus.xr3player.smartcontroller.media.Audio;
import main.java.com.goxr3plus.xr3player.smartcontroller.tags.mp3.ID3v1;
import main.java.com.goxr3plus.xr3player.smartcontroller.tags.mp3.ID3v2;
import main.java.com.goxr3plus.xr3player.smartcontroller.tags.mp3.MP3BasicInfo;

/**
 * This window allows to modify Tags of various AudioFormats
 * 
 * @author GOXR3PLUS
 *
 */
public class TagWindow extends StackPane {
	
	//--------------------------------------------------------
	
	@FXML
	private SplitPane splitPane;
	
	@FXML
	private ListView<String> listView;
	
	@FXML
	private JFXTabPane tabPane;
	
	@FXML
	private Tab basicInfoTab;
	
	@FXML
	private Tab artWorkTab;
	
	@FXML
	private Tab id3v1Tab;
	
	@FXML
	private Tab id3v2Tab;
	
	//--------------------------------------------------------
	
	/** The logger. */
	private Logger logger = Logger.getLogger(getClass().getName());
	
	/** The Window */
	private final Stage window = new Stage();
	
	//For MP3
	private final MP3BasicInfo mp3BasicInfo = new MP3BasicInfo();
	private final ArtWorkController artWork = new ArtWorkController();
	private final ID3v1 id3V1Controller = new ID3v1();
	private final ID3v2 id3V2Controller = new ID3v2();
	
	/**
	 * Constructor
	 */
	public TagWindow() {
		
		// ------------------------------------FXMLLOADER
		FXMLLoader loader = new FXMLLoader(getClass().getResource(InfoTool.FXMLS + "TagWindowController.fxml"));
		loader.setController(this);
		loader.setRoot(this);
		
		try {
			loader.load();
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "", ex);
		}
		
		window.setTitle("Tag Window");
		window.initStyle(StageStyle.UTILITY);
		window.setScene(new Scene(this));
		window.getScene().setOnKeyReleased(k -> {
			if (k.getCode() == KeyCode.ESCAPE)
				window.close();
		});
		window.getScene().setOnDragOver(dragOver -> dragOver.acceptTransferModes(TransferMode.LINK));
		window.getScene().setOnDragDropped(drop -> {
			// Keeping the absolute path
			String absolutePath;
			
			// File?
			for (File file : drop.getDragboard().getFiles()) {
				absolutePath = file.getAbsolutePath();
				if (file.isFile() && InfoTool.isAudioSupported(absolutePath)) {
					openAudio(file.getAbsolutePath(), TagTabCategory.BASICINFO, true);
					break;
				}
			}
		});
	}
	
	/**
	 * Called as soon as .fxml is initialized
	 */
	@FXML
	private void initialize() {
		
		//basicInfoTab
		basicInfoTab.setContent(mp3BasicInfo);
		
		//artWorkTab
		artWorkTab.setContent(artWork);
		//ImageView
		artWork.getImageView().fitWidthProperty().bind(window.widthProperty().subtract(20));
		artWork.getImageView().fitHeightProperty().bind(window.heightProperty().subtract(110));
		
		//id3v1Tab
		id3v1Tab.setContent(id3V1Controller);
		
		//id3v2Tab
		id3v2Tab.setContent(id3V2Controller);
		
		//listView
		listView.setCellFactory(lv -> new ListCell<String>() {
			@Override
			public void updateItem(String item , boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setText(null);
				} else {
					String text = InfoTool.getFileName(item); // get text from item
					setText(text);
					setTooltip(new Tooltip(item));
				}
			}
		});
		listView.getSelectionModel().selectedItemProperty().addListener((observable , oldValue , newValue) -> {
			if (newValue != null)
				openAudio(newValue, TagTabCategory.CURRENT, false);
		});
		
	}
	
	/**
	 * @return the window
	 */
	public Stage getWindow() {
		return window;
	}
	
	/**
	 * Close the Window
	 */
	public void close() {
		window.close();
	}
	
	/**
	 * Show the Window
	 */
	public void show() {
		if (!window.isShowing())
			window.show();
		else
			window.requestFocus();
	}
	
	/**
	 * Opens multiple audio files at once ( based on the ListView that is
	 * created)
	 * 
	 * @param list
	 *            A given observable list containing all the absolute file paths
	 * @param selectedItem
	 *            Not null if a specific file item must be selected first
	 */
	public void openMultipleAudioFiles(ObservableList<String> list , String selectedItem) {
		listView.setItems(list);
		if (selectedItem != null) {
			listView.getSelectionModel().select(selectedItem);
			listView.scrollTo(selectedItem);
		} else
			listView.getSelectionModel().select(0);
	}
	
	/**
	 * Open the TagWindow based on the extension of the Audio
	 * 
	 * @param absolutePath
	 *            The absolute path of the file
	 * @param tabCategory
	 *            The tag tab category
	 */
	public void openAudio(String absolutePath , TagTabCategory tabCategory , boolean clearListView) {
		//Clear listView
		if (clearListView) {
			listView.setItems(Arrays.asList(absolutePath).stream().collect(Collectors.toCollection(FXCollections::observableArrayList)));
			listView.getSelectionModel().select(0);
		}
		
		//Check the absolutePath
		if (absolutePath != null) {
			
			//Find file extension
			String extension = InfoTool.getFileExtension(absolutePath);
			
			//Clear Tab Pane Tabs
			tabPane.getTabs().clear();
			
			//mp3?
			if (extension.equalsIgnoreCase("mp3")) {
				
				//Add the tabs
				tabPane.getTabs().addAll(basicInfoTab, artWorkTab, id3v1Tab, id3v2Tab);
				
				//Check the tabCategory
				if (tabCategory == TagTabCategory.BASICINFO)
					tabPane.getSelectionModel().select(0);
				else if (tabCategory == TagTabCategory.ARTWORK)
					tabPane.getSelectionModel().select(1);
				
				//basicInfoTab
				mp3BasicInfo.updateInformation(new Audio(absolutePath, 0.0, 0, "", "", Genre.SEARCHWINDOW, -1));
				
				//artWorkTab
				artWork.showMediaFileImage(absolutePath);
				
				//id3v1Tab
				//id3v1Tab.setContent(id3V1Controller)
				
				//id3v2Tab
				//id3v2Tab.setContent(id3V2Controller)
				
			} else {
				
				//Add the tabs
				tabPane.getTabs().addAll(basicInfoTab);
				
				//basicInfoTab
				mp3BasicInfo.updateInformation(new Audio(absolutePath, 0.0, 0, "", "", Genre.SEARCHWINDOW, -1));
				
			}
			
			show();
		} else
			ActionTool.showNotification("No File", "No File has been selected ...", Duration.seconds(2), NotificationType.SIMPLE);
	}
	
}