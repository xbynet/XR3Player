package main.java.com.goxr3plus.xr3player.smartcontroller.presenter;

import java.io.IOException;
import java.util.Iterator;

import com.ibm.icu.util.LocaleData;
import com.ibm.icu.util.ULocale;
import com.jfoenix.controls.JFXButton;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import main.java.com.goxr3plus.xr3player.application.tools.InfoTool;

/**
 * AlphabetBar
 * 
 * @author GOXR3PLUSSTUDIO
 *
 */
public class AlphabetBar extends StackPane {
	
	//-----------------------------------------------------
	
	@FXML
	private JFXButton leftArrow;
	
	@FXML
	private ScrollPane scrollPane;
	
	@FXML
	private Pane alphabetBox;
	
	@FXML
	private JFXButton rightArrow;
	// -------------------------------------------------------------
	
	private SimpleBooleanProperty letterPressed = new SimpleBooleanProperty(this, "AlphabetBar");
	private final SmartController smartController;
	private final Orientation orientation;
	
	/**
	 * Constructor.
	 */
	public AlphabetBar(SmartController smartController, Orientation orientation) {
		this.smartController = smartController;
		this.orientation = orientation;
		
		// ------------------------------------FXMLLOADER ----------------------------------------
		FXMLLoader loader = new FXMLLoader(getClass().getResource(InfoTool.FXMLS + ( orientation == Orientation.HORIZONTAL ? "AlphabetBar.fxml" : "VAlphabetBar.fxml" )));
		loader.setController(this);
		loader.setRoot(this);
		
		try {
			loader.load();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * Called as soon as .fxml is initialised
	 */
	@FXML
	private void initialize() {
		changeLanguageBar(ULocale.ENGLISH);
		
		double speed = 0.10;
		
		if (orientation == Orientation.HORIZONTAL) {
			//Left
			leftArrow.setOnAction(a -> scrollPane.setHvalue(scrollPane.getHvalue() - speed));
			
			//Right 
			rightArrow.setOnAction(a -> scrollPane.setHvalue(scrollPane.getHvalue() + speed));
			
			//On Mouse Scrolling
			//scrollPane.setOnScroll(scroll -> scrollPane.setHvalue(scrollPane.getHvalue() + ( scroll.getDeltaY() > 0 ? speed : -speed )))
		} else {
			//Left
			leftArrow.setOnAction(a -> scrollPane.setVvalue(scrollPane.getVvalue() - speed));
			
			//Right 
			rightArrow.setOnAction(a -> scrollPane.setVvalue(scrollPane.getVvalue() + speed));
			
			//On Mouse Scrolling
			//scrollPane.setOnScroll(scroll -> scrollPane.setVvalue(scrollPane.getVvalue() + ( scroll.getDeltaY() > 0 ? speed : -speed )))
		}
		
	}
	
	/**
	 * @param ulocale
	 */
	public void changeLanguageBar(ULocale ulocale) {
		
		//Clear the previous buttons
		alphabetBox.getChildren().clear();
		
		Iterator<String> iterator = LocaleData.getExemplarSet(ulocale, LocaleData.ES_STANDARD).iterator();
		//System.out.println(LocaleData.getExemplarSet(ULocale.CHINESE, LocaleData.ES_STANDARD).size());
		
		//	int counter = 0;
		//For each letter
		while (iterator.hasNext()) {
			
			//Button
			JFXButton letter = new JFXButton(iterator.next());
			letter.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
			letter.getStyleClass().add("jfx-button4");
			
			//On Action
			letter.setOnAction(a -> {
				setLetterPressed(true);
				//Set the text to search field
				smartController.getSearchService().getSearchField().setText(letter.getText().toUpperCase());
				//In case instant search is not activated
				if (!smartController.getInstantSearch().isSelected())
					smartController.getInstantSearch();
				
				//System.out.println(letter.getText())
			});
			
			//Append on bar
			alphabetBox.getChildren().add(letter);
			
			//System.out.println(++counter+" "+iterator.next())
		}
		
	}
	
	/**
	 * @return the letterPressed
	 */
	public boolean isLetterPressed() {
		return letterPressed.get();
	}
	
	/**
	 * @param letterPressed
	 *            the letterPressed to set
	 */
	public void setLetterPressed(boolean letterPressed) {
		this.letterPressed.set(letterPressed);
	}
	
	/**
	 * @return the letterPressed
	 */
	public SimpleBooleanProperty letterPressedProperty() {
		return letterPressed;
	}
	
}
