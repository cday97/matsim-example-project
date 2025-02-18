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
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup.SnapshotStyle;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.vehicles.VehicleType;

/**
 * @author nagel
 *
 */
public class RunMatsim2 {

	public static void main(String[] args) {

		Config config;
		if ( args==null || args.length==0 || args[0]==null ){
			config = ConfigUtils.loadConfig( "scenarios/equil/config.xml" );
		} else {
			config = ConfigUtils.loadConfig( args );
		}
		config.global().setInsistingOnDeprecatedConfigVersion(false);

		config.controler().setOverwriteFileSetting( OverwriteFileSetting.deleteDirectoryIfExists );
		config.controler().setLastIteration(2);

		config.qsim().setTrafficDynamics( TrafficDynamics.kinematicWaves );
		config.qsim().setSnapshotStyle( SnapshotStyle.kinematicWaves );

		final String PEDELEC = "pedelec";
		final String[] modes = {TransportMode.car, PEDELEC};

		config.strategy().addStrategySettings( new StrategyConfigGroup.StrategySettings().setWeight( 0.1 ).setStrategyName( DefaultPlanStrategiesModule.DefaultStrategy.ChangeSingleTripMode ) );
		config.changeMode().setModes( modes );

//      config.plansCalcRoute().addModeRoutingParams( new ModeRoutingParams( PEDELEC ).setTeleportedModeSpeed( 25./3.6 ) );
//      config.plansCalcRoute().addModeRoutingParams( new ModeRoutingParams( "walk" ).setTeleportedModeSpeed( 4./3.6 ) );
		config.plansCalcRoute().setNetworkModes( CollectionUtils.stringArrayToSet( modes ) );

		config.planCalcScore().addModeParams( new PlanCalcScoreConfigGroup.ModeParams( PEDELEC ) );

		// adding the PEDELEC mode param may (or may not; it unfortunately depends on the setup) remove all default entries.  If that happens, one needs to add the following:
		config.planCalcScore().addModeParams( new PlanCalcScoreConfigGroup.ModeParams( "car" ) );
		config.planCalcScore().setMarginalUtlOfWaitingPt_utils_hr( 0. );

		config.qsim().setMainModes( CollectionUtils.stringArrayToSet( modes ) );

		// ---

		Scenario scenario = ScenarioUtils.loadScenario(config) ;

		// possibly modify scenario here

		for( Link link : scenario.getNetwork().getLinks().values() ){
			link.setAllowedModes( CollectionUtils.stringArrayToSet( modes ) );
		}

		config.qsim().setVehiclesSource( QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData );
		{
			VehicleType vehicleType = scenario.getVehicles().getFactory().createVehicleType( Id.create( TransportMode.car, VehicleType.class ) );
			vehicleType.setMaximumVelocity( 200. / 3.6 );
			scenario.getVehicles().addVehicleType( vehicleType );
		}
		{
			VehicleType vehicleType = scenario.getVehicles().getFactory().createVehicleType( Id.create( PEDELEC, VehicleType.class ) );
			vehicleType.setMaximumVelocity( 10. / 3.6 );
			scenario.getVehicles().addVehicleType( vehicleType );
		}

		config.qsim().setLinkDynamics(QSimConfigGroup.LinkDynamics.PassingQ);

		// possibly modify scenario here
		
		// ---
		
		Controler controler = new Controler( scenario ) ;
		
		// possibly modify controler here

		//controler.addOverridingModule( new OTFVisLiveModule() ) ;

		
		// ---
		
		controler.run();
	}
	
}
