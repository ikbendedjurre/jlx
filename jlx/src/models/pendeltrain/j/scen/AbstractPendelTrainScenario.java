package models.pendeltrain.j.scen;

import jlx.scenario.*;
import models.pendeltrain.j.*;

public abstract class AbstractPendelTrainScenario extends Scenario {
	private final PendelTrain_SR.Block mainDiagram;
	
	public AbstractPendelTrainScenario(PendelTrain_SR.Block mainDiagram) {
		this.mainDiagram = mainDiagram;
	}
	
	public Train_SR.Block get_train() {
		return mainDiagram.train;
	}
	
	public Infrastructure_SR.Block get_infrastructure() {
		return mainDiagram.infrastructure;
	}
}
