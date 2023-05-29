package models.pendeltrain;

import jlx.blocks.ibd2.*;

public class PendelTrain_SR {
	public static class Block extends Type2IBD {
		public final Train_SR.Block train = new Train_SR.Block();
		public final Infrastructure_SR.Block infrastructure = new Infrastructure_SR.Block();
		
		@Override
		public void connectFlows() {
			train.D3_pos.connect(infrastructure.D1_trainPos);
			train.D4_signal1.connect(infrastructure.D2_signal1);
			train.D5_signal2.connect(infrastructure.D3_signal2);
		}
	}
}
