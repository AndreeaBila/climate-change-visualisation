package aib.life;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A polar bear is one of the species in our world
 */
public class PolarBear extends Animal{

    /**
     * Create a new polar bear
     */
    public PolarBear() {
        super();
        this.setName("Polar Bear");
        this.setCompatibleTerrainsIDs(new ArrayList<>(Arrays.asList(1,2,3)));
        this.setImage(new Image("/bear.png"));
        this.setMinHeight(0f);
        this.setMaxHeight(1f);
        this.setHeightDifference(this.getMaxHeight() - this.getMinHeight());
        this.setMaxProb(0.01f);
    }
}
