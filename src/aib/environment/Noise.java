package aib.environment;

import aib.Main;
import aib.libraries.FastNoise;

import java.util.Random;

/**
 * The noise class handles the necessary generation of Perlin noise, i.e maps or lines
 */
public class Noise {

    /**
     * Generate a map (2D array) of Perlin gradual noise
     * @param mapWidth Desired map width
     * @param mapHeight Desired map height
     * @param seed Map seed
     * @param scale Scale for the noise
     * @param octaves Number of octaves for the noise (higher -> more smaller detail)
     * @param persistence Persistence for the noise
     * @param lacunarity Lacunarity for the noise
     * @param offsetX Horizontal noise offset (see different regions on the noise -
     *               the real full size of the map is not the width and the height, but how far the offset can go.
     *               In theory it could be infinite, if we were not limited by number representations)
     * @param offsetY Vertical noise offset (as above)
     * @return The generated noise map
     */
    public static float[][] generateNoiseMap(int mapWidth, int mapHeight, int seed, float scale, int octaves,
                                             float persistence, float lacunarity, float offsetX, float offsetY) {

        // The map
        float[][] noiseMap = new float[mapWidth][mapHeight];

        // Use library to generate the noise sample
        FastNoise myNoise = new FastNoise();
        myNoise.SetNoiseType(Main.userInterface.noiseTypeComboBox.getValue());

        // Octaves offsets
        Random rand = new Random(seed);
        float[] octavesOffsetsX = new float[octaves];
        float[] octavesOffsetsY = new float[octaves];
        for (int i = 0; i < octaves; i++) {
            octavesOffsetsX[i] = (rand.nextInt(100000 + 100000) - 100000) + offsetX;
            octavesOffsetsY[i] = (rand.nextInt(100000 + 100000) - 100000) + offsetY;
        }

        // Ensure scale is > 0 to avoid division by 0
        if (scale <= 0) {
            scale = 0.0001f;
        }

        // Track max and min noise height to normalise the map later
        float maxHeight = -1f;
        float minHeight = 1f;

        // Zooming (scale) to the center instead of top right
        float halfWidth = mapWidth / 2f;
        float halfHeight = mapHeight / 2f;

        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                float amplitude = 1;
                float frequency = 1;
                float noiseHeight = 0;


                // Iterate through octaves
                for (int i = 0; i < octaves; i++) {
                    // set the sample for perlin noise
                    // the higher the frequency, the further apart the sample points will be
                    // => the height values will change more rapidly
                    float sampleX = (x - halfWidth) / scale * frequency + octavesOffsetsX[i];
                    float sampleY = (y - halfHeight) / scale * frequency + octavesOffsetsY[i];

                    // get the perlin noise from the sample
                    // for more interesting noise, have perlinValue in range -1 to 1, so noiseHeight can also decrease
                    float perlinValue = myNoise.GetNoise(sampleX, sampleY);

                    // Increase noiseHeight by perlin value of each octave
                    noiseHeight += perlinValue * amplitude;

                    // at the end of each octave, decrease the amplitude (because persistence is in range 0-1)
                    amplitude *= persistence;

                    // frequency increases at each octave since lacunarity should be greater than 1
                    // each octave adds more finer detail
                    frequency *= lacunarity;
                }

                // Obtain the range of noise height of our map (for normalisation)
                if (noiseHeight > maxHeight) maxHeight = noiseHeight;
                else if (noiseHeight < minHeight) minHeight = noiseHeight;

                noiseMap[x][y] = noiseHeight;
            }
        }

        // before returning the noiseMap, normalise it so that all of its values are in the range 0 to 1
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                // InverseLerp return a value in the range 0-1
                // reflective of where noiseMap[x, y] lies between minNoiseHeight and maxNoiseHeight
                noiseMap[x][y] = inverseLerp(minHeight, maxHeight, noiseMap[x][y]);
                if ( noiseMap[x][y] < World.lowestPoint) World.lowestPoint = noiseMap[x][y];
                else if (noiseMap[x][y] > World.highestPoint) World.highestPoint = noiseMap[x][y];
            }
        }

        return noiseMap;
    }

    /**
     * Generate a line (1D array) of Perlin gradual noise
     * @param lineWidth Desired line width
     * @param seed Line seed
     * @param scale Scale for the noise
     * @param octaves Number of octaves for the noise (higher -> more smaller detail)
     * @param persistence Persistence for the noise
     * @param lacunarity Lacunarity for the noise
     * @param offset Noise offset (see different regions on the noise -
     *               the real full size of the line si not the width, but how far the offset can go.
     *               In theory it could be infinite, if we were not limited by number representations)
     * @return The generated noise map
     */
    public static float[] generateNoiseLine(int lineWidth, int seed, float scale, int octaves,
                                             float persistence, float lacunarity, float offset) {

        float[] noiseLine = new float[lineWidth];

        // perlin noise generator
        FastNoise myNoise = new FastNoise();
        myNoise.SetNoiseType(FastNoise.NoiseType.Perlin);

        // octaves offsets
        Random rand = new Random(seed);
        float[] octavesOffsets = new float[octaves];
        for (int i = 0; i < octaves; i++) {
            octavesOffsets[i] = (rand.nextInt(100000 + 100000) - 100000) + offset;
        }

        // Ensure scale is > 0 to avoid division by 0
        if (scale <= 0) {
            scale = 0.0001f;
        }

        // Track max and min noise height to normalise the map later
        float maxHeight = -1f;
        float minHeight = 1f;

        // zooming to the center instead of top right
        float halfWidth = lineWidth / 2f;

        for (int x = 0; x < lineWidth; x++) {
            float amplitude = 1;
            float frequency = 1;
            float noiseHeight = 0;

            // iterate through octaves
            for (int i = 0; i < octaves; i++) {
                // set the sample for perlin noise
                // the higher the frequency, the further apart the sample points will be
                // => the height values will change more rapidly
                float sampleX = (x - halfWidth) / scale * frequency + octavesOffsets[i];

                // get the perlin noise from the sample
                // for more interesting noise, have perlinValue in range -1 to 1, so noiseHeight can also decrease
                float perlinValue = myNoise.GetNoise(sampleX,0);

                // Increase noiseHeight by perlin value of each octave
                noiseHeight += perlinValue * amplitude;

                // at the end of each octave, decrease the amplitude (because persistence is in range 0-1)
                amplitude *= persistence;

                // frequency increases at each octave since lacunarity should be greater than 1
                // each octave adds more finer detail
                frequency *= lacunarity;
            }

            // Obtain the range of noise height of our map (for normalisation)
                if (noiseHeight > maxHeight) maxHeight = noiseHeight;
                else if (noiseHeight < minHeight) minHeight = noiseHeight;

            noiseLine[x] = noiseHeight;
        }

        // before returning the noiseMap, normalise it so that all of its values are in the range 0 to 1
        for (int x = 0; x < lineWidth; x++) {
            // InverseLerp return a value in the range 0-1
            // reflective of where noiseMap[x, y] lies between minNoiseHeight and maxNoiseHeight
            noiseLine[x] = inverseLerp(minHeight, maxHeight, noiseLine[x]);
        }

        return noiseLine;
    }

    /**
     * Normalise a value so it fits the range 0 to 1
     * @param min Minimum value of current range
     * @param max Maximum value of current range
     * @param x The value to normalise
     * @return The normalised value
     */
    public static float inverseLerp(float min, float max, float x) {
         return (x - min) / (max - min);
    }
}
