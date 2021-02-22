package frc.lib.PathPlanner;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jaci.pathfinder.Waypoint;

public class PathWeaverToWaypoints {

    LinkedList<Waypoint> outputWaypoints = new LinkedList<Waypoint>();

    final String resourceBaseLocal = "./src/main/deploy/pathData";
    final String resourceBaseRIO = "/home/lvuser/deploy/pathData";
    String resourceBase = resourceBaseRIO; // default to roboRIO

    double initialRotRad  = 0;
    double initialTransX  = 0;
    double initialTransY  = 0;

    /**
     * Converts a pathweaver output .json file to Casserole 2020 waypoints for
     * consumption by Jaci's pathplanner. Discards the time and velocity information
     * (since pathplanner does that anyway) Performs user-specified decimatino on
     * the path (since pathplanner does its own interpolation)
     */

    public PathWeaverToWaypoints(String sourceFile, int decimationFactor) {

        // Check if the path for resources expected on the roboRIO exists.
        if (Files.exists(Paths.get(resourceBaseRIO))) {
            // If RIO path takes priority (aka we're running on a roborio) this path takes
            // priority
            resourceBase = resourceBaseRIO;
        } else {
            // Otherwise use a local path, like we're running on a local machine.
            resourceBase = resourceBaseLocal;
        }

        parse(Path.of(resourceBase, sourceFile), decimationFactor);

    }

    public void parse(Path fpath, int decFactor) {

        System.out.println("Starting path import from " + fpath.toString());
        int pointCounter = 0;
        JSONParser parser = new JSONParser();
        Reader reader;
        try {
            reader = new FileReader(fpath.toString());

            JSONArray pointsArr = (JSONArray) parser.parse(reader);

            Iterator<JSONObject> it = pointsArr.iterator();
            while (it.hasNext()) {
                JSONObject thisStep = it.next();
                double curvature = Math.abs((Double) thisStep.get("curvature"));

                //Adaptive sampling - on segments with high curvature,
                // decrease the decimation factor to get more points
                double adaptiveDecFactor = decFactor;
                if(curvature > 0.2){
                    adaptiveDecFactor = Math.round(decFactor/3);
                } else if(curvature > 0.05){
                    adaptiveDecFactor = Math.round(decFactor/1.5);
                }
                adaptiveDecFactor = Math.max(1, adaptiveDecFactor);

                if(pointCounter % adaptiveDecFactor == 0){
                    JSONObject pose = (JSONObject) thisStep.get("pose");
                    //System.out.println("point = " + pose.toString());   
                    
                    JSONObject rotation = (JSONObject) pose.get("rotation");
                    JSONObject translation = (JSONObject) pose.get("translation");

                    //Transform to our robot's assumed reference frame
                    double inputRotRad = -1.0* ((Double) rotation.get("radians"));
                    double inputTransX = ((Double) translation.get("x"));
                    double inputTransY = -1.0* ((Double) translation.get("y"));

                    if(pointCounter == 0){
                        initialRotRad  = inputRotRad;
                        initialTransX  = inputTransX;
                        initialTransY  = inputTransY;
                    }

                    //Transform to be relative to robot strating position
                    double outputRotDeg = inputRotRad - initialRotRad;
                    double outputTransX = inputTransX - initialTransX;
                    double outputTransY = inputTransY - initialTransY;

                    //System.out.println(curvature);

                    Waypoint newPoint = new Waypoint(outputTransX, outputTransY, outputRotDeg);

                    outputWaypoints.add(newPoint);
                    
                } //otherwise skip the point
                pointCounter++;
            }

            reader.close();

        } catch (IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("Import complete!");        

    }

    public Waypoint[] getWaypoints(){
        Waypoint retVal[] = new Waypoint[outputWaypoints.size()];
        outputWaypoints.toArray(retVal);
        return retVal;
    }



    /**
     * Unit test
     * @param args
     */
    public static void main(String[] args) {
        PathWeaverToWaypoints objUnderTest = new PathWeaverToWaypoints("barrel_run_main.wpilib.json", 40);
        System.out.println(objUnderTest.getWaypoints());
    }


}
