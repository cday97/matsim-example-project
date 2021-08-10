/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.project;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup.SnapshotStyle;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;

import java.util.Set;

/**
 * @author nagel
 *
 */
public class RunMatsimBike {

	public static void main(String[] args) {

		Config config;
		if ( args==null || args.length==0 || args[0]==null ){
			config = ConfigUtils.loadConfig( "scenarios/equil1/config.xml" );
		} else {
			config = ConfigUtils.loadConfig( args );
		}
		config.controler().setOverwriteFileSetting( OverwriteFileSetting.deleteDirectoryIfExists );

		config.plansCalcRoute().setAccessEgressType(PlansCalcRouteConfigGroup.AccessEgressType.accessEgressModeToLink);

		config.qsim().setTrafficDynamics( TrafficDynamics.kinematicWaves );
		config.qsim().setSnapshotStyle( SnapshotStyle.kinematicWaves );

		// possibly modify config here
		
		// ---
		
		Scenario scenario = ScenarioUtils.loadScenario(config) ;
		
		// possibly modify scenario here

		var bikeLinks = Set.of(
				Id.createLinkId(3),
				Id.createLinkId(4),
				Id.createLinkId(5),
				Id.createLinkId(6),
				Id.createLinkId(7),
				Id.createLinkId(8),
				Id.createLinkId(9)
		);

		for (Link link: scenario.getNetwork().getLinks().values()) {
			if (bikeLinks.contains(link.getId())) {
				link.setAllowedModes(Set.of(TransportMode.bike));
			} else {
				link.setAllowedModes(Set.of(TransportMode.car, TransportMode.bike));
			}

			link.setLength(CoordUtils.calcEuclideanDistance(link.getFromNode().getCoord(),link.getToNode().getCoord()));
		}

		scenario.getPopulation().getPersons().stream()
				.map(person -> person.getSelectedPlan())
				.flatMap(plan -> TripStructureUtils.getLegs(plan).stream())
				.forEach(legs -> leg.setRoute(null));

		//for (Link link: scenario.getNetwork().getLinks().values()) {
		//	link.setAllowedModes(Set.of(TransportMode.car, TransportMode.bike));
		//}

		// ---
		
		Controler controler = new Controler( scenario ) ;
		
		// possibly modify controler here

		//controler.addOverridingModule( new OTFVisLiveModule() ) ;

		
		// ---
		
		controler.run();
	}
	
}
