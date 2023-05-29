package jlx.behave.proto;

import java.util.*;

import jlx.asal.j.JScope;

public class DecaFourStateConfigEncoder {
	private Map<JScope, Data> dataPerScope;
	
	private static class Data {
		public final long mask;
		public final Map<Long, DecaFourVertex> vtxPerCode;
		public final Map<DecaFourVertex, Long> codePerVtx;
		
		private Data(long mask) {
			this.mask = mask;
			
			vtxPerCode = new HashMap<Long, DecaFourVertex>();
			codePerVtx = new HashMap<DecaFourVertex, Long>();
		}
	}
	
	public DecaFourStateConfigEncoder(DecaFourStateMachines sms) {
		int totalBitCount = 0;
		
		for (Map.Entry<JScope, DecaFourStateMachine> e : sms.smPerScope.entrySet()) {
			totalBitCount += Integer.toBinaryString(e.getValue().vertices.size() + 1).length();
		}
		
		if (totalBitCount >= 64) {
			throw new Error("Too many vertices for LONG encoding (" + totalBitCount + ")!");
		}
		
		dataPerScope = new HashMap<JScope, Data>();
		int offset = 0;
		
		for (Map.Entry<JScope, DecaFourStateMachine> e : sms.smPerScope.entrySet()) {
			int bitCount = Integer.toBinaryString(e.getValue().vertices.size()).length();
			Data data = new Data(createOnes(bitCount) << offset);
			long vertexIndex = 1L;
			
			for (DecaFourVertex v : e.getValue().vertices) {
				long code = vertexIndex << offset;
				data.codePerVtx.put(v, code);
				data.vtxPerCode.put(code, v);
				vertexIndex++;
			}
			
			dataPerScope.put(e.getKey(), data);
			offset += bitCount;
		}
	}
	
	private static long createOnes(int count) {
		long result = 0L;
		
		for (int index = 0; index < count; index++) {
			result = result | (1L << index);
		}
		
		return result;
	}
	
	public Map<JScope, DecaFourVertex> decode(long bits) {
		Map<JScope, DecaFourVertex> result = new HashMap<JScope, DecaFourVertex>();
		
		for (Map.Entry<JScope, Data> e : dataPerScope.entrySet()) {
			result.put(e.getKey(), e.getValue().vtxPerCode.get(bits & e.getValue().mask));
		}
		
		return result;
	}
	
	public long encode(Map<JScope, DecaFourVertex> vtxs) {
		long result = 0L;
		
		for (Map.Entry<JScope, DecaFourVertex> e : vtxs.entrySet()) {
			result = result | dataPerScope.get(e.getKey()).codePerVtx.get(e.getValue());
		}
		
		return result;
	}
}

