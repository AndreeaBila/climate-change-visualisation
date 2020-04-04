package aib.life;

import javafx.scene.image.Image;

import java.util.List;

/**
 * An animal representation, with all of its information
 */
public abstract class Animal {
    /** The name of this type of animal */
    private String name;
    /** The x coordinate of this animal on the map */
    private int x;
    /** The y coordinate of this animal on the map  */
    private int y;
    /** The list of terrains that are compatible with this animal */
    private List<Integer> compatibleTerrainsIDs;
    /** The image object for this animal */
    private Image image;
    /** The minimum terrain height this animal can spawn at */
    private float minHeight;
    /** The maximum terrain height this animal can spawn at  */
    private float maxHeight;
    /** The maximum probability for this animal to spawn on an inhabitable pixel */
    private float maxProb;
    /** The difference between the maximum and the minimum height for this animal */
    private float heightDifference;
    /** The flag that determines whether the animal is alive or not */
    private boolean alive;

    /**
     * Create a new animal
     */
    public Animal() {
        this.x = 0;
        this.y = 0;
        this.alive = true;
    }

    /**
     * Get the animal type name
     * @return The animal type name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the animal type name
     * @param name The animal type name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the x coordinate of the animal on the map
     * @return The x coordinate of the animal on the map
     */
    public int getX() {
        return x;
    }

    /**
     * Set the x coordinate of the animal on the map
     * @param x The x coordinate of the animal on the map
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Get the y coordinate of the animal on the map
     * @return The y coordinate of the animal on the map
     */
    public int getY() {
        return y;
    }

    /**
     * Set the y coordinate of the animal on the map
     * @param y The y coordinate of the animal on the map
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Get the list of all terrains compatible with this animal
     * @return The list of all terrains compatible with this animal
     */
    public List<Integer> getCompatibleTerrainsIDs() {
        return compatibleTerrainsIDs;
    }

    /**
     * Set the list of all terrains compatible with this animal
     * @param compatibleTerrainsIDs The list of all terrains compatible with this animal
     */
    public void setCompatibleTerrainsIDs(List<Integer> compatibleTerrainsIDs) {
        this.compatibleTerrainsIDs = compatibleTerrainsIDs;
    }

    /**
     * Get the image object for this animal
     * @return The image object for this animal
     */
    public Image getImage() {
        return image;
    }

    /**
     * Set the image object for this animal
     * @param image The image object for this animal
     */
    public void setImage(Image image) {
        this.image = image;
    }

    /**
     * Get the minimum terrain height this animal can spawn at
     * @return The minimum terrain height this animal can spawn at
     */
    public float getMinHeight() {
        return minHeight;
    }

    /**
     * Set the minimum terrain height this animal can spawn at
     * @param minHeight The minimum terrain height this animal can spawn at
     */
    public void setMinHeight(float minHeight) {
        this.minHeight = minHeight;
    }

    /**
     * Get the maximum terrain height this animal can spawn at
     * @return The maximum terrain height this animal can spawn at
     */
    public float getMaxHeight() {
        return maxHeight;
    }

    /**
     * Set the maximum terrain height this animal can spawn at
     * @param maxHeight The maximum terrain height this animal can spawn at
     */
    public void setMaxHeight(float maxHeight) {
        this.maxHeight = maxHeight;
    }

    /**
     * Get the maximum probability for this animal to spawn on an inhabitable pixel
     * @return The maximum probability for this animal to spawn on an inhabitable pixel
     */
    public float getMaxProb() {
        return maxProb;
    }

    /**
     * Set the maximum probability for this animal to spawn on an inhabitable pixel
     * @param maxProb The maximum probability for this animal to spawn on an inhabitable pixel
     */
    public void setMaxProb(float maxProb) {
        this.maxProb = maxProb;
    }

    /**
     * Get the difference between the maximum and the minimum height for this animal
     * @return The difference between the maximum and the minimum height for this animal
     */
    public float getHeightDifference() {
        return heightDifference;
    }

    /**
     * Set the difference between the maximum and the minimum height for this animal
     * @param heightDifference The difference between the maximum and the minimum height for this animal
     */
    public void setHeightDifference(float heightDifference) {
        this.heightDifference = heightDifference;
    }

    /**
     * Return if the animal is alive or not
     * @return If the animal is alive or not
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Set the animal state, alive or not
     * @param alive The animal state
     */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
