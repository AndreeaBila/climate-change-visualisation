package aib.life;

import aib.Main;
import aib.environment.Pixel;
import aib.environment.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Deals with generating animals on the existing world map
 */
public class Life {

    /** A list of all the species we want to generate in the world */
    public static final List<Animal> species = new ArrayList<>(Arrays.asList(new PolarBear(),new Bee()));

    /**
     * Generate animals on the map
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static void generateLife()throws IllegalAccessException, InstantiationException {
        // Remove the previous animals
        World.getAnimals().clear();

        List<Animal> animals = new ArrayList<>();
        // Use the same seed as for the map to generate the same animals every time for this map
        Random rand = new Random(Integer.parseInt(Main.userInterface.seedField.getText()));

        // For each pixel, find and spawn animals based on probabilities
        for (int x = 0; x < World.pixels.length - 40; x++) {
            for (int y = 0; y < World.pixels[x].length - 40; y++) {
                // Get the current pixel
                Pixel pixel = World.pixels[x][y];

                // Iterate over all available animals species to find ones that are compatible with the current pixel
                for (Animal animal : species) {

                    // If current animal can live in current terrain type
                    if (animal.getCompatibleTerrainsIDs().contains(pixel.getTerrainType().getId())) {
                        // And if it can spawn at this terrain height, calculate a probability for it to spawn
                        if (pixel.getNoiseHeight() < animal.getMaxHeight() && pixel.getNoiseHeight() > animal.getMinHeight()) {

                            // The maximum probability for this type of animal to spawn (how "popular" it is)
                            // realistically, we don't want animals on every pixel as there are 1.3 million of them
                            // we want much fewer animals than that, so the probability must be low
                            int pMax = (int) (animal.getMaxProb() * 10000);
                            // The actual probability, based on the pixel terrain height and the optimum height for this species
                            int f = (int) (getAnimalProbability(animal.getHeightDifference(), pixel.getNoiseHeight(), animal.getMinHeight()) * animal.getMaxProb() * 10000);
                            // Apply the probability
                            if (rand.nextInt(pMax) < f && rand.nextInt(2000) < (animal.getMaxProb() / animal.getHeightDifference())) {
                                // If the probability condition passed,
                                // create a new instance of this type of animal on this pixel
                                Animal a = animal.getClass().newInstance();
                                a.setX(x);
                                a.setY(y);
                                animals.add(a);
                                // If an animal is found for this pixel, move to the next pixel
                                // don't look for any more animals
                                break;
                            }
                        }
                    }

                }
            }
        }

        // Add the animals to the world
        World.setAnimals(animals);
        Main.userInterface.printToUserTextBox(World.getAnimals().size() + " animals generated");
    }

    /**
     * Calculate the probability for an animal to spawn
     * @param heightDifference The difference between the animal's maximum and minimum spawn terrain height
     * @param pixelHeight The terrain height on the current pixel
     * @param animalMinimumHeight The minimum height the animal can spawn on
     * @return The probability for an animal to spawn on the current pixel
     */
    public static float getAnimalProbability(float heightDifference, float pixelHeight, float animalMinimumHeight) {
        // A function that maps the probability (for the animal to spawn)
        // to be the highest at the midway point between its maximum and minimum spawn height.
        // The probability decreases symmetrically both ways from that height
        return (float) ((-4 / (heightDifference * heightDifference)) *
                        Math.pow((pixelHeight - animalMinimumHeight - (heightDifference / 2)),2) + 1);
    }
}
