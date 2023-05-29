package jlx.behave.stable;

import jlx.behave.proto.DecaFourVertex;

public abstract class DecaStableTestTarget {
	private final DecaStableTransition minLevelSrc;
	
	private DecaStableTestTarget(DecaStableTransition minLevelSrc) {
		this.minLevelSrc = minLevelSrc;
	}
	
	public DecaStableTransition getMinLevelSrc() {
		return minLevelSrc;
	}
	
	public abstract boolean isMatch(DecaStableTransition src);
	
	public static class StateConfigTarget extends DecaStableTestTarget {
		private final DecaStableVertex v;
		
		public StateConfigTarget(DecaStableTransition minLevelSrc, DecaStableVertex v) {
			super(minLevelSrc);
			
			this.v = v;
		}
		
		@Override
		public boolean isMatch(DecaStableTransition src) {
			return src.getSrc() == v;
		}
	}
	
	public static class VertexTarget extends DecaStableTestTarget {
		private final DecaFourVertex vtx;
		
		public VertexTarget(DecaStableTransition minLevelSrc, DecaFourVertex vtx) {
			super(minLevelSrc);
			
			this.vtx = vtx;
		}
		
		@Override
		public boolean isMatch(DecaStableTransition src) {
			return src.getSrc().getCfg().getVtxs().values().contains(vtx);
		}
	}
	
	public static class VertexPairTarget extends DecaStableTestTarget {
		private final DecaFourVertex vtx1;
		private final DecaFourVertex vtx2;
		
		public VertexPairTarget(DecaStableTransition minLevelSrc, DecaFourVertex vtx1, DecaFourVertex vtx2) {
			super(minLevelSrc);
			
			this.vtx1 = vtx1;
			this.vtx2 = vtx2;
		}
		
		@Override
		public boolean isMatch(DecaStableTransition src) {
			return src.getSrc().getCfg().getVtxs().values().contains(vtx1) && src.getSrc().getCfg().getVtxs().values().contains(vtx2);
		}
	}
	
	public static class TransitionTarget extends DecaStableTestTarget {
		private final DecaStableTransition t;
		
		public TransitionTarget(DecaStableTransition minLevelSrc, DecaStableTransition t) {
			super(minLevelSrc);
			
			this.t = t;
		}
		
		public DecaStableTransition getTransition() {
			return t;
		}
		
		@Override
		public boolean isMatch(DecaStableTransition src) {
			return src == t;
		}
	}
}

