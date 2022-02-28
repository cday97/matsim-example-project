package org.matsim.project;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.utils.geometry.CoordinateTransformation;

import java.util.Random;

public class MyPerson {
    private static final Logger log = Logger.getLogger(MyPerson.class);

    String gender;
    Integer age;
    Id<Person> id;

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

        // add to MATSim population
        Person p = pf.createPerson(Id.createPersonId(id));
        sc.getPopulation().addPerson(p);
        p.getAttributes().putAttribute("age", age);
        p.getAttributes().putAttribute("gender", gender);

    }

    private Integer makeAge(Random r){
        Integer top = r.nextInt(60);
        return top + 20;
    }

    public void printInfo(){
        log.info("Person: " + this.id);
        log.info("age: " + this.age);
        log.info("gender: " + this.gender);
    }

}
