# Educational Tool for Climate Change Visualisation
### 2019/2020

#### Using the application
To test the application, simply open a terminal in the climate-change-visualisation folder 
and run the command `java -jar climate-change-vis-app.jar`

#### Running the project in IntelliJ IDEA
To access the code and run it yourself in IntelliJ IDEA, click `Create A New Project` 
on the home screen, or go to `File > New > Project`. From the menu on the left side, 
select **JavaFX** and click Next. Click on `Project Location` and from the File Explorer
select the `climate-change-visualisation` Folder and press Finish. Remove the `Sample` Folder that IntelliJ
created. Mark `src` as the Source Root Folder, `resources` as the Resource Root Folder and
`test` as the Test Root Folder. Add JUnit 5 as a Project Library.
In `File > Project Structure > Project` ensure the `Project SDK` is set to 1.8 and `Project 
Language Level` is set to 8. Finally, go to `aib743 > src > aib > Main` and run the `main()` 
method.

#### Libraries
##### FastNoise by Jordan Peck
Library used to generate noise values from my samples. I replaced javax.vecmath.Vector2f 
and javax.vecmath.Vector3f with com.sun.javafx.geom.Vec2f and com.sun.javafx.geom.Vec3f
because javax is no longer part of java and I already use JavaFX.
##### ZoomableScrollPane by
Replacement for the JavaFX ScrollPane, for the added functionality of zooming.
Used as a container for the map, so it can be zoomed in on.
