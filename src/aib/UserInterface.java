package aib;

import aib.environment.World;
import aib.environment.WorldThread;
import aib.libraries.FastNoise;
import aib.libraries.ZoomableScrollPane;
import aib.life.Life;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * UserInterface class, which handles building the application interaction interface.
 */
public class UserInterface {
    /** Generic slider width */
    public final int SLIDER_WIDTH = 380;
    /** UserInterface container */
    public VBox menu;
    /** Message box where information is displayed to the user */
    public TextArea messageText;
    /** Boolean to determine if the map should auto update after settings are changed */
    public boolean autoUpdate;
    /** List of available map types to draw */
    public ComboBox<String> mapTypeCombo;
    /** Field for the seed used to generate the map */
    public TextField seedField;
    /** Button to randomise the map seed */
    public Button randomSeed;
    /** Button to generate animals */
    public Button generateAnimals;
    /** Boolean to determine if the animals should be shown or hidden on the map */
    public boolean showAnimals;
    /** Checkbox to select if animals are shown or hidden */
    public CheckBox showAnimalsCheck;
    /** Boolean to determine if the equator line show be displayed or hidden **/
    public boolean showEquatorLine;
    /** CheckBox to show or hide the equator line **/
    public CheckBox showEquatorLineCheck;

    /** Map containers -> ZoomableScrollPane to zoom and scroll */
    public ZoomableScrollPane mapZoomablePane;
    /** VBox nested in ZoomableScrollPane with editable children because scrollPane is non-modifiable */
    public Group mapHolder;
    /** Map, nested in VBox nested in ZoomableScrollPane */
    public Group map;

    /** Information dialog */
    public String dialogText;
    public VBox dialogOverlay;

    /** All the settings sliders */
    public Slider timelineSlider, scaleSlider, octavesSlider, lacunaritySlider, persistenceSlider, offsetXSlider, offsetYSlider;
    /** All the slider value labels */
    public Label timelineValue, scaleValue, octavesValue, lacunarityValue, persistenceValue, offsetXValue, offsetYValue;
    /** Noise type list **/
    public ComboBox<FastNoise.NoiseType> noiseTypeComboBox;

    /** Loading animation */
    public ProgressIndicator progressIndicator;
    public ColorAdjust loadingEffect;

    /**
     * UserInterface constructor, that sets up all the prerequisite initialisations
     */
    public UserInterface() {
        // UserInterface container
        menu = new VBox();
        // Map containers
        mapHolder = new Group();
        mapZoomablePane = new ZoomableScrollPane(mapHolder);
        mapZoomablePane.setMinHeight(400.0);
        mapZoomablePane.setMinWidth(400.0);
        mapZoomablePane.setPrefSize(1300.0, 1020.0);
        map = new Group();

        // Information boxes
        messageText = new TextArea("Interact with the map!\n");
        dialogText = "";
        // Use VBox overlay so the dialog is always centered on the screen irrespective of its size
        // (size varies because the text varies)
        dialogOverlay = new VBox();
        dialogOverlay.setPrefSize(1300,1000);

        dialogOverlay.setAlignment(Pos.CENTER);

        // Loading animation
        progressIndicator = new ProgressIndicator();
        loadingEffect = new ColorAdjust();
        progressIndicator.setLayoutX(645);
        progressIndicator.setLayoutY(475);

        // Initialise loading effect - when applied, map is darker to signify it is not ready
        // (loading animation will also be shown on top)
        loadingEffect.setBrightness(-0.5);
        loadingEffect.setHue(0.0);

        // Important menu settings
        mapTypeCombo = new ComboBox<>();
        seedField = new TextField();
        randomSeed = new Button("Random");
        generateAnimals = new Button("Generate Animals");
        showAnimalsCheck = new CheckBox();
        showEquatorLineCheck = new CheckBox();
        initSliders();
        noiseTypeComboBox = new ComboBox<FastNoise.NoiseType>();

        // Do not allow the user to edit the contents of the information box
        // its purpose is purely to provide additional information
        messageText.setEditable(false);
    }

    /**
     * Determine the initial state of all sliders and slider labels in the menu
     */
    public void initSliders() {
        timelineSlider = new Slider(2000, 2100, 2000);
        timelineValue = new Label("" + (int)timelineSlider.getValue());
        octavesSlider = new Slider(1, 6, 5);
        octavesValue = new Label("" + (int) octavesSlider.getValue());
        persistenceSlider = new Slider(0, 1, 0.4);
        persistenceValue = new Label(Double.toString(persistenceSlider.getValue()));
        lacunaritySlider = new Slider(1, 5, 3);
        lacunarityValue = new Label(Double.toString(lacunaritySlider.getValue()));
        scaleSlider = new Slider(0, 30, 2.5);
        scaleValue = new Label(Double.toString(scaleSlider.getValue()));
        offsetXSlider = new Slider(-5000, 5000, 0);
        offsetXValue = new Label("" + (int) offsetXSlider.getValue());
        offsetYSlider = new Slider(-5000, 5000, 0);
        offsetYValue = new Label("" + (int) offsetYSlider.getValue());
    }

    /**
     * Start loading behaviour
     */
    public void startLoading() {
        dismissDialog();
        progressIndicator.setVisible(true);
        map.setEffect(Main.userInterface.loadingEffect);
        disableMenuItems();
    }

    /**
     * End loading behaviour
     */
    public void stopLoading() {
        drawMap();
        progressIndicator.setVisible(false);
        map.setEffect(null);
        enableMenuItems();
    }

    /**
     * Print a new message to the user information box
     * @param text The message you want to print
     */
    public void printToUserTextBox(String text) {
        // Ensuring we are on the right thread (the javafx application thread) because interface can only be modified on this thread
        Platform.runLater(() -> {
            // Add the message to the text box
            messageText.appendText(text + "\n");
        });
    }

    /**
     * Create and draw a new map with the setting from the menu
     */
    private void createMap() {
        dismissDialog();
        // Print divider before printing new information, for clarity
        printToUserTextBox("--------------------------------------------------------");

        // Start tracking how long generating and drawing takes
        long st = System.nanoTime();

        System.out.println("Generating world and map...");

        // Hide the show animals checkbox until animals are generated
        showAnimalsCheck.setVisible(false);
        generateAnimals.setVisible(true);

        // Whenever a new map is created, reset the time slider to its initial position
        timelineSlider.setValue(2000);

        // Get the map settings from the menu
        int inputSeed = Integer.parseInt(seedField.getText());
        int inputOctaves = (int) octavesSlider.getValue();
        float inputScale = (float) scaleSlider.getValue();
        float inputPersistence = (float) persistenceSlider.getValue();
        float inputLacunarity = (float) lacunaritySlider.getValue();
        int inputOffsetX = (int) offsetXSlider.getValue();
        int inputOffsetY = (int) offsetYSlider.getValue();

        // On a separate thread, generate the perlin noise map
        WorldThread x = new WorldThread(inputSeed, inputScale, inputOctaves, inputPersistence, inputLacunarity, inputOffsetX, inputOffsetY);
        (new Thread(x)).start();

        // Display elapsed time
        double elapsed = ((double) System.nanoTime() - st) / 1_000_000_000.0;
        System.out.println("World and map generated");//in " + elapsed + " seconds");
    }

    /**
     * Draw or redraw the map with the latest information
     */
    public void drawMap() {
        dismissDialog();
        // Remove existing map from the container
        mapHolder.getChildren().remove(map);

        // Draw the type of map the user has selected - i.e. terrain, temperature etc.
        String mapType = mapTypeCombo.getValue();
        map = Renderer.drawPixels(mapType);

        // When the user clicks any point on the map, provide all the information for that location
        map.addEventHandler(MouseEvent.MOUSE_CLICKED, m -> {
            int x = (int) m.getX();
            int y = (int) m.getY();

            printToUserTextBox("--------------------------------------------------------");
            printToUserTextBox("x:" + x + " y:" + y);
            printToUserTextBox("Latitude: " + World.pixels[x][y].getLatitude() + "\u00B0");
            printToUserTextBox("Noise height: " + World.pixels[x][y].getNoiseHeight());
            printToUserTextBox("Height in meters: " + Math.round(World.pixels[x][y].getTerrainHeight()));
            printToUserTextBox("Terrain: " + World.pixels[x][y].getTerrainType().getName());
            printToUserTextBox("Temperature: " + String.format("%.2f", World.pixels[x][y].getTemperature().value) + "\u00B0" + "C");
            printToUserTextBox("Greenhouse Gas Factor: " + World.pixels[x][y].getGreenhouseGasFactor());

        });

        // Draw the animals in the world
        if(showAnimals) Renderer.drawAnimals();

        // Add the newly drawn map to the map container
        mapHolder.getChildren().add(0, map);
    }

    /**
     * Create the application node, with the menu and the map
     * @return The node that will be used as scene root, containing the entire application
     */
    public HBox buildWindow() {
        // Main application container
        HBox root = new HBox();

        // Grid with menu options that are always on display
        // Providing the most important functionality to the user
        GridPane importantGrid = new GridPane();
        // Set layout
        importantGrid.setPadding(new Insets(0, 5, 20, 10));
        importantGrid.setVgap(25);
        importantGrid.setHgap(15);
        importantGrid.setPrefWidth(600);

        // Auto update checkbox
        // allowing the user to decide if the map should update immediately when a setting is changed
        CheckBox autoUpdateCheck = new CheckBox();
        autoUpdateCheck.setSelected(true);
        autoUpdate = autoUpdateCheck.isSelected();
        autoUpdateCheck.setText("Auto Update Map");
        autoUpdateCheck.setTextFill(Color.WHITE);
        autoUpdateCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            autoUpdate = newValue;
            if (newValue) createMap();
        });
        GridPane.setConstraints(autoUpdateCheck, 1, 1);
        GridPane.setColumnSpan(autoUpdateCheck, 3);
        importantGrid.getChildren().add(autoUpdateCheck);

        // Editable field for the seed used to generate the map
        // The user can decide what seed they want to use, or randomise it using the next button
        Label seedLabel = new Label("Seed: ");
        seedField.setText("1");
        GridPane.setConstraints(seedLabel, 0, 2);
        importantGrid.getChildren().add(seedLabel);
        GridPane.setConstraints(seedField, 1, 2);
        importantGrid.getChildren().add(seedField);

        // Button to randomise the map seed
        GridPane.setConstraints(randomSeed, 2, 2);
        importantGrid.getChildren().add(randomSeed);
        randomSeed.getStyleClass().add("menuButton");
        Random r = new Random();
        randomSeed.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            int s = r.nextInt();
            seedField.setText(s + "");
        });

        // Dropdown list that allows the user to select which type of map they want to draw
        Label mapTypeLabel = new Label("Map Type:");
        mapTypeCombo.getItems().addAll("Height", "Temperature", "Terrain", "Greenhouse Gas");
        mapTypeCombo.setValue("Terrain");
        mapTypeCombo.setPrefWidth(SLIDER_WIDTH);
        GridPane.setConstraints(mapTypeLabel, 0, 3);
        importantGrid.getChildren().add(mapTypeLabel);
        GridPane.setConstraints(mapTypeCombo, 1, 3);
        importantGrid.getChildren().add(mapTypeCombo);

        // Button to generate animals on the existing map
        GridPane.setConstraints(generateAnimals, 1, 4);
        importantGrid.getChildren().add(generateAnimals);
        generateAnimals.getStyleClass().add("menuButton");
        generateAnimals.setPrefWidth(SLIDER_WIDTH);
        generateAnimals.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            // On a separate thread, do the calculations to generate the animals
            (new Thread(() -> {
                // On the javafx application thread (inside Platform.runLater())
                // show the loading animation and apply the loading effect
                Platform.runLater(() -> {
                    // Create animals in 2000
                    Main.userInterface.startLoading();
                });
                // On the separate thread, create the animals
                try {

                    Life.generateLife();
                } catch (IllegalAccessException e) {
                    System.out.println("Illegal Access Exception in menu when generating animals");
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    System.out.println("Instantiation Exception in menu when generating animals");
                    e.printStackTrace();
                }
                // Once animals are created, stop the loading animation and draw the animals on the map
                Platform.runLater(() -> {
                    Main.userInterface.stopLoading();
                    Main.userInterface.showAnimalsCheck.setVisible(true);
                    Main.userInterface.generateAnimals.setVisible(false);
                });
            })).start();
        });

        // Show animals checkbox
        // allowing the user to decide if the map should show the animals or not
        showAnimalsCheck.setSelected(true);
        showAnimals = showAnimalsCheck.isSelected();
        showAnimalsCheck.setText("Show Animals");
        showAnimalsCheck.setTextFill(Color.WHITE);
        showAnimalsCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showAnimals = newValue;
            drawMap();
        });
        GridPane.setConstraints(showAnimalsCheck, 1, 4);
        importantGrid.getChildren().add(showAnimalsCheck);


        // Time slider, which allows the user to observe changes on the map through time
        Label timelineLabel = new Label("Year: ");
        timelineSlider.setBlockIncrement(10f);
        timelineSlider.setShowTickMarks(true);
        timelineSlider.setShowTickLabels(true);
        timelineSlider.setMajorTickUnit(10);
        timelineSlider.setMinorTickCount(0);
        timelineSlider.setSnapToTicks(true);
        GridPane.setConstraints(timelineLabel, 0, 6);
        importantGrid.getChildren().add(timelineLabel);
        GridPane.setConstraints(timelineSlider, 1, 6);
        importantGrid.getChildren().add(timelineSlider);
        GridPane.setConstraints(timelineValue, 2, 6);
        importantGrid.getChildren().add(timelineValue);

        // Grid with map settings, which is collapsible
        // it is useful but not essential to the user
        GridPane mapSettingsGrid = new GridPane();
        mapSettingsGrid.setPadding(new Insets(0, 5, 10, 10));
        mapSettingsGrid.setVgap(30);
        mapSettingsGrid.setHgap(25);
        mapSettingsGrid.setPrefWidth(600);
        mapSettingsGrid.setMinWidth(400);

        // Show equator line checkbox
        showEquatorLineCheck.setSelected(true);
        showEquatorLine = autoUpdateCheck.isSelected();
        showEquatorLineCheck.setText("Show equator line");
        showEquatorLineCheck.setTextFill(Color.WHITE);
        showEquatorLineCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showEquatorLine = newValue;
            drawMap();
        });
        GridPane.setConstraints(showEquatorLineCheck, 1, 1);
        GridPane.setColumnSpan(showEquatorLineCheck, 3);
        mapSettingsGrid.getChildren().add(showEquatorLineCheck);

        // Noise type combo list
        Label noiseTypeLabel = new Label("Noise Type:");
        ObservableList<FastNoise.NoiseType> noiseTypes = FXCollections.observableArrayList(
                FastNoise.NoiseType.Perlin, FastNoise.NoiseType.Simplex,
                FastNoise.NoiseType.Cubic, FastNoise.NoiseType.Value, FastNoise.NoiseType.PerlinFractal,
                FastNoise.NoiseType.SimplexFractal, FastNoise.NoiseType.CubicFractal, FastNoise.NoiseType.ValueFractal);
        noiseTypeComboBox.getItems().addAll(noiseTypes);
        noiseTypeComboBox.setValue(FastNoise.NoiseType.Perlin);
        noiseTypeComboBox.setPrefWidth(SLIDER_WIDTH);
        GridPane.setConstraints(noiseTypeLabel, 0, 2);
        mapSettingsGrid.getChildren().add(noiseTypeLabel);
        GridPane.setConstraints(noiseTypeComboBox, 1, 2);
        mapSettingsGrid.getChildren().add(noiseTypeComboBox);

        // Noise scale slider
        Label scaleLabel = new Label("Scale: ");
        scaleSlider.setBlockIncrement(1f);
        scaleSlider.setShowTickMarks(true);
        scaleSlider.setShowTickLabels(true);
        scaleSlider.setMajorTickUnit(5);
        scaleSlider.setMinorTickCount(4);
        scaleSlider.setPrefWidth(SLIDER_WIDTH);
        GridPane.setConstraints(scaleLabel, 0, 3);
        mapSettingsGrid.getChildren().add(scaleLabel);
        GridPane.setConstraints(scaleSlider, 1, 3);
        mapSettingsGrid.getChildren().add(scaleSlider);
        GridPane.setConstraints(scaleValue, 2, 3);
        mapSettingsGrid.getChildren().add(scaleValue);

        // Noise offset x slider
        Label offsetXLabel = new Label("Offset X: ");
        offsetXSlider.setBlockIncrement(1f);
        offsetXSlider.setShowTickMarks(true);
        offsetXSlider.setShowTickLabels(true);
        offsetXSlider.setMajorTickUnit(2500);
        offsetXSlider.setMinorTickCount(4);
        GridPane.setConstraints(offsetXLabel, 0, 4);
        mapSettingsGrid.getChildren().add(offsetXLabel);
        GridPane.setConstraints(offsetXSlider, 1, 4);
        mapSettingsGrid.getChildren().add(offsetXSlider);
        GridPane.setConstraints(offsetXValue, 2, 4);
        mapSettingsGrid.getChildren().add(offsetXValue);

        // Noise offset y slider
        Label offsetYLabel = new Label("Offset Y: ");
        offsetYSlider.setBlockIncrement(1f);
        offsetYSlider.setShowTickMarks(true);
        offsetYSlider.setShowTickLabels(true);
        offsetYSlider.setMajorTickUnit(2500);
        offsetYSlider.setMinorTickCount(4);
        GridPane.setConstraints(offsetYLabel, 0, 5);
        mapSettingsGrid.getChildren().add(offsetYLabel);
        GridPane.setConstraints(offsetYSlider, 1, 5);
        mapSettingsGrid.getChildren().add(offsetYSlider);
        GridPane.setConstraints(offsetYValue, 2, 5);
        mapSettingsGrid.getChildren().add(offsetYValue);

        // Noise octaves slider
        Label octavesLabel = new Label("Octaves: ");
        octavesSlider.setBlockIncrement(1f);
        octavesSlider.setShowTickMarks(true);
        octavesSlider.setShowTickLabels(true);
        octavesSlider.setMajorTickUnit(1);
        octavesSlider.setMinorTickCount(0);
        octavesSlider.setSnapToTicks(true);
        GridPane.setConstraints(octavesLabel, 0, 6);
        mapSettingsGrid.getChildren().add(octavesLabel);
        GridPane.setConstraints(octavesSlider, 1, 6);
        mapSettingsGrid.getChildren().add(octavesSlider);
        GridPane.setConstraints(octavesValue, 2, 6);
        mapSettingsGrid.getChildren().add(octavesValue);

        // Noise lacunarity slider
        Label lacunarityLabel = new Label("Lacunarity: ");
        lacunaritySlider.setShowTickMarks(true);
        lacunaritySlider.setShowTickLabels(true);
        lacunaritySlider.setMajorTickUnit(.5);
        lacunaritySlider.setMinorTickCount(4);
        GridPane.setConstraints(lacunarityLabel, 0, 7);
        mapSettingsGrid.getChildren().add(lacunarityLabel);
        GridPane.setConstraints(lacunaritySlider, 1, 7);
        mapSettingsGrid.getChildren().add(lacunaritySlider);
        GridPane.setConstraints(lacunarityValue, 2, 7);
        mapSettingsGrid.getChildren().add(lacunarityValue);

        // Noise persistence slider
        Label persistenceLabel = new Label("Persistence: ");
        persistenceSlider.setShowTickMarks(true);
        persistenceSlider.setShowTickLabels(true);
        persistenceSlider.setMajorTickUnit(.1);
        persistenceSlider.setMinorTickCount(4);
        GridPane.setConstraints(persistenceLabel, 0, 8);
        mapSettingsGrid.getChildren().add(persistenceLabel);
        GridPane.setConstraints(persistenceSlider, 1, 8);
        mapSettingsGrid.getChildren().add(persistenceSlider);
        GridPane.setConstraints(persistenceValue, 2, 8);
        mapSettingsGrid.getChildren().add(persistenceValue);

        // Add on change event handlers for all menu settings, so they can auto update
        addHandlers();

        // Button that forces the map to regenerate
        // Useful when auto update is off
        // or when you want to remove animals and start fresh but with the same settings as before
        Button generate = new Button("Generate Map (clean)");
        generate.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> createMap());

        // Button to toggle the map settings part of the menu
        Button mapSettingsCollapse = new Button("Map Settings +");

        // Additional button styling
        generate.getStyleClass().add("menuButton");
        mapSettingsCollapse.getStyleClass().add("menuButton");
        generate.setPrefWidth(SLIDER_WIDTH);
        generate.getStyleClass().add("bottomButton");
        mapSettingsCollapse.setPrefWidth(SLIDER_WIDTH);

        // Add all menu elements to the menu container
        menu.getChildren().addAll(importantGrid, mapSettingsCollapse, generate);
        menu.getStyleClass().add("menuBox");
        menu.prefWidth(600);
        menu.prefHeight(600);
        menu.setMinWidth(600);

        // Make the menu scrollable
        ScrollPane menuScrollPane = new ScrollPane(menu);
        menuScrollPane.setPrefSize(620, 558);
        menuScrollPane.setMinWidth(620);

        // Additional menu styling
        messageText.getStyleClass().add("messageTextArea");
        messageText.setPrefSize(620, 555);
        messageText.setMinSize(620,200);


        // Hide or show settings
        mapSettingsCollapse.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (mapSettingsCollapse.getText().equals("Map Settings -")) {
                menu.getChildren().remove(mapSettingsGrid);
                mapSettingsCollapse.setText("Map Settings +");
                messageText.setPrefHeight(555);
                menuScrollPane.setPrefHeight(558);
                menuScrollPane.vvalueProperty().unbind();
            } else if (mapSettingsCollapse.getText().equals("Map Settings +")) {
                menu.getChildren().add(2, mapSettingsGrid);
                mapSettingsCollapse.setText("Map Settings -");
                messageText.setPrefHeight(200);
                menuScrollPane.setPrefHeight(1800);
            }
        });
        // When settings menu is expanded, scroll down to it.
        menu.heightProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldVal, Object newVal) {
                menuScrollPane.setVvalue((Double)newVal);
            }
        });

        // Left side of the application, with the menu and the information box
        VBox finalMenu = new VBox(menuScrollPane, messageText);
        finalMenu.getStyleClass().add("finalMenu");

        // Application root node, with the menu on the left and the map on the right
        root.getChildren().addAll(finalMenu, mapZoomablePane);

        // Draw map on start
        createMap();

        // Set the position of the loading animation for when it appears
        mapHolder.getChildren().addAll(map,progressIndicator);

        return root;
    }


    /**
     * Set events that need to happen when any map setting is changed
     */
    public void addHandlers() {
        // When the user changes the map type, redraw the map if auto update is on
        mapTypeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (autoUpdate) {
                drawMap();
            }
        });

        // When the user changes the seed field, create a new map with the new seed if auto update is on
        seedField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("(-)?\\d{1,10}")) {
                seedField.setText(oldValue);
            } else if (autoUpdate) createMap();
        });

        // When the user changes the time (year), update the year label to show where the pointer is
        timelineSlider.valueProperty().addListener((ov, old_val, new_val) -> {
            int newV = new_val.intValue();
            int oldV = old_val.intValue();
            if (newV != oldV) {
                timelineSlider.setValue(newV);
                timelineValue.setText("" + newV);
            }
        });

        // When the user releases the mouse and changes the time (year), update the map accordingly
        timelineSlider.setOnMouseReleased(event -> {
            if (timelineSlider.getValue() != World.prevYear) {
                printToUserTextBox("--------------------------------------------------------");
                dialogText = "";
                timelineValue.setText("" + (int)timelineSlider.getValue());
                if (timelineSlider.getValue() > World.prevYear) {
                    World.updateMap(((int) timelineSlider.getValue() - (int) World.prevYear) / 10, true);
                }
                else if (timelineSlider.getValue() < World.prevYear) {
                    World.updateMap(((int) World.prevYear - (int) timelineSlider.getValue()) / 10, false);
                }
            }
        });

        // When the user changes the noise type, create a new world if auto update is on
        noiseTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (autoUpdate) {
                createMap();
            }
        });

        // When the user changes the scale, create a new map with the new scale if auto update is on
        floatSlidersHandler(scaleSlider, scaleValue);
        // When the user changes the lacunarity or persistence, create a new map with the new information is auto update is on
        floatSlidersHandler(lacunaritySlider, lacunarityValue);
        floatSlidersHandler(persistenceSlider, persistenceValue);
        // When the user changes the number of octaves, create a new map with the new octaves if auto update is on
        intSlidersHandler(octavesSlider, octavesValue);
        // When the user changes the offset, create a new map with that offset if auto update is on
        intSlidersHandler(offsetXSlider, offsetXValue);
        intSlidersHandler(offsetYSlider, offsetYValue);
    }

    /**
     * Handle sliders with float values
     * @param slider The slider to apply the event handler on
     * @param value The label which holds the slider value
     */
    public void floatSlidersHandler(Slider slider, Label value) {
        // When moving the slider, update value label too see the value you are on
        slider.valueProperty().addListener((ov, old_val, new_val) -> {
            if (!new_val.equals(old_val)) {
                slider.setValue(new_val.doubleValue());
                value.setText(String.format("%.2f", new_val));
            }
        });

        // When the mouse is released after moving the slider, create the map with this new setting
        slider.setOnMouseReleased(event -> {
            if (autoUpdate) createMap();
        });
    }

    /**
     * Handle sliders with int values
     * @param slider The slider to apply the event handler on
     * @param value The label which holds the slider value
     */
    public void intSlidersHandler(Slider slider, Label value) {
        slider.valueProperty().addListener((ov, old_val, new_val) -> {
            int newV = new_val.intValue();
            int oldV = old_val.intValue();
            if (newV != oldV) {
                slider.setValue(newV);
                value.setText("" + newV);
            }
        });

        slider.setOnMouseReleased(event -> {
            if (autoUpdate) createMap();
        });
    }


    /**
     * Allow all sliders to be modifiable
     */
    public void enableMenuItems() {
        toggleMenuItems(false);
    }

    /**
     * Block all sliders from being modifiable
     */
    public void disableMenuItems() {
        toggleMenuItems(true);
    }

    /**
     * Enable or disable menu items from being modifiable
     * @param flag If the menu items should be disabled or not (true => disable, false => enable)
     */
    public void toggleMenuItems(boolean flag) {
        seedField.setDisable(flag);
        randomSeed.setDisable(flag);
        generateAnimals.setDisable(flag);
        timelineSlider.setDisable(flag);
        scaleSlider.setDisable(flag);
        lacunaritySlider.setDisable(flag);
        octavesSlider.setDisable(flag);
        persistenceSlider.setDisable(flag);
        offsetXSlider.setDisable(flag);
        offsetYSlider.setDisable(flag);
    }

    /**
     * Show a dialog with information to the user
     * @param text The text you want displayed on the dialog
     */
    public void showDialog(String text) {
        // Button to dismiss the dialog
        Button closeButton = new Button("x");
        closeButton.getStyleClass().add("dismissButton");

        // Using label as dialog for simplicity as it offers all the required functionality
        Label label = new Label("UPDATES\n\n" + text);
        // Add dismiss button to label
        label.setGraphic(closeButton);
        label.setContentDisplay(ContentDisplay.RIGHT);
        // Style label
        label.getStyleClass().add("infoDialog");
        label.setAlignment(Pos.CENTER);
        // Using a VBox overlay so that the label is centered on the screen irrespective of its size
        dialogOverlay.getChildren().add(label);
        mapHolder.getChildren().add(dialogOverlay);

        // Dismiss the dialog when the dismiss button is clicked or any point on the map is clicked
        closeButton.setOnAction(event -> dismissDialog());
        dialogOverlay.setOnMouseClicked(event -> dismissDialog());
        // But don't dismiss it when the anything on the dialog (except the button) is clicked
        label.setOnMouseClicked(Event::consume);
    }

    /**
     * Dismiss the information dialog
     */
    public void dismissDialog() {
        // First check that the dialog is on the screen
        if(mapHolder.getChildren().contains(dialogOverlay)) {
            // Then remove the dialog from the overlay
            dialogOverlay.getChildren().clear();
            // And finally remove the overlay
            mapHolder.getChildren().remove(dialogOverlay);
        }
    }
}