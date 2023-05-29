package models.point.v2x7x0A.scen_1pm;

import jlx.scenario.*;
import models.point.v2x7x0A.SCI_P_PDI_SR;
import models.point.v2x7x0A.SubS_P_SR;

public abstract class SubS_P_SD extends Scenario {
	public final SCI_P_PDI_SR.Block p;
	public final SubS_P_SR.Block fe;
	
	public SubS_P_SD(SCI_P_PDI_SR.Block p, SubS_P_SR.Block fe) {
		this.p = p;
		this.fe = fe;
	}
}
