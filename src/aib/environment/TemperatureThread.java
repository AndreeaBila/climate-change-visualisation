package aib.environment;

import aib.Main;
import javafx.application.Platform;

/**
 * A separate thread to compute the world updates after the temperature changes
 */
public class TemperatureThread implements Runnable {
    /** The number of decades passed */
    int decades;
    /** Flag that determines if temperature should increase or decrease
     * we want to decrease when moving backwards in time (from 2030 to 2020) */
    boolean increase;

    /**
     * Create a new thread with the provided information
     * @param decades The number of decades passed
     * @param increase The flag that determines if temperature should increase
     */
    TemperatureThread(int decades, boolean increase) {
        this.decades = decades;
        this.increase = increase;
    }

    /**
     * When the thread starts, do the computations
     */
    @Override
    public void run() {
        // On the javafx application thread, display the loading animation
        Platform.runLater( () -> Main.userInterface.startLoading());
        // On this thread, do the temperature calculations and change the world
        World.updateMapCalculation(decades, increase);
        // Once the calculations are done, back on the javafx app thread stop displaying the loading animation
        Platform.runLater( () -> {
            World.prevYear = Main.userInterface.timelineSlider.getValue();
            Main.userInterface.stopLoading();
            Main.userInterface.showDialog(Main.userInterface.dialogText);
        });
    }


}
