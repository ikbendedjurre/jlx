package jlx.behave.proto;

import java.util.*;

import jlx.asal.j.JScope;
import jlx.common.FileLocation;
import jlx.utils.Texts;

public class DecaFourTransition {
	private DecaFourVertex sourceVertex;
	//private Set<DecaFourVertex> targetVertices;
	private DecaFourTgtGrp tgtGrp;
	private Map<DecaFourVertex, Set<DecaFourTransition>> stabTrsPerSecondTgt;
	//private Set<Map<JScope, DecaFourVertex>> enablingVtxCfgs;
	private DecaFourTransitionReqSet reqSet;
	private DecaTwoBTransition legacy;
	
	public boolean isReachable;
	
	public DecaFourTransition(DecaTwoBTransition legacy, DecaFourTransitionReqSet reqSet, DecaFourVertex sourceVertex, DecaFourTgtGrp tgtGrp) {
		this.sourceVertex = sourceVertex;
		
		for (Map<JScope, DecaFourVertex> c : reqSet.getReqCfgs()) {
			for (Map.Entry<JScope, DecaFourVertex> e : c.entrySet()) {
				if (e.getValue() == null) {
					throw new Error("Should not happen!");
				}
			}
		}
		
		if (reqSet.isEmpty()) {
			throw new Error("Should not happen!");
		}
		
		stabTrsPerSecondTgt = new HashMap<DecaFourVertex, Set<DecaFourTransition>>();
		
		this.legacy = legacy;
		this.reqSet = reqSet;
		this.tgtGrp = tgtGrp;
	}
	
	public DecaFourTransition(DecaThreeTransition source, DecaFourTransitionReqSet reqSet, DecaFourVertex sourceVertex, DecaFourTgtGrp tgtGrp) {
		this.sourceVertex = sourceVertex;
//		this.targetVertices = targetVertices;
//		this.enablingVtxCfgs = enablingVtxCfgs;
		
		for (Map<JScope, DecaFourVertex> c : reqSet.getReqCfgs()) {
			for (Map.Entry<JScope, DecaFourVertex> e : c.entrySet()) {
				if (e.getValue() == null) {
					throw new Error("Should not happen!");
				}
			}
		}
		
		if (reqSet.isEmpty()) {
			throw new Error("Should not happen!");
		}
		
		stabTrsPerSecondTgt = new HashMap<DecaFourVertex, Set<DecaFourTransition>>();
		
		legacy = null;
		
		this.reqSet = reqSet;
		this.tgtGrp = tgtGrp;
		
	}
	
	public JScope getScope() {
		return sourceVertex.getScope();
	}
	
	public DecaTwoBTransition getLegacy() {
		return legacy;
	}
	
	public List<FileLocation> getFileLocations() {
		return legacy.getFileLocations();
	}
	
	public DecaFourVertex getSourceVertex() {
		return sourceVertex;
	}
	
	public DecaFourTgtGrp getTgtGrp() {
		return tgtGrp;
	}
	
	public Map<DecaFourVertex, Set<DecaFourTransition>> getStabTrsPerSecondTgt() {
		return stabTrsPerSecondTgt;
	}
	
	public Set<PulsePackMap> getInputs() {
		return legacy.getInputVals();
	}
	
	public DecaFourTransitionReqSet getReqs() {
		return reqSet;
	}
	
	public Set<ProtoTransition> getProtoTrs() {
		return legacy.getProtoTrs();
	}
	
	public String getDotStr() {
		return "C.O."; //Texts._break(legacy.getInputs().getCombinedGuard().toString(), "<BR/>", 70);
	}
	
	public String getHtmlStr(Collection<DecaFourVertex> activeVtxs, int mode, int maxReqCount) {
		List<String> xs = new ArrayList<String>();
		
		for (Map<JScope, DecaFourVertex> req : reqSet.getReqCfgs()) {
			List<String> vs = new ArrayList<String>();
			boolean hasActive = false;
			boolean hasInactive = false;
			
			for (DecaFourVertex v : req.values()) {
				if (activeVtxs.contains(v)) {
					vs.add("<B>" + v.getName() + "</B>");
					hasActive = true;
				} else {
					vs.add(v.getName());
					hasInactive = true;
				}
			}
			
			if (req.isEmpty() || mode == 0 || (mode == 1 && hasActive) || (mode == 2 && !hasInactive)) {
				xs.add("{ " + Texts.concat(vs, " x ") + " }");
				
				if (xs.size() > maxReqCount) {
					xs.add("etc");
					break;
				}
			}
		}
		
		return Texts.concat(xs, "<BR/>") + "<BR/>" + getDotStr();
	}
}

