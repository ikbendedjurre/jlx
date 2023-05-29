package models.point.machines;

import jlx.blocks.ibd2.*;
import models.point.v2x7x0A.F_P3_SR;

public class SCI_PM_Gen_SR {
	public static class Block extends Type2IBD {
		public final F_P3_SR.Block p3 = new F_P3_SR.Block();
		public final F_PM_Gen.Block pm1 = new F_PM_Gen.Block();
		public final F_PM_Gen.Block pm2 = new F_PM_Gen.Block();
		
		@Override
		public void connectFlows() {
			//PM1:
			
			p3.D21_PM1_Position.connect(pm1.D1_Position_Out);
			
			//PM2:
			
			p3.D22_PM2_Position.connect(pm2.D1_Position_Out);
		}
	}
}
