/**
 * 
 */
package loginsystema;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.controlsfx.control.Notifications;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXSlider.IndicatorPosition;

import application.Main;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import tools.ActionTool;
import tools.InfoTool;
import tools.NotificationType;

/**
 * @author GOXR3PLUS
 *
 */
public class LoginMode extends BorderPane {

    @FXML
    private StackPane usersStackView;

    @FXML
    private Button previous;

    @FXML
    private Button next;

    @FXML
    private Button newUser;

    @FXML
    private Button createUser;

    @FXML
    private Button loginButton;

    @FXML
    private Button deleteUser;

    @FXML
    private Button exitButton;

    // --------------------------------------------

    /** The logger for this class */
    private static final Logger logger = Logger.getLogger(LoginMode.class.getName());

    /**
     * Allows to see the users in a beatiful way
     */
    public UsersViewer userViewer = new UsersViewer();

    /** This variable is used during the creation of a new user. */
    private final InvalidationListener creationInvalidator = new InvalidationListener() {
	@Override
	public void invalidated(Observable observable) {

	    // Remove the Listener
	    Main.renameWindow.showingProperty().removeListener(this);

	    // !Showing && !XPressed
	    if (!Main.renameWindow.isShowing() && !Main.renameWindow.isXPressed()) {

		Main.window.requestFocus();

		// Check if this name already exists
		String name = Main.renameWindow.getUserInput();

		// if can pass
		if (!userViewer.items.stream().anyMatch(user -> user.getUserName().equals(name))) {

		    if (new File(InfoTool.ABSOLUTE_DATABASE_PATH_WITH_SEPARATOR + name).mkdir())
			userViewer.addUser(new User(name, userViewer.items.size()), true);
		    else
			ActionTool.showNotification("Error", "An error occured trying to create a new user",
				Duration.seconds(2), NotificationType.ERROR);

		    // update the positions
		    //updateUsersPosition()
		} else {
		    Notifications.create().title("Dublicate User").text("This user already exists").darkStyle()
			    .showConfirm();
		}
	    }
	}
    };

    /**
     * Constructor
     */
    public LoginMode() {

	// ----------------------------------FXMLLoader-------------------------------------
	FXMLLoader loader = new FXMLLoader(getClass().getResource(InfoTool.fxmls + "LoginScreenController.fxml"));
	loader.setController(this);
	loader.setRoot(this);

	// -------------Load the FXML-------------------------------
	try {
	    loader.load();
	} catch (IOException ex) {
	    logger.log(Level.WARNING, "", ex);
	}

    }

    /**
     * Called as soon as FXML file has been loaded
     */
    @FXML
    private void initialize() {

	//super
	setStyle(
		"-fx-background-color:rgb(0,0,0,0.9); -fx-background-size:100% 100%; -fx-background-image:url('/image/libraryModeBackground.jpg'); -fx-background-position: center center; -fx-background-repeat:stretch;");

	// createLibrary
	createUser.setOnAction(a -> {
	    if (!Main.renameWindow.isShowing()) {

		// Open rename window
		Main.renameWindow.show("", createUser);

		// Add the showing listener
		Main.renameWindow.showingProperty().addListener(creationInvalidator);
	    }
	});

	//newUser
	newUser.setOnAction(a -> {
	    if (!Main.renameWindow.isShowing()) {

		// Open rename window
		Main.renameWindow.show("", newUser);

		// Add the showing listener
		Main.renameWindow.showingProperty().addListener(creationInvalidator);
	    }
	});
	newUser.visibleProperty().bind(Bindings.size(userViewer.items).isEqualTo(0));

	//loginButton
	loginButton.setOnAction(a -> Main.startAppWithUser(userViewer.getSelectedItem()));

	//deleteUser
	deleteUser.disableProperty().bind(newUser.visibleProperty());
	deleteUser.setOnAction(a -> {	   
	    //Try to delete it
	    if (ActionTool.deleteFile(new File(
		    InfoTool.ABSOLUTE_DATABASE_PATH_WITH_SEPARATOR + userViewer.getSelectedItem().getUserName())))
		userViewer.deleteUser(userViewer.getSelectedItem());
	    else
		ActionTool.showNotification("Error", "An error occured trying to delete the user", Duration.seconds(2),
			NotificationType.ERROR);
	});

	//exitButton
	exitButton.setOnAction(a -> System.exit(0));

	// previous
	previous.setOnAction(a -> userViewer.previous());

	// next
	next.setOnAction(a -> userViewer.next());

	//Continue
	usersStackView.getChildren().add(userViewer);
	userViewer.toBack();
    }

    /**
     * Gets the previous.
     *
     * @return the previous
     */
    protected Button getPrevious() {
	return previous;
    }

    /**
     * Gets the next.
     *
     * @return the next
     */
    protected Button getNext() {
	return next;
    }

    /*-----------------------------------------------------------------------
     * 
     * 
     * -----------------------------------------------------------------------
     * 
     * 
     * -----------------------------------------------------------------------
     * 
     * 
     * 						    Libraries Viewer
     * 
     * -----------------------------------------------------------------------
     * 
     * -----------------------------------------------------------------------
     * 
     * -----------------------------------------------------------------------
     * 
     * -----------------------------------------------------------------------
     */
    /**
     * This class allows you to view items
     *
     * @author GOXR3PLUS
     */
    public class UsersViewer extends Region {

	/** The Constant WIDTH. */
	double WIDTH = 120;

	/** The Constant HEIGHT. */
	double HEIGHT = WIDTH + (WIDTH * 0.4);

	/** The duration. */
	private final Duration duration = Duration.millis(450);

	/** The interpolator. */
	private final Interpolator interpolator = Interpolator.EASE_BOTH;

	/** The Constant SPACING. */
	private double SPACING = 120;

	/** The Constant LEFT_OFFSET. */
	private double LEFT_OFFSET = -110;

	/** The Constant RIGHT_OFFSET. */
	private double RIGHT_OFFSET = 110;

	/** The Constant SCALE_SMALL. */
	private static final double SCALE_SMALL = 0.6;

	/** The items. */
	ObservableList<User> items = FXCollections.observableArrayList();
	SimpleListProperty<User> list = new SimpleListProperty<>(items);

	/** The centered. */
	private Group centered = new Group();

	/** The left group. */
	private Group leftGroup = new Group();

	/** The center group. */
	private Group centerGroup = new Group();

	/** The right group. */
	private Group rightGroup = new Group();

	/** The center index. */
	int centerIndex = 0;

	/** The scroll bar. */
	JFXSlider jfSlider = new JFXSlider();

	/** The time line */
	private Timeline timeline = new Timeline();

	Rectangle clip = new Rectangle();

	/**
	 * Instantiates a new libraries viewer.
	 */
	// Constructor
	public UsersViewer() {

	    // this.setOnMouseMoved(m -> {
	    //
	    // if (dragDetected) {
	    // System.out.println("Mouse Moving... with drag detected");
	    //
	    // try {
	    // Robot robot = new Robot();
	    // robot.mouseMove((int) m.getScreenX(),
	    // (int) this.localToScreen(this.getBoundsInLocal()).getMinY() + 2);
	    // } catch (AWTException ex) {
	    // ex.printStackTrace();
	    // }
	    // }
	    // })

	    // clip.set
	    setClip(clip);
	    setStyle("-fx-background-color: linear-gradient(to bottom,black 60,#141414 60.2%, purple 87%);");

	    // ScrollBar
	    jfSlider.setIndicatorPosition(IndicatorPosition.RIGHT);
	    jfSlider.setCursor(Cursor.HAND);
	    jfSlider.setMin(0);
	    jfSlider.setMax(0);
	    jfSlider.visibleProperty().bind(list.sizeProperty().greaterThan(3));
	    // scrollBar.setVisibleAmount(1)
	    // scrollBar.setUnitIncrement(1)
	    // scrollBar.setBlockIncrement(1)
	    jfSlider.setShowTickLabels(true);
	    // scrollBar.setMajorTickUnit(1)
	    jfSlider.setShowTickMarks(true);
	    jfSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
		int newVal = (int) Math.round(newValue.doubleValue());
		int oldVal = (int) Math.round(oldValue.doubleValue());
		// new!=old
		if (newVal != oldVal) {
		    setCenterIndex(newVal);
		}

		// System.out.println(scrollBar.getValue())
	    });

	    // setFocusTraversable(true)
	    // setOnKeyReleased(key -> {
	    // if (key.getCode() == KeyCode.LEFT) {
	    // if (timeline.getStatus() != Status.RUNNING)
	    // previous();
	    // } else if (key.getCode() == KeyCode.RIGHT) {
	    // if (timeline.getStatus() != Status.RUNNING)
	    // next();
	    // }
	    //
	    // })

	    // create content
	    centered.getChildren().addAll(leftGroup, rightGroup, centerGroup);

	    getChildren().addAll(centered, jfSlider);
	}

	/**
	 * The Collection that holds all the Items
	 * 
	 * @return The Collection that holds all the Items
	 */
	public ObservableList<User> getItems() {
	    return items;
	}

	// ----About the last size of each Library
	double lastSize;

	// ----About the width and height of LibraryMode Clip
	int previousWidth;
	int previousHeight;

	int counter;
	double var = 1.5;

	@Override
	protected void layoutChildren() {

	    // update clip to our size
	    clip.setWidth(getWidth());
	    clip.setHeight(getHeight());

	    // keep centered centered

	    WIDTH = getHeight();
	    HEIGHT = WIDTH;// + (WIDTH * 0.4)

	    centered.setLayoutX((getWidth() - WIDTH) / 2);
	    centered.setLayoutY((getHeight() - HEIGHT / var) / 2);

	    // centered.setLayoutX((getWidth() - WIDTH) / 2)
	    // centered.setLayoutY((getHeight() - HEIGHT) / 2)

	    jfSlider.setLayoutX(getWidth() / 2 - 100);
	    jfSlider.setLayoutY(10);
	    jfSlider.resize(200, 15);

	    // AVOID DOING CALCULATIONS WHEN THE CLIP SIZE IS THE SAME
	    // if (previousWidth != (int) WIDTH ||
	    if (previousHeight != (int) HEIGHT) {
		// System.out.println("Updating Library Size")

		// Update ImageView width and height
		SPACING = HEIGHT / (var + 0.5);
		LEFT_OFFSET = -(SPACING - 10);
		RIGHT_OFFSET = -LEFT_OFFSET;
		// For-Each
		items.forEach(user -> {
		    double size = HEIGHT / var;

		    // --
		    user.imageView.setFitWidth(size);
		    user.imageView.setFitHeight(size);
		    user.setMaxWidth(size);
		    user.setMaxHeight(size);
		});

		// Dont Fuck the CPU
		double currentSize = WIDTH / var; // the current size of each
						 // library
		boolean doUpdate = Math.abs(currentSize - lastSize) > 2;
		// System.out.println("Do update?:" + doUpdate + " , " +
		// Math.abs(currentSize - lastSize) + "SSD.U2\n")
		lastSize = currentSize;
		if (doUpdate)
		    update();
	    }

	    previousWidth = (int) WIDTH;
	    previousHeight = (int) HEIGHT;
	    // System.out.println("Counter:" + (++counter) + " , " + getWidth()
	    // + "," + getHeight())

	}

	/**
	 * @return The selected item from the List (That means the center index)
	 */
	public User getSelectedItem() {
	    return items.get(centerIndex);
	}

	//	/**
	//	 * Go on selection mode.
	//	 *
	//	 * @param way
	//	 *            the way
	//	 */
	//	public void goOnSelectionMode(boolean way) {
	//	    for (Library library : items)
	//		library.goOnSelectionMode(way);
	//	}

	/**
	 * Add multiple users at once.
	 *
	 * @param list
	 *            The List with the users to be added
	 */
	public void addMultipleUsers(List<User> list) {
	    list.forEach(user -> this.addUser(user, false));

	    // update
	    update();
	}

	/**
	 * Add the new library.
	 *
	 * @param user
	 *            The User to be added
	 */
	public void addUser(User user, boolean update) {
	    items.add(user);

	    // --
	    double size = HEIGHT / var;

	    user.imageView.setFitWidth(size);
	    user.imageView.setFitHeight(size);
	    user.setMaxWidth(size);
	    user.setMaxHeight(size);

	    // --
	    user.setOnMouseClicked(m -> {

		if (m.getButton() == MouseButton.PRIMARY || m.getButton() == MouseButton.MIDDLE) {

		    // If it isn't the same User again
		    if (((User) centerGroup.getChildren().get(0)).getPosition() != user.getPosition()) {

			setCenterIndex(user.getPosition());
			// scrollBar.setValue(library.getPosition())
		    }

		} else if (m.getButton() == MouseButton.SECONDARY) {

		    // if isn't the same User again
		    if (((User) centerGroup.getChildren().get(0)).getPosition() != user.getPosition()) {

			setCenterIndex(user.getPosition());
			// scrollBar.setValue(library.getPosition())

			//			timeline.setOnFinished(v -> {
			//			    Bounds bounds = library.localToScreen(library.getBoundsInLocal());
			//			    contextMenu.show(Main.window, bounds.getMinX() + bounds.getWidth() / 3,
			//				    bounds.getMinY() + bounds.getHeight() / 4, library);
			//			    timeline.setOnFinished(null);
			//			});

		    } else { // if is the same User again
			//			Bounds bounds = library.localToScreen(library.getBoundsInLocal());
			//			contextMenu.show(Main.window, bounds.getMinX() + bounds.getWidth() / 3,
			//				bounds.getMinY() + bounds.getHeight() / 4, library);
		    }
		}

	    });

	    // MAX
	    jfSlider.setMax(items.size() - 1.00);

	    //Update?
	    if (update)
		userViewer.update();
	}

	//	/**
	//	 * Recalculate the position of all the libraries.
	//	 *
	//	 * @param commit
	//	 *            the commit
	//	 */
	//	public void updateLibrariesPositions(boolean commit) {
	//
	//	    for (int i = 0; i < items.size(); i++)
	//		items.get(i).updatePosition(i);
	//
	//	    if (commit)
	//		Main.dbManager.commit();
	//	}

	/**
	 * Deletes the specific user from the list
	 * 
	 * @param user
	 *            User to be deleted
	 */
	public void deleteUser(User user) {
	    items.remove(user);

	    for (int i = 0; i < items.size(); i++)
		items.get(i).setPosition(i);

	    calculateCenterAfterDelete();
	}

	/**
	 * Recalculate the center index after a delete occurs.
	 */
	private void calculateCenterAfterDelete() {

	    // center index
	    if (!leftGroup.getChildren().isEmpty())
		centerIndex = leftGroup.getChildren().size() - 1;
	    else
		// if (!rightGroup.getChildren().isEmpty())
		// centerIndex = 0
		// else
		centerIndex = 0;

	    // Max
	    jfSlider.setMax(items.size() - 1.00);

	    update();

	}

	/**
	 * Sets the center index.
	 *
	 * @param i
	 *            the new center index
	 */
	public void setCenterIndex(int i) {
	    if (centerIndex != i) {
		centerIndex = i;
		update();

		// Update the ScrollBar Value
		jfSlider.setValue(centerIndex);
	    }
	}

	/**
	 * Goes to next Item (RIGHT).
	 */
	public void next() {
	    if (centerIndex + 1 < items.size())
		setCenterIndex(centerIndex + 1);
	}

	/**
	 * Goes to previous item(LEFT).
	 */
	public void previous() {
	    if (centerIndex > 0)
		setCenterIndex(centerIndex - 1);
	}

	/**
	 * Update the library viewer so it shows the center index correctly.
	 */
	public void update() {

	    // Reconstruct Groups
	    leftGroup.getChildren().clear();
	    centerGroup.getChildren().clear();
	    rightGroup.getChildren().clear();

	    if (!items.isEmpty()) {

		// If only on item exists
		if (items.size() == 1) {
		    centerGroup.getChildren().add(items.get(0));
		    centerIndex = 0;
		} else {

		    // LEFT,
		    for (int i = 0; i < centerIndex; i++)
			leftGroup.getChildren().add(items.get(i));

		    // CENTER,
		    if (centerIndex == items.size()) {
			centerGroup.getChildren().add(leftGroup.getChildren().get(centerIndex - 1));
		    } else
			centerGroup.getChildren().add(items.get(centerIndex));

		    // RIGHT
		    for (int i = items.size() - 1; i > centerIndex; i--)
			rightGroup.getChildren().add(items.get(i));

		}

		// stop old time line
		if (timeline.getStatus() == Status.RUNNING)
		    timeline.stop();

		// clear the old keyFrames
		timeline.getKeyFrames().clear();
		final ObservableList<KeyFrame> keyFrames = timeline.getKeyFrames();

		// LEFT KEYFRAMES
		for (int i = 0; i < leftGroup.getChildren().size(); i++) {

		    final User it = items.get(i);

		    double newX = -leftGroup.getChildren().size() *

			    SPACING + SPACING * i + LEFT_OFFSET;

		    keyFrames.add(new KeyFrame(duration,

			    new KeyValue(it.translateXProperty(), newX, interpolator),

			    new KeyValue(it.scaleXProperty(), SCALE_SMALL, interpolator),

			    new KeyValue(it.scaleYProperty(), SCALE_SMALL, interpolator)));

		    // new KeyValue(it.angle, 45.0, INTERPOLATOR)))

		}

		// CENTER ITEM KEYFRAME
		final User centerItem;
		if (items.size() == 1)
		    centerItem = items.get(0);
		else
		    centerItem = (User) centerGroup.getChildren().get(0);

		keyFrames.add(new KeyFrame(duration,

			new KeyValue(centerItem.translateXProperty(), 0, interpolator),

			new KeyValue(centerItem.scaleXProperty(), 1.0, interpolator),

			new KeyValue(centerItem.scaleYProperty(), 1.0, interpolator)));// ,

		// new KeyValue(centerItem.rotationTransform.angleProperty(),
		// 360)));

		// new KeyValue(centerItem.angle, 90, INTERPOLATOR)));

		// RIGHT KEYFRAMES
		for (int i = 0; i < rightGroup.getChildren().size(); i++) {

		    final User it = items.get(items.size() - i - 1);

		    final double newX = rightGroup.getChildren().size() *

			    SPACING - SPACING * i + RIGHT_OFFSET;

		    keyFrames.add(new KeyFrame(duration,

			    new KeyValue(it.translateXProperty(), newX, interpolator),

			    new KeyValue(it.scaleXProperty(), SCALE_SMALL, interpolator),

			    // new
			    // KeyValue(it.rotationTransform.angleProperty(),
			    // -360)));

			    new KeyValue(it.scaleYProperty(), SCALE_SMALL, interpolator)));

		    // new KeyValue(it.angle, 135.0, INTERPOLATOR)));

		}

		// play animation
		timeline.setAutoReverse(true);
		timeline.play();
	    }

	    // Previous and Next Visibility
	    if (rightGroup.getChildren().isEmpty())
		getNext().setVisible(false);
	    else
		getNext().setVisible(true);

	    if (leftGroup.getChildren().isEmpty())
		getPrevious().setVisible(false);
	    else
		getPrevious().setVisible(true);

	}

    }

}
