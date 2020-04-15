package aib.environment;

import aib.Main;
import aib.UserInterface;
import javafx.application.Platform;

/**
 * A separate thread that deals with the computations of creating a new world
 */
public class WorldThread implements Runnable {
    /** The seed the world is created from */
    int inputSeed;
    /** The number of octaves for the Perlin noise */
    int inputOctaves;
    /** The scale for the Perlin noise */
    float inputScale;
    /** The persistence for the perlin noise */
    float inputPersistence;
    /** The lacunarity for the perlin noise */
    float inputLacunarity;
    /** The offset for the perlin noise */
    float offsetX;
    float offsetY;
    /** The application userInterface */
    UserInterface userInterface;

    /**
     * Create a new world calculation thread
     * @param inputSeed Seed for the world
     * @param inputScale Scale for the noise
     * @param inputOctaves Octaves for the noise
     * @param inputPersistence Persistence for the noise
     * @param inputLacunarity Lacunarity for the noise
     * @param offsetX Horizontal offset for the noise
     * @param offsetY Vertical offset for the noise
     */
    public WorldThread(int inputSeed, float inputScale, int inputOctaves, float inputPersistence, float inputLacunarity, float offsetX, float offsetY) {
        this.inputLacunarity = inputLacunarity;
        this.inputOctaves = inputOctaves;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.inputSeed = inputSeed;
        this.inputScale = inputScale;
        this.inputPersistence = inputPersistence;
        this.userInterface = Main.userInterface;
    }

    /**
     * Once the thread start, create the new world
     */
    @Override
    public void run() {
        // On the javafx application thread, start the loading animation
        Platform.runLater( () -> userInterface.startLoading());
        // On this thread, create the new world map
        World.calculateMap(inputSeed, inputScale, inputOctaves, inputPersistence, inputLacunarity, offsetX,offsetY);
        // Back on the javafx application thread, stop the loading animation
        Platform.runLater( () -> userInterface.stopLoading());
    }
}
