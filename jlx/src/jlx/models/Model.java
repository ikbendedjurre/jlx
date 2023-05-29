package jlx.models;

import java.util.*;

import jlx.asal.j.JTypeLibrary;
import jlx.blocks.ibd1.Type1IBD;
import jlx.blocks.ibd2.InterfacePort;
import jlx.common.reflection.ClassReflectionException;
import jlx.models.IBDInstances.*;

public class Model extends ComposableModel {
	public JTypeLibrary createTypeLibrary() throws ClassReflectionException {
		JTypeLibrary result = new JTypeLibrary();
		
		for (IBD1Instance i1 : getIBD1Instances()) {
			for (Type1IBD i2 : i1.getLegacyPts()) {
				result = new JTypeLibrary(result, i2.getTypeLib());
			}
		}
		
		return result;
	}
	
	public Set<IBD1Port> getConjugateIBD1Ports(IBD1Port port) {
		Set<IBD1Port> result = new HashSet<IBD1Port>();
		
//		System.out.println("-> getIBD1Flows().size() = " + getIBD1Flows().size());
		
		for (IBD1Flow f : getIBD1Flows()) {
			if (f.v1 == port) {
//				System.out.println("-> found " + port.name);
				confirmConjugation(f.v2, port, null);
				result.add(f.v2);
			} else {
				if (f.v2 == port) {
//					System.out.println("-> found " + port.name);
					confirmConjugation(f.v1, port, null);
					result.add(f.v1);
				}
			}
		}
		
//		System.out.println("-> getIBD1ToIBD2Flows().size() = " + getIBD1ToIBD2Flows().size());
		
		for (IBD1ToIBD2Flow f1 : getIBD1ToIBD2Flows()) {
			if (f1.v1 == port) {
//				System.out.println("-> found " + port.name);
				List<IBD2Flow> seq = getIBD2FlowSeq(f1.v2);
				
				if (seq.size() > 0) {
					IBD2Port otherEnd = seq.get(seq.size() - 1).v2;
					
					for (IBD1ToIBD2Flow f2 : getIBD1ToIBD2Flows()) {
						if (f2.v2 == otherEnd) {
							if (f2.v1.isSameSuffix(port)) {
								confirmConjugation(f2.v1, port, seq);
								result.add(f2.v1);
							}
						}
					}
				}
			}
		}
		
		return result;
	}
	
	private List<IBD2Flow> getIBD2FlowSeq(IBD2Port initialPort) {
		for (IBD2Flow f : getIBD2Flows()) {
			if (f.v1 == initialPort) {
				return getIBD2FlowSeq(f);
			}
		}
		
		return Collections.emptyList();
	}
	
	private List<IBD2Flow> getIBD2FlowSeq(IBD2Flow initialFlow) {
		List<IBD2Flow> result = new ArrayList<IBD2Flow>();
		result.add(initialFlow);
		boolean done = false;
		
		while (!done) {
			done = true;
			
			IBD2Flow firstFlow = result.get(0);
			IBD2Flow lastFlow = result.get(result.size() - 1);
			
			for (IBD2Flow f : getIBD2Flows()) {
				if (result.size() > 1 && f.v1 == lastFlow.v2 && f.v2 == firstFlow.v1) {
					throw new Error("Interface flow port must not be part of a flow cycle!");
				}
				
				if (f.v2 == firstFlow.v1 && f.v1 != firstFlow.v2) {
					if (result.get(0) != firstFlow) {
						throw new Error("Interface flow port is connected to too many other interface flow ports!");
					}
					
					result.add(0, f);
					done = false;
				}
				
				if (f.v1 == lastFlow.v2 && f.v2 != lastFlow.v1) {
					if (result.get(result.size() - 1) != lastFlow) {
						throw new Error("Interface flow port is connected to too many other interface flow ports!");
					}
					
					result.add(f);
					done = false;
				}
			}
		}
		
		return result;
	}
	
	private static void confirmConjugation(IBD1Port p1, IBD1Port p2, List<IBD2Flow> debugSeq) {
		if (!p1.getType().equals(p2.getType())) {
			throw new Error("Port and its conjugate must have matching types!");
		}
		
		if (p1.computeDir() != p2.computeDir().getOpposite()) {
			String msg = "Ports and their conjugates must have opposite directions, but found";
			msg += "\n\t\t" + p1.computeDir().text + " " + p1.owner.getName() + "." + p1.name;
			msg += "\n\t\t" + p2.computeDir().text + " " + p2.owner.getName() + "." + p2.name;
			
			if (debugSeq != null) {
				msg += "\nconnected via";
				
				for (IBD2Flow s : debugSeq) {
					msg += "\n\t\t" + s.v1.owner.getType().getSimpleName() + "." + s.v1.id + " -> " + s.v2.owner.getType().getSimpleName() + "." + s.v2.id;
					
					for (InterfacePort ip : s.v1.getLegacyPts()) {
						msg += "\n\t\t\tfirst found in \"" + ip + "\"";
					}
					
					for (InterfacePort ip : s.v2.getLegacyPts()) {
						msg += "\n\t\t\tsecond found in \"" + ip + "\"";
					}
				}
			}
			
			throw new Error(msg);
		}
	}
}
