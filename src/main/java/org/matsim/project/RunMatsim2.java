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

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup.SnapshotStyle;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;
import org.matsim.core.scenario.ScenarioUtils;

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
		config.controler().setOverwriteFileSetting( OverwriteFileSetting.deleteDirectoryIfExists );

		config.qsim().setTrafficDynamics( TrafficDynamics.kinematicWaves );
		config.qsim().setSnapshotStyle( SnapshotStyle.kinematicWaves );

		// possibly modify config here
		config.controler().setLastIteration(50);

		config.strategy().addStrategySettings( new StrategyConfigGroup.StrategySettings().setStrategyName( DefaultPlanStrategiesModule.DefaultStrategy.ChangeSingleTripMode ).setWeight( 0.1 ) );
		config.changeMode().setModes( new String [] {TransportMode.car, "pedelec"} );
		config.plansCalcRoute().addModeRoutingParams( new PlansCalcRouteConfigGroup.ModeRoutingParams( "pedelec" ).setTeleportedModeSpeed( 10./3.6 ) );
		config.plansCalcRoute().addModeRoutingParams( new PlansCalcRouteConfigGroup.ModeRoutingParams( "walk" ).setTeleportedModeSpeed( 4./3.6 ) );

		{
			final PlanCalcScoreConfigGroup.ModeParams modeParams = new PlanCalcScoreConfigGroup.ModeParams( "pedelec" );
			modeParams.setConstant( 100. );
			config.planCalcScore().addModeParams( modeParams );
		}

		{
			PlanCalcScoreConfigGroup.ActivityParams abc = new PlanCalcScoreConfigGroup.ActivityParams("tennisPlaying");
			abc.setTypicalDuration(2);
			abc.setOpeningTime(3600 * 17);
			abc.setClosingTime(3600 * 22);
			config.planCalcScore().addActivityParams(abc);
		}

		// ---
		
		Scenario scenario = ScenarioUtils.loadScenario(config) ;
		
		// possibly modify scenario here
		
		// ---
		
		Controler controler = new Controler( scenario ) ;
		
		// possibly modify controler here

		//controler.addOverridingModule( new OTFVisLiveModule() ) ;

		
		// ---
		
		controler.run();
	}
	
}
