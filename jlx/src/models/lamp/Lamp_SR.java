package models.lamp;

import jlx.blocks.ibd2.*;

public class Lamp_SR {
	public static class Block extends Type2IBD {
		public final Toggle_SR.Block toggle = new Toggle_SR.Block();
		public final Lightbulb_SR.Block lightbulb = new Lightbulb_SR.Block();
		
		@Override
		public void connectFlows() {
			toggle.D2_power.connect(lightbulb.D1_power);
		}
	}
}

