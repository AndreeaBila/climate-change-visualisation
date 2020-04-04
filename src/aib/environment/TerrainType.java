package aib.environment;

import javafx.scene.paint.Color;

/**
 * A terrain type has a descriptive name and a representative colour, and can be identified using its ID
 * e.g. name: "Grass", colour: Green, id: 123
 */
public class TerrainType {
    /** A descriptive name for this terrain type */
    private String name;
    /** The base colour for this type of terrain */
    private Color colour;
    /** The identifier for this terrain type */
    private int id;

    /**
     * Create a new terrain type
     * @param name The name for the new terrain type
     * @param colour The colour for the new terrain type
     * @param id The ID for the new terrain type
     */
    public TerrainType(String name, Color colour, int id) {
        this.setName(name);
        this.setColour(colour);
        this.id = id;
    }

    /**
     * Get the name of this terrain
     * @return The name of the terrain
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the terrain
     * @param name The new name for the terrain
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the colour for this terrain type
     * @return The colour of the terrain
     */
    public Color getColour() {
        return colour;
    }

    /**
     * Set a new colour for this terrain type
     * @param colour The new colour for this terrain
     */
    public void setColour(Color colour) {
        this.colour = colour;
    }

    /**
     * Get the ID of this terrain type
     * @return The id of this terrain type
     */
    public int getId() {
        return id;
    }
}

