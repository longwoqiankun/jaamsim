/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2014 Ausenco Engineering Canada Inc.
 * Copyright (C) 2018 JaamSim Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaamsim.ProcessFlow;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.Samples.SampleInput;
import com.jaamsim.Statistics.SampleStatistics;
import com.jaamsim.Statistics.TimeBasedStatistics;
import com.jaamsim.input.Input;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.Output;
import com.jaamsim.input.UnitTypeInput;
import com.jaamsim.units.Unit;
import com.jaamsim.units.UserSpecifiedUnit;

/**
 * Collects basic statistical information on the entities that are received.
 * @author Harry King
 *
 */
public class Statistics extends LinkedComponent {

	@Keyword(description = "The unit type for the variable whose statistics will be collected.",
	         exampleList = {"DistanceUnit"})
	private final UnitTypeInput unitType;

	@Keyword(description = "The variable for which statistics will be collected.",
	         exampleList = {"'this.obj.attrib1'"})
	private final SampleInput sampleValue;

	private final SampleStatistics sampStats = new SampleStatistics();
	private final TimeBasedStatistics timeStats = new TimeBasedStatistics();

	{
		stateAssignment.setHidden(true);

		unitType = new UnitTypeInput("UnitType", KEY_INPUTS, UserSpecifiedUnit.class);
		unitType.setRequired(true);
		this.addInput(unitType);

		sampleValue = new SampleInput("SampleValue", KEY_INPUTS, null);
		sampleValue.setUnitType(UserSpecifiedUnit.class);
		sampleValue.setEntity(this);
		this.addInput(sampleValue);
	}

	public Statistics() {}

	@Override
	public void updateForInput(Input<?> in) {
		super.updateForInput(in);

		if (in == unitType) {
			Class<? extends Unit> ut = unitType.getUnitType();
			sampleValue.setUnitType(ut);
			return;
		}
	}

	@Override
	public void earlyInit() {
		super.earlyInit();
		sampStats.clear();
		timeStats.clear();
	}

	@Override
	public void addEntity(DisplayEntity ent) {
		super.addEntity(ent);
		double simTime = this.getSimTime();

		// Update the statistics
		double val = sampleValue.getValue().getNextSample(simTime);
		sampStats.addValue(val);
		timeStats.addValue(simTime, val);

		// Pass the entity to the next component
		this.sendToNextComponent(ent);
	}

	@Override
	public void clearStatistics() {
		super.clearStatistics();
		sampStats.clear();
		timeStats.clear();
	}

	@Override
	public Class<? extends Unit> getUserUnitType() {
		return unitType.getUnitType();
	}

	// ******************************************************************************************************
	// OUTPUT METHODS
	// ******************************************************************************************************

	@Output(name = "SampleMinimum",
	 description = "The smallest value that was recorded.",
	    unitType = UserSpecifiedUnit.class,
	  reportable = true,
	    sequence = 0)
	public double getSampleMinimum(double simTime) {
		return sampStats.getMin();
	}

	@Output(name = "SampleMaximum",
	 description = "The largest value that was recorded.",
	    unitType = UserSpecifiedUnit.class,
	  reportable = true,
	    sequence = 1)
	public double getSampleMaximum(double simTime) {
		return sampStats.getMax();
	}

	@Output(name = "SampleAverage",
	 description = "The average of the values that were recorded.",
	    unitType = UserSpecifiedUnit.class,
	  reportable = true,
	    sequence = 2)
	public double getSampleAverage(double simTime) {
		return sampStats.getMean();
	}

	@Output(name = "SampleStandardDeviation",
	 description = "The standard deviation of the values that were recorded.",
	    unitType = UserSpecifiedUnit.class,
	  reportable = true,
	    sequence = 3)
	public double getSampleStandardDeviation(double simTime) {
		return sampStats.getStandardDeviation();
	}

	@Output(name = "StandardDeviationOfTheMean",
	 description = "The estimated standard deviation of the sample mean.",
	    unitType = UserSpecifiedUnit.class,
	  reportable = true,
	    sequence = 4)
	public double getStandardDeviationOfTheMean(double simTime) {
		return sampStats.getStandardDeviation()/Math.sqrt(sampStats.getCount() - 1L);
	}

	@Output(name = "TimeAverage",
	 description = "The average of the values recorded, weighted by the duration of each value.",
	    unitType = UserSpecifiedUnit.class,
	  reportable = true,
	    sequence = 5)
	public double getTimeAverage(double simTime) {
		return timeStats.getMean(simTime);
	}

	@Output(name = "TimeStandardDeviation",
	 description = "The standard deviation of the values recorded, weighted by the duration of each value.",
	    unitType = UserSpecifiedUnit.class,
	  reportable = true,
	    sequence = 6)
	public double getTimeStandardDeviation(double simTime) {
		return timeStats.getStandardDeviation(simTime);
	}

}
