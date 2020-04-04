package aib.environment;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TemperatureSettings provides all the necessary associations
 * to turn temperature values at each point into a suggestive colour temperature map
 */
public class TemperatureSettings {

    /**
     * A list of predefined temperature ranges with colours, used to create the temperature maps for the generated worlds
     * Based on legends from temperature maps from the real world
     */
    public static final List<Temperature> temperatures = new ArrayList<>(Arrays.asList(
            new Temperature(-60f, Color.rgb(200,200,200)),
            new Temperature(-15f, Color.rgb(150,10,200)),
            new Temperature(-8f, Color.rgb(90,50,150)),
            new Temperature(-3f, Color.rgb(35,50,130)),
            new Temperature(1f, Color.rgb(40,110,180)),
            new Temperature(3f, Color.rgb(35,170,145)),
            new Temperature(5f, Color.rgb(25,120,80)),
            new Temperature(7f, Color.rgb(25,160,50)),
            new Temperature(9f, Color.rgb(115,190,65)),
            new Temperature(11f, Color.rgb(240,185,15)),
            new Temperature(16f, Color.rgb(240,90,1)),
            new Temperature(20f, Color.rgb(255,0,0)),
            new Temperature(60f, Color.rgb(130,0,0))
        ));

    /**
     * Small inner class for temperature.
     * A temperature object hold a temperature value and an appropriate colour for that value
     * e.g. value: -5, colour: Blue
     */
    public static class Temperature {
        /** The temperature value, representing a temperature in Celsius degrees*/
        public float value;
        /** An appropriate colour that is suggestive for the temperature value */
        public Color colour;

        /**
         * Create a new temperature for a given value and colour
         * @param value The value for the temperature
         * @param colour The colour for the temperature
         */
        public Temperature(float value, Color colour) {
            this.value = value;
            this.colour = colour;
        }
    }
}
