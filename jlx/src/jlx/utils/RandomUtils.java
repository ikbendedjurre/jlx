package jlx.utils;

import java.util.*;

public class RandomUtils {
	public static <T> T getElem(Collection<T> elems) {
		int randomIndex = (int)(Math.random() * elems.size());
		Iterator<T> q = elems.iterator();
		
		while (randomIndex > 0) {
			randomIndex--;
			q.next();
		}
		
		return q.next();
	}
	
	public static <T> List<T> shuffle(Collection<T> elems) {
		List<T> result = new ArrayList<T>(elems);
		Collections.shuffle(result);
		return result;
	}
}
