package org.matsim.project;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.util.Random;

public class PlansMaker {
    public static final Logger log = Logger.getLogger(PlansMaker.class);

    private final Scenario scenario;
    private final PopulationFactory pf;
    private final CoordinateTransformation ct;
    public Random r;

    public PlansMaker(String crs){

        log.info("Started Plans Maker");
        scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        pf = scenario.getPopulation().getFactory();

        log.info("using crs " + crs);
        ct = TransformationFactory.getCoordinateTransformation(
                TransformationFactory.WGS84,
                crs
        );

        scenario.getConfig().global().setCoordinateSystem(crs);

        this.r = new Random(42);

    }

    public void makePlans(Integer numberOfPeople){

        for(int i = 0; i < numberOfPeople; i++){
            MyPerson p = new MyPerson(i, r, scenario, pf, ct);
            //p.printInfo();
        }
    }

    public String getCrs(){
        return this.scenario.getConfig().global().getCoordinateSystem();
    }

    public void writePlans(String file){
        PopulationWriter writer = new PopulationWriter(scenario.getPopulation());
        writer.write(file);
    }

    public static void main(String[] args) {

        String crs = args[0];
        String outFile = args[1];

        PlansMaker pm = new PlansMaker(crs);
        //pm.makePlans( 19892); // current scenario
        pm.makePlans(47446); // future scenario
        pm.writePlans(outFile);
    }
}
