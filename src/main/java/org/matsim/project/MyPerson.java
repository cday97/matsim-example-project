package org.matsim.project;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.utils.geometry.CoordinateTransformation;

import java.util.Random;

public class MyPerson {
    private static final Logger log = Logger.getLogger(MyPerson.class);

    String gender;
    Integer age;
    Boolean worker;
    String dap;
    Coord homeLocation;
    Id<Person> id;

    Double I15NBx = -111.68361;
    Double I15NBy = 40.10346;
    Double paysonsdx = 0.0135612;
    Double paysonsdy = 0.0105619;
    Double paysonmeanx = -111.7362;
    Double paysonmeany = 40.03375;

    Scenario sc;
    PopulationFactory pf;
    CoordinateTransformation ct;

    public MyPerson(String gender, Integer age){
        this.gender = gender;
        this.age = age;
    }

    public MyPerson(Integer id, Random r, Scenario sc, PopulationFactory pf, CoordinateTransformation ct){
        this.id = Id.createPersonId(id);
        this.sc = sc;
        this.pf = pf;
        this.ct = ct;

        Boolean gendercoin = r.nextBoolean();
        if(gendercoin){
            this.gender = "female";
        }
        else {
            this.gender = "male";
        }

        this.age = makeAge(r);
        this.worker = setWorker(r, age);
        setDap(r);
        setHomeLocation(r);

        // add to MATSim population
        Person p = pf.createPerson(Id.createPersonId(id));
        sc.getPopulation().addPerson(p);
        p.getAttributes().putAttribute("age", age);
        p.getAttributes().putAttribute("gender", gender);
        p.getAttributes().putAttribute("isWorker", worker);
        p.getAttributes().putAttribute("typeOfPlan", dap);

        // create a plan for the person
        Plan plan = createPlans(r);
        p.addPlan(plan);
    }

    private Plan createPlans(Random r){
        Plan plan = pf.createPlan();
        Activity homeStart = pf.createActivityFromCoord("home", homeLocation);
        Integer homeStartTime = r.nextInt(2*3600) + 7*3600; // have people leave between 7 and 9 am
        plan.addActivity(homeStart);

        //if dap is home, then stay there all day
        if(dap.equals("H")){
            homeStart.setEndTime(homeStartTime);
        }
        // if dop is mandatory, then go to work and maybe one other place
        else if(dap.equals("M")){
            homeStart.setEndTime(homeStartTime);
            plan.addLeg(pf.createLeg("car"));

            Activity work = pf.createActivityFromCoord("work", getPaysonLocation("work", r));
            Integer workEndTime = r.nextInt(2*3600) + 16*3600; //generate random work time between 4:00pm and 6:00pm
            work.setEndTime(workEndTime); // make random
            plan.addActivity(work);
            plan.addLeg(pf.createLeg("car"));

            Integer numOfActs = r.nextInt(2);
            if (numOfActs == 0){
                // worker goes straight home
                Activity homeEnd = pf.createActivityFromCoord("home", homeLocation);
                plan.addActivity(homeEnd);
            } else {
                // worker goes to another before going home location
                Activity other = pf.createActivityFromCoord("other", getPaysonLocation("other",r));
                Integer otherEndTime = r.nextInt(4*3600) + workEndTime;
                other.setEndTime(otherEndTime);
                plan.addActivity(other);
                plan.addLeg(pf.createLeg("car"));

                Activity homeEnd = pf.createActivityFromCoord("home", homeLocation);
                plan.addActivity(homeEnd);
            }
        }

        // if dap is non mandatory then go to 1-3 places
        else if(dap.equals("NM")){
            Integer newHomeStart = r.nextInt(3*3600)+15*3600;
            homeStart.setEndTime(newHomeStart);
            plan.addLeg(pf.createLeg("car"));

            Activity other1 = pf.createActivityFromCoord("other", getPaysonLocation("other",r));
            Integer other1EndTime = r.nextInt(2*3600) + newHomeStart;
            other1.setEndTime(other1EndTime);
            plan.addActivity(other1);
            plan.addLeg(pf.createLeg("car"));

            Integer numOfActs = r.nextInt(3) + 1;
            if (numOfActs == 1) {
                Activity homeEnd = pf.createActivityFromCoord("home", homeLocation);
                plan.addActivity(homeEnd);
            } else{
                Integer timeLeftInDay =  24*3600 - other1EndTime;
                Activity other2 = pf.createActivityFromCoord("other", getPaysonLocation("other",r));
                Integer other2EndTime = r.nextInt(timeLeftInDay - 2*3600) + other1EndTime;
                other2.setEndTime(other2EndTime);
                plan.addActivity(other2);
                plan.addLeg(pf.createLeg("car"));
                if(numOfActs == 3){
                    Integer timeLeftInDay2 = 24*3600 - other2EndTime;
                    Activity other3 = pf.createActivityFromCoord("other", getPaysonLocation("other",r));
                    Integer other3EndTime = r.nextInt(timeLeftInDay2) + other2EndTime;
                    other3.setEndTime(other3EndTime);
                    plan.addActivity(other3);
                    plan.addLeg(pf.createLeg("car"));
                }
                Activity homeEnd = pf.createActivityFromCoord("home", homeLocation);
                plan.addActivity(homeEnd);
            }
        }

        return plan;
    }

    private Integer makeAge(Random r){
        Integer top = r.nextInt(64); // people are from 16 to 80 yrs old
        return top + 16;
    }

    private Boolean setWorker(Random r, Integer age) {
        // create code that determines whether someone is a worker or nonworker (false)
        // could be based on age, gender, + random numbers
        if (age > 65){
            return false;
        }
        else{
            return r.nextBoolean();
        }
    }

    private void setDap(Random r){
        this.dap = null;
        Double draw = r.nextDouble();

        // for workers
        if(worker) {
            if (draw < 0.08293) {
                this.dap = "H";
            } else if (draw < 0.08293 + 0.6216277) {
                this.dap = "M";
            } else {
                this.dap = "NM";
            }
        }
        // for non workers
        else {
          if (draw < 0.2294942) {
            this.dap = "H";
        } else if (draw < 0.2294942 + 0.1654482) {
            this.dap = "M";
        } else {
            this.dap = "NM";
        }
        }
    }

    private void setHomeLocation(Random r){
        this.homeLocation = getPaysonLocation("home", r);
    }

    private Coord getPaysonLocation(String activityType, Random r){
        Coord thisCoord = null;
        if (activityType.equals("work")){
            // get I-15 coordinate
            Coord thisLatLong = new Coord(I15NBx,I15NBy);
            thisCoord = ct.transform(thisLatLong);
        } else {
            // get random coordinate in payson
            Double randomX = r.nextGaussian() * paysonsdx + paysonmeanx;
            Double randomY = r.nextGaussian() * paysonsdy + paysonmeany;
            Coord thisLatLong = new Coord(randomX, randomY);
            thisCoord = ct.transform(thisLatLong);
        }

        return thisCoord;
    }

    public void printInfo(){
        log.info("Person: " + this.id);
        log.info("age: " + this.age);
        log.info("gender: " + this.gender);
    }

}
