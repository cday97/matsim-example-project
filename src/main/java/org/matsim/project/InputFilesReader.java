package org.matsim.project;

import org.activitysim.utils.ActivitySimFacilitiesReader;
import org.activitysim.utils.ActivitySimPersonsReader;
import org.activitysim.utils.ActivitySimTripsReader;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.File;

public class InputFilesReader {
    private static final Logger log = Logger.getLogger(InputFilesReader.class);
    private Scenario scenario;

    public InputFilesReader(Scenario sc){
        this.scenario = sc;
    }

    /**
     * Read the ActivitySim output files into the scenario
     * @param personsFile Path to activitysim output persons file
     * @param tripsFile Path to activitysim output trips file
     */
    public void readActivitySimFiles(File personsFile, File tripsFile, File facilitiesFile, File householdsFile, File householdCoordFile){
        ActivitySimFacilitiesReader facilitiesReader = new ActivitySimFacilitiesReader(scenario, facilitiesFile, householdsFile, householdCoordFile);
        facilitiesReader.readFacilities();
        facilitiesReader.readHouseholds();
        ActivitySimPersonsReader personsReader = new ActivitySimPersonsReader(scenario, personsFile);
        personsReader.readPersons();
        ActivitySimTripsReader tripsReader = new ActivitySimTripsReader(scenario, tripsFile, facilitiesReader.getTazFacilityMap());
        tripsReader.readTrips();
        personsReader.readPlans();

    }


    public static void main(String[] args){
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        scenario.getConfig().global().setCoordinateSystem("EPSG:26912");
        InputFilesReader reader = new InputFilesReader(scenario);
        String scenarioPath = "scenarios/slc-1k/";
        String outputFileName = "slc_plans_1k.xml.gz";

        File personsFile = new File(scenarioPath + "persons.csv");
        File tripsFile = new File(scenarioPath + "trips.csv");
        File householdsFile = new File(scenarioPath + "households.csv");
        File facilitiesFile = new File(scenarioPath + "facility_ids.csv");
        File householdCoordFile = new File(scenarioPath + "hhcoord.csv");
        reader.readActivitySimFiles(personsFile, tripsFile, facilitiesFile, householdsFile, householdCoordFile);

        new PopulationWriter(scenario.getPopulation()).write(scenarioPath + outputFileName);
    }

}
