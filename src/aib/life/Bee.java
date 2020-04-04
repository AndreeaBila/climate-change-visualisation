package aib.life;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A bee is one of the species in our world
 */
public class Bee extends Animal {

    /**
     * Create a new bee
     */
    public Bee() {
        super();
        this.setName("Bee");
        this.setCompatibleTerrainsIDs(new ArrayList<>(Arrays.asList(7,8)));
        this.setImage(new Image("/bee.png"));
        this.setMinHeight(0.55f);
        this.setMaxHeight(1f);
        this.setHeightDifference(this.getMaxHeight() - this.getMinHeight());
        this.setMaxProb(0.01f);
    }

}
