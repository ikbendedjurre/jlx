package jlx.behave.proto.gui;

import jlx.behave.stable.DecaStableOutputEvolution;

public class DecaFourScenarioMismatch {
	private String header;
	private DecaStableOutputEvolution expected;
	private DecaStableOutputEvolution found;
	
	public DecaFourScenarioMismatch(String header, DecaStableOutputEvolution found, DecaStableOutputEvolution expected) {
		this.header = header;
		this.expected = expected;
		this.found = found;
	}
	
	public String getHeader() {
		return header;
	}
	
	public DecaStableOutputEvolution getExpected() {
		return expected;
	}
	
	public DecaStableOutputEvolution getFound() {
		return found;
	}
	
//	/**
//	 * Returns descriptions of differences between the outputs of the given transition and the outputs that are expected.
//	 */
//	public static Set<ScenarioMismatch> fromOutput(DecaStableTransition t, Set<List<Map<ReprPort, ASALSymbolicValue>>> expectedOutputs) {
//		Set<ScenarioMismatch> result = new HashSet<ScenarioMismatch>();
//		
//		for (List<Map<ReprPort, ASALSymbolicValue>> expected : expectedOutputs) {
//			ScenarioMismatch mismatch = fromOutput(t, expected);
//			
//			if (mismatch != null) {
//				result.add(mismatch);
//			}
//		}
//		
//		return result;
//	}
//	
//	/**
//	 * Returns NULL if the given transition CONTAINS the expected outputs.
//	 * Returns a description of the difference, otherwise.
//	 */
//	public static ScenarioMismatch fromOutput(DecaStableTransition t, List<Map<ReprPort, ASALSymbolicValue>> expectedOutputSeq) {
//		List<Map<ReprPort, ASALSymbolicValue>> found = new ArrayList<Map<ReprPort, ASALSymbolicValue>>();
//		
//		//The output that is expected first should always match with the current output:
//		{
//			Map<ReprPort, ASALSymbolicValue> valuePerPort = t.getSeq().get(0).getOutputVal().getValuePerPort();
//			Map<ReprPort, ASALSymbolicValue> diffs = getDiffs(valuePerPort, expectedOutputSeq.get(0));
//			
//			if (diffs.size() > 0) {
//				found.add(diffs);
//				return new ScenarioMismatch("Invalid start of output evolution", expectedOutputSeq, found);
//			}
//			
//			found.add(expectedOutputSeq.get(0));
//		}
//		
//		int expectIndex = 0;
//		
//		for (int observeIndex = 1; observeIndex < t.getSeq().size(); observeIndex++) {
//			Map<ReprPort, ASALSymbolicValue> valuePerPort = t.getSeq().get(observeIndex).getOutputVal().getValuePerPort();
//			Map<ReprPort, ASALSymbolicValue> diffs = getDiffs(valuePerPort, expectedOutputSeq.get(expectIndex));
//			
//			if (diffs.isEmpty()) {
//				//There are no differences relative to our most recent expectation.
//			} else {
//				//There is a difference!
//				//Perhaps we moved on to the next expectation (if any)?
//				if (expectIndex < expectedOutputSeq.size() - 1) {
//					Map<ReprPort, ASALSymbolicValue> diffs2 = getDiffs(valuePerPort, expectedOutputSeq.get(expectIndex + 1));
//					
//					//Still a difference, so there is a mismatch:
//					if (diffs2.size() > 0) {
//						found.add(diffs2);
//						return new ScenarioMismatch("Unexpected output evolution", expectedOutputSeq, found);
//					}
//					
//					//The next expectation is met, so continue:
//					found.add(expectedOutputSeq.get(expectIndex + 1));
//					expectIndex++;
//				} else {
//					//There is no next expectation, so there is a mismatch:
//					found.add(diffs);
//					return new ScenarioMismatch("Unexpected output evolution", expectedOutputSeq, found);
//				}
//			}
//		}
//		
//		//The output has not evolved completely:
//		if (expectIndex < expectedOutputSeq.size() - 1) {
//			return new ScenarioMismatch("Incomplete output evolution", expectedOutputSeq, found);
//		}
//		
//		//The output has full evolved exactly as expected:
//		return null;
//	}
//	
//	private static Map<ReprPort, ASALSymbolicValue> getDiffs(Map<ReprPort, ASALSymbolicValue> outputVal, Map<ReprPort, ASALSymbolicValue> expectedOutputVal) {
//		Map<ReprPort, ASALSymbolicValue> result = new HashMap<ReprPort, ASALSymbolicValue>();
//		
//		for (Map.Entry<ReprPort, ASALSymbolicValue> e : expectedOutputVal.entrySet()) {
//			if (!e.getValue().equals(outputVal.get(e.getKey()))) {
//				result.put(e.getKey(), outputVal.get(e.getKey()));
//			}
//		}
//		
//		return result;
//	}
}

