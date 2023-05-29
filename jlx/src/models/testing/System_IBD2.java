package models.testing;

import jlx.blocks.ibd2.*;

public class System_IBD2 {
	public static class Block extends Type2IBD {
		public final Main.Block main = new Main.Block();
		public final Worker.Block worker1 = new Worker.Block();
		public final Worker.Block worker2 = new Worker.Block();
		
		@Override
		public void connectFlows() {
			main.start.connect(worker1.start);
			main.start.connect(worker2.start);
			
			worker1.notify.connect(main.finished1);
			worker1.result.connect(main.value1);
			worker2.notify.connect(main.finished2);
			worker2.result.connect(main.value2);
		}
	}
}
