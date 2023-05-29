package jlx.utils;

import java.util.*;

public class ArrayLists {
	public static <K, V> Set<List<V>> allCombinations(List<? extends Collection<V>> valuesPerIndex) {
		return allCombinations(valuesPerIndex, true);
	}
	
	public static <V> Set<List<V>> allCombinations(List<? extends Collection<V>> valuesPerIndex, boolean oneValuePerKey) {
		Set<List<V>> result = new HashSet<List<V>>();
		addCombinations(valuesPerIndex, new ArrayList<V>(), 0, result, oneValuePerKey);
		return result;
	}
	
	private static <V> void addCombinations(List<? extends Collection<V>> valuesPerIndex, List<V> soFar, int key, Set<List<V>> destination, boolean oneValuePerKey) {
		if (key < valuesPerIndex.size()) {
			for (V value : valuesPerIndex.get(key)) {
				List<V> newSoFar = new ArrayList<V>(soFar);
				newSoFar.add(value);
				
				addCombinations(valuesPerIndex, newSoFar, key + 1, destination, oneValuePerKey);
			}
			
			if (!oneValuePerKey) {
				addCombinations(valuesPerIndex, new ArrayList<V>(soFar), key + 1, destination, oneValuePerKey);
			}
		} else {
			destination.add(soFar);
		}
	}
	
	private static <T> void addOrderings(List<T> remainingElems, List<T> soFar, Set<List<T>> dest) {
		if (remainingElems.isEmpty()) {
			dest.add(soFar);
			return;
		}
		
		for (int index = 0; index < remainingElems.size(); index++) {
			List<T> newRemainingElems = new ArrayList<T>(remainingElems);
			newRemainingElems.remove(index);
			List<T> newSoFar = new ArrayList<T>(soFar);
			newSoFar.add(remainingElems.get(index));
			addOrderings(newRemainingElems, newSoFar, dest);
		}
	}
	
	public static <T> Set<List<T>> allOrderings(List<T> elems) {
		Set<List<T>> result = new HashSet<List<T>>();
		addOrderings(elems, new ArrayList<T>(), result);
		return result;
	}
	
//	public static <K, V> Set<List<V>> all(Map<K, V> elemPerKey) {
//		List<K> keys = new ArrayList<K>(elemPerKey.keySet());
//		Set<List<V>> result = new HashSet<List<V>>();
//		
//		for (List<K> keyPerm : allOrderings(keys)) {
//			List<V> elemPerm = new ArrayList<V>();
//			
//			for (K key : keyPerm) {
//				elemPerm.add(elemPerKey.get(key));
//			}
//			
//			result.add(elemPerm);
//		}
//		
//		return result;
//	}
	
	private static <T> void addInterleavings(List<T> seq1, List<T> seq2, int i1, int i2, List<T> soFar, Set<List<T>> dest) {
		if (i1 == seq1.size()) {
			soFar.addAll(seq2);
			dest.add(soFar);
			return;
		}
		
		if (i2 == seq2.size()) {
			soFar.addAll(seq1);
			dest.add(soFar);
			return;
		}
		
		{
			List<T> newSoFar = new ArrayList<T>(soFar);
			newSoFar.add(seq1.get(i1));
			addInterleavings(seq1, seq2, i1 + 1, i2, newSoFar, dest);
		}
		
		{
			List<T> newSoFar = new ArrayList<T>(soFar);
			newSoFar.add(seq2.get(i2));
			addInterleavings(seq1, seq2, i1, i2 + 1, newSoFar, dest);
		}
	}
	
	private static <T> void addInterleavings(List<T> seq1, Set<List<T>> seqs2, Set<List<T>> dest) {
		for (List<T> seq2 : seqs2) {
			addInterleavings(seq1, seq2, 0, 0, new ArrayList<T>(), dest);
		}
	}
	
	/**
	 * Interleaves seq1 and seq2.
	 */
	public static <T> Set<List<T>> allInterleavings(List<T> seq1, List<T> seq2) {
		Set<List<T>> result = new HashSet<List<T>>();
		addInterleavings(seq1, seq2, 0, 0, new ArrayList<T>(), result);
		return result;
	}
	
	/**
	 * Interleaves seq1 with each seq2 in seqs2.
	 */
	public static <T> Set<List<T>> allInterleavings(List<T> seq1, Set<List<T>> seqs2) {
		Set<List<T>> result = new HashSet<List<T>>();
		addInterleavings(seq1, seqs2, result);
		return result;
	}
	
	/**
	 * Interleaves each seq1 in seqs1 with each seq2 in seqs2.
	 */
	public static <T> Set<List<T>> allInterleavings(Set<List<T>> seqs1, Set<List<T>> seqs2) {
		Set<List<T>> result = new HashSet<List<T>>();
		int i = 1;
		
		for (List<T> seq1 : seqs1) {
			System.out.println(i + " / " + seqs1.size());
			i++;
			addInterleavings(seq1, seqs2, result);
		}
		
		return result;
	}
	
	/**
	 * Interleaves all sequences in seqs.
	 */
	public static <T> Set<List<T>> allInterleavings(Collection<List<T>> seqs) {
		Set<List<T>> result = Collections.singleton(Collections.emptyList());
		
		for (List<T> seq : seqs) {
			result = allInterleavings(seq, result);
		}
		
		return result;
	}
	
	/**
	 * Interleaves all sequences in the seqPerKey map.
	 */
	public static <K, V> Set<List<V>> allInterleavings(Map<K, List<V>> seqPerKey) {
		return allInterleavings(new ArrayList<List<V>>(seqPerKey.values()));
	}
}
