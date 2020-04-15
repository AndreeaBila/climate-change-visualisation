package aib;

import aib.environment.TerrainSections;
import aib.environment.World;
import aib.life.Animal;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * The application renderer, used to handle all major map drawing
 */
public class Renderer {

    /**
     * Draw the map pixel by pixel
     * @param mapType The type of map to be drawn
     * @return The drawn map
     */
    public static Group drawPixels(String mapType) {
        // The container for the map, and the canvas used to draw it
        Group root = new Group();
        Canvas canvas = new Canvas(Constants.MAP_SIZE_X,Constants.MAP_SIZE_Y);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Draw Image Data
        PixelWriter pixelWriter = gc.getPixelWriter();

        // Set the colour for each pixel of the world, depending on the type of map that is being drawn
        for (int x = 0; x < World.pixels.length; x++) {
            for (int y = 0; y < World.pixels[x].length; y++) {
                Color colour;
                switch (mapType) {
                    // For the terrain height map, pixels will be a different intensity of gray
                    // based on their height (from the perlin noise height map)
                    case "Height":
                        if(World.pixels[x][y].getNoiseHeight() > 1) {colour = Color.gray(1.0);}
                        else if(World.pixels[x][y].getNoiseHeight() < 0) {colour = Color.gray(0.0);}
                        else {colour = Color.gray(World.pixels[x][y].getNoiseHeight());}
                        break;
                    // For the greenhouse gas map, pixels will have a colour between green and red
                    // the higher the greenhouse gas, the colour will be closer to red
                    // the lower the greenhouse gas, the colour will be closer to green
                    case "Greenhouse Gas":
                        if(World.pixels[x][y].getGreenhouseGasFactor() > 2) {
                            colour = Color.RED;
                        }
                        else if(World.pixels[x][y].getGreenhouseGasFactor() < 0.1) {
                            colour = Color.rgb(30,165,55);
                        }
                        else {
                            float intensity = scaleToRange(
                                    0.1f,2f,0f,1f,World.pixels[x][y].getGreenhouseGasFactor());
                            colour = Renderer.generateColor(Color.rgb(
                                    30, 165, 55), Color.RED,intensity, 0, 2);
                        }
                        break;
                    // For the temperature map, the pixel's colour will be determined by its temperature
                    case "Temperature": colour = World.pixels[x][y].getTemperature().colour; break;
                    // For the default case, which covers terrain maps, the pixel's colour will be determined by its terrain type
                    default: colour = World.pixels[x][y].getColour();
                }

                // Draw the equator line halfway on the map
                if(Main.userInterface.showEquatorLineCheck.isSelected())
                    if(y >= (Constants.MAP_SIZE_Y/2)-1 && y <= (Constants.MAP_SIZE_Y/2))
                        colour = Color.BLACK;

                pixelWriter.setColor(x, y, colour);
            }
        }

        root.getChildren().add(canvas);
        return root;
    }

    /**
     * Scale any value from the current range to a new range (e.g scaling 5 from range 0-10 to range 0-100 will return 50)
     * @param rmin Current range minimum value
     * @param rmax Current range maximum value
     * @param tmin New range minimum value
     * @param tmax New range maximum value
     * @param m The value you want scaled
     * @return The scaled value
     */
    public static float scaleToRange(float rmin, float rmax, float tmin, float tmax, float m) {
        m = ((m - rmin) / (rmax - rmin)) * (tmax - tmin) + tmin;
        return m;
    }

    /**
     * Generate a colour that is a mix between two colours with a ration based on an intensity value
     * if intensity ( 0 > intensity > 1) is closer to 0, the mixed colour will be closer to the first colour
     * if intensity ( 0 > intensity > 1) is closer to 1, the mixed colour will be closer to the second colour
     * @param col1 The first colour to mix
     * @param col2 The second colour to mix
     * @param intensity The intensity value
     * @param function Flag that indicates if these particular colours should mix or not
     * @param SHARPEN Value that determines how much the colour should mix
     *                i.e. a high level of gradient (transition is smooth) or an abrupt transition (you can see transition lines)
     * @return The new colour
     */
    public static Color generateColor(Color col1, Color col2, float intensity, int function, double SHARPEN) {
        // If function is 0, use SHARPEN to determine how much to mix colours
        if (function == 0)
            intensity = (float) Math.pow(intensity, SHARPEN);
        // Otherwise, make the transition more abrupt
        // (used for ice, as ice does not blend into other terrains
        //  but rather is sharp and you can clearly see where it begins and ends)
        else if (function == 1)
            intensity = (float) Math.pow(intensity, 64);
        else
            intensity = 0;

        // Calculate new colour
        double newRed   = (col1.getRed() + (col2.getRed() - col1.getRed()) * intensity) * 255;
        double newGreen   = (col1.getGreen() + (col2.getGreen() - col1.getGreen()) * intensity) * 255;
        double newBlue   = (col1.getBlue() + (col2.getBlue() - col1.getBlue()) * intensity) * 255;

        return Color.rgb((int)newRed, (int)newGreen, (int)newBlue);
    }

    /**
     * Draw the animals on the map
     */
    public static void drawAnimals() {
        int imgWidth = 40;
        int x;
        int y;
        // Greyscale filter for when animals are dead
        ColorAdjust greyscale = new ColorAdjust();
        greyscale.setBrightness(-0.8);
        greyscale.setHue(0.0);

        // Display the image for each animal
        for(Animal animal : World.getAnimals()) {
            ImageView iv = new ImageView();
            iv.setImage(animal.getImage());

            // set its position (the center of the image will be on the animal coordinates)
            x = animal.getX() - imgWidth/2;
            y = animal.getY() - imgWidth/2;
            // but not outside the map boundaries
            if(x < 0) x = 0;
            if(y < 0) y = 0;
            iv.setX(x);
            iv.setY(y);
            iv.setFitWidth(imgWidth);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            iv.setCache(true);

            // Differentiate between alive and dead animals using the greyscale filter
            if(!animal.isAlive())
                iv.setEffect(greyscale);

            // Add the image on top of the map
            Main.userInterface.map.getChildren().add(iv);

            // When the animal is clicked, informed the user of its state
            iv.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                // Create a string of all the compatible habitats
                String habitats = "";
                for(int id: animal.getCompatibleTerrainsIDs()) {
                    habitats += TerrainSections.getTerrainByID(id).getName() + ", ";
                }
                habitats = habitats.substring(0, habitats.length()-2);

                Main.userInterface.printToUserTextBox("--------------------------------------------------------");
                Main.userInterface.printToUserTextBox("Species: " + animal.getName());
                Main.userInterface.printToUserTextBox("Animal center coordinates: x: " + animal.getX() + " y: " + animal.getY());
                Main.userInterface.printToUserTextBox("Habitats: " + habitats);
                Main.userInterface.printToUserTextBox("Current terrain: " + World.pixels[animal.getX()][animal.getY()].getTerrainType().getName());
                Main.userInterface.printToUserTextBox("State: " + ((animal.isAlive()) ? "alive" : "dead"));

            });
        }
    }
}
