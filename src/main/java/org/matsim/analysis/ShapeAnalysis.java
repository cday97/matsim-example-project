package org.matsim.analysis;

import com.graphhopper.jsprit.core.problem.job.Activity;
import org.locationtech.jts.geom.Geometry;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;

public class ShapeAnalysis {

    private static final String shapefile = "D:\\Users\\chris\\Documents\\CSDSchool\\MatSimClass\\classwork\\Bezirke_-_Berlin-shp\\Berlin_Bezirke.shp";
    private static final String populationPath = "D:\\Users\\chris\\Documents\\CSDSchool\\MatSimClass\\classwork\\5.5.x-1pct\\5.5.x-1pct\\berlin-v5.5-1pct.output_plans.xml";
    private static final String networkPath = "D:\\Users\\chris\\Documents\\CSDSchool\\MatSimClass\\classwork\\5.5.x-1pct\\5.5.x-1pct\\berlin-v5.5-1pct.output_network.xml";
    private static final CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation("EPSG:31468", "EPSG:3857");

    private static final String gemeinde_s = "001";
    private static final String gemeinde_s2 = "002";

    public static void main(String[] args) {

       var features = ShapeFileReader.getAllFeatures(shapefile);
       var network = NetworkUtils.readNetwork(networkPath);
       var population = PopulationUtils.readPopulation(populationPath);

       var geometry = features.stream()
               .filter(feature -> feature.getAttribute("Gemeinde_s").equals(gemeinde_s))
               .map(feature -> (Geometry)feature.getDefaultGeometry())
               .findAny()
               .orElseThrow();
        var geometry2 = features.stream()
                .filter(feature -> feature.getAttribute("Gemeinde_s").equals(gemeinde_s2))
                .map(feature -> (Geometry)feature.getDefaultGeometry())
                .findAny()
                .orElseThrow();

       int counter  = 0;
       int bwCounter = 0;

       for(var persons : population.getPersons().values()) {

           var plan = persons.getSelectedPlan();
           var activities = TripStructureUtils.getActivities(plan, TripStructureUtils.StageActivityHandling.ExcludeStageActivities);
           var trips = TripStructureUtils.getTrips(plan);
                //another way to do the activities thing
                //for(var element : plan.getPlanElements()) {
                //    if (element instanceof Activity) {var activity = (Activity)element;}
                //    }

           for (var activity : activities) {

               var coord = activity.getCoord();
               var transformedCoord = transformation.transform(coord);

               if (geometry.covers(MGC.coord2Point(transformedCoord))) {
                   counter++;
               }
           }


           for (var trip : trips) {

               var startCoord = trip.getOriginActivity().getCoord();
               var transStartCoord = transformation.transform(startCoord);

               var endCoord = trip.getDestinationActivity().getCoord();
               var transEndCoord = transformation.transform(endCoord);

               if (geometry.covers(MGC.coord2Point(transStartCoord)) & geometry2.covers(MGC.coord2Point(transEndCoord))){ bwCounter++;}
               if (geometry.covers(MGC.coord2Point(transEndCoord)) & geometry2.covers(MGC.coord2Point(transStartCoord))){ bwCounter++;}
           }
       }

        System.out.println(counter + " activities in Mitte");
        System.out.println(bwCounter + " trips between 001 and 002");

    }
}
