package models.pendeltrain.j.scen;

import jlx.asal.j.*;
import jlx.scenario.*;
import models.pendeltrain.j.*;

public class ScenarioBackAndForth extends AbstractPendelTrainScenario {
	public ScenarioBackAndForth(PendelTrain_SR.Block mainDiagram) {
		super(mainDiagram);
	}
	
	@Override
	public Step[] getSteps() {
		return new Step[] {
			expectStateChange(get_train(), "train[0][AT_S1]", "train[1][MOVING_1]"),
			expectStateChange(get_infrastructure(), "infrastructure[0][TRAIN_AT_S1]"),
			expectOutput(Output.from(get_train().D2_moving, JBool.FALSE), Output.from(get_train().D2_moving, JBool.TRUE)),
			stabilize(),
			setInput(get_train().T1_arrival, JPulse.TRUE),
			expectStateChange(get_train(), "train[1][MOVING_1]", "train[2][AT_S2]", "train[3][MOVING_2]"),
			expectStateChange(get_infrastructure(), "infrastructure[0][TRAIN_AT_S1]", "infrastructure[2][TRAIN_AT_S2]"),
			expectOutput(Output.from(get_train().D2_moving, JBool.TRUE), Output.from(get_train().D2_moving, JBool.FALSE), Output.from(get_train().D2_moving, JBool.TRUE)),
			expectOutput(Output.from(get_train().D3_pos, new TrainPos.S1()), Output.from(get_train().D3_pos, new TrainPos.S2())),
			expectOutput(Output.from(get_infrastructure().D3_signal2, new SignalColor.RED()), Output.from(get_infrastructure().D3_signal2, new SignalColor.GREEN())),
			expectOutput(Output.from(get_infrastructure().D2_signal1, new SignalColor.GREEN()), Output.from(get_infrastructure().D2_signal1, new SignalColor.RED())),
			stabilize(),
			setInput(get_train().T1_arrival, JPulse.TRUE),
			expectStateChange(get_train(), "train[3][MOVING_2]", "train[0][AT_S1]", "train[1][MOVING_1]"),
			expectStateChange(get_infrastructure(), "infrastructure[2][TRAIN_AT_S2]", "infrastructure[3][TRAIN_AT_S1]"),
			expectOutput(Output.from(get_train().D2_moving, JBool.TRUE), Output.from(get_train().D2_moving, JBool.FALSE), Output.from(get_train().D2_moving, JBool.TRUE)),
			expectOutput(Output.from(get_infrastructure().D2_signal1, new SignalColor.RED()), Output.from(get_infrastructure().D2_signal1, new SignalColor.GREEN())),
			expectOutput(Output.from(get_infrastructure().D3_signal2, new SignalColor.GREEN()), Output.from(get_infrastructure().D3_signal2, new SignalColor.RED())),
			expectOutput(Output.from(get_train().D3_pos, new TrainPos.S2()), Output.from(get_train().D3_pos, new TrainPos.S1())),
			stabilize()
		};
	}
}



