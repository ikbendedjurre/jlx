package jlx.utils;

import java.util.*;

public class LDDFactoryTests {
	private final static int MAX_LEVEL = 4;
	private final static LDDFactory f = new LDDFactory(MAX_LEVEL);
	
	public static void main(String[] args) {
		test1();
		test2();
		test3();
		test4();
		
		for (int index = 1; index <= 1000; index++) {
			test5();
		}
		
		test6();
		System.out.println("done");
	}
	
	private static void test1() {
		System.out.println("test 1");
		
		LDDFactory.LDD x = f.emptyLDD();
		
		for (int level = 0; level < MAX_LEVEL; level++) {
			for (int v = 0; v < 10; v++) {
				if (x.contains(Collections.singletonMap(level, v))) {
					throw new Error("");
				}
			}
		}
	}
	
	private static void test2() {
		System.out.println("test 2");
		
		for (int level = 0; level < MAX_LEVEL; level++) {
			Map<Integer, Integer> m = Collections.singletonMap(level, 111 * (level + 1));
			LDDFactory.LDD x = f.mapToLDD(m);
			
			if (!x.contains(m)) {
				throw new Error("");
			}
		}
	}
	
	private static void test3() {
		List<Map<Integer, Integer>> ms = new ArrayList<Map<Integer, Integer>>();
		
		for (int level = 0; level < MAX_LEVEL; level++) {
			Map<Integer, Integer> m = Collections.singletonMap(level, 111 * (level + 1));
			ms.add(m);
		}
		
		List<LDDFactory.LDD> xs = new ArrayList<LDDFactory.LDD>();
		
		for (Map<Integer, Integer> m : ms) {
			LDDFactory.LDD x = f.mapToLDD(m);
			xs.add(x);
		}
		
		System.out.println("test 3/a");
		
		LDDFactory.LDD y = f.emptyLDD();
		
		for (LDDFactory.LDD x : xs) {
			LDDFactory.LDD temp = f.union(y, x);
			
			if (!temp.containsAll(x)) {
				x.printGraphvizFile("ldd-test-x");
				y.printGraphvizFile("ldd-test-y");
				temp.printGraphvizFile("ldd-test-temp");
				throw new Error("");
			}
			
			y = temp;
		}
		
		System.out.println("test 3/b");
		
		for (Map<Integer, Integer> m : ms) {
			if (!y.contains(m)) {
				throw new Error("");
			}
		}
		
		System.out.println("test 3/c");
		
		for (LDDFactory.LDD x : xs) {
			LDDFactory.LDD z = f.intersection(y, x);
			
			if (!z.equals(x)) {
				throw new Error("");
			}
		}
	}
	
	private static void test4() {
		System.out.println("test 4/a");
		
		Set<Map<Integer, Integer>> ms = new HashSet<Map<Integer, Integer>>();
		
		for (int index = 1; index < 1000; index++) {
			int randomLevel = (int)(Math.random() * MAX_LEVEL);
			int randomValue = (int)(Math.random() * 10000);
			
			Map<Integer, Integer> m = Collections.singletonMap(randomLevel, randomValue);
			ms.add(m);
		}
		
		List<LDDFactory.LDD> xs = new ArrayList<LDDFactory.LDD>();
		
		for (Map<Integer, Integer> m : ms) {
			LDDFactory.LDD x = f.mapToLDD(m);
			xs.add(x);
		}
		
		System.out.println("test 4/b");
		
		LDDFactory.LDD y = f.emptyLDD();
		
		for (LDDFactory.LDD x : xs) {
			y = f.union(y, x);
		}
		
		System.out.println("test 4/c");
		
		LDDFactory.LDD z1 = f.emptyLDD();
		LDDFactory.LDD z2 = f.emptyLDD();
		
		for (Map<Integer, Integer> m : ms) {
			if (Math.random() < 0.5) {
				z1 = f.union(z1, f.mapToLDD(m));
			} else {
				z2 = f.union(z2, f.mapToLDD(m));
			}
		}
		
		System.out.println("test 4/d");
		
		if (!f.intersection(y, z1).equals(z1)) {
			throw new Error();
		}
		
		if (!f.intersection(y, z2).equals(z2)) {
			throw new Error();
		}
		
		if (!f.intersection(z1, z2).equals(f.emptyLDD())) {
			z1.printGraphvizFile("ldd-test-z1");
			z2.printGraphvizFile("ldd-test-z2");
			f.intersection(z1, z2).printGraphvizFile("ldd-test-intersection");
			throw new Error();
		}
	}
	
	private static void test5() {
		System.out.println("test 5/a");
		
		Set<Map<Integer, Integer>> ms = new HashSet<Map<Integer, Integer>>();
		
		for (int index = 1; index < 10; index++) {
			Map<Integer, Integer> m = new HashMap<Integer, Integer>();
			
			for (int level = 0; level < MAX_LEVEL; level++) {
//				if (Math.random() < 0.75) {
					int randomValue = (int)(Math.random() * 10000);
					m.put(level, randomValue);
//				}
			}
			
			if (m.size() > 0) {
				ms.add(m);
			}
		}
		
		List<LDDFactory.LDD> xs = new ArrayList<LDDFactory.LDD>();
		
		for (Map<Integer, Integer> m : ms) {
			LDDFactory.LDD x = f.mapToLDD(m);
			xs.add(x);
		}
		
		System.out.println("test 5/b");
		
		LDDFactory.LDD y = f.emptyLDD();
		
		for (LDDFactory.LDD x : xs) {
			y = f.union(y, x);
		}
		
		System.out.println("test 5/c");
		
		LDDFactory.LDD z1 = f.emptyLDD();
		LDDFactory.LDD z2 = f.emptyLDD();
		
		for (Map<Integer, Integer> m : ms) {
			if (Math.random() < 0.5) {
				z1 = f.union(z1, f.mapToLDD(m));
			} else {
				z2 = f.union(z2, f.mapToLDD(m));
			}
		}
		
		System.out.println("test 5/d");
		
		if (!f.intersection(y, z1).equals(z1)) {
			y.printGraphvizFile("ldd-test-y");
			z1.printGraphvizFile("ldd-test-z1");
			f.intersection(y, z1).printGraphvizFile("ldd-test-intersection");
			throw new Error();
		}
		
		if (!f.intersection(y, z2).equals(z2)) {
			y.printGraphvizFile("ldd-test-y");
			z2.printGraphvizFile("ldd-test-z2");
			f.intersection(y, z2).printGraphvizFile("ldd-test-intersection");
			throw new Error();
		}
		
		if (!f.intersection(z1, z2).equals(f.emptyLDD())) {
			z1.printGraphvizFile("ldd-test-z1");
			z2.printGraphvizFile("ldd-test-z2");
			f.intersection(z1, z2).printGraphvizFile("ldd-test-intersection");
			throw new Error();
		}
	}
	
	private static void test6() {
		System.out.println("test 6/a");
		
		Set<Map<Integer, Integer>> ms1 = new HashSet<Map<Integer, Integer>>();
		Set<Map<Integer, Integer>> ms2 = new HashSet<Map<Integer, Integer>>();
		
		for (int index = 1; index < 10; index++) {
			Map<Integer, Integer> m1 = new HashMap<Integer, Integer>();
			Map<Integer, Integer> m2 = new HashMap<Integer, Integer>();
			
			for (int level = 0; level < MAX_LEVEL; level++) {
				int randomValue = (int)(Math.random() * 10000);
				
				if (Math.random() < 0.9) {
					m1.put(level, randomValue);
				}
				
				if (Math.random() < 0.9) {
					m2.put(level, randomValue);
				}
			}
			
			ms1.add(m1);
			ms2.add(m2);
		}
		
		List<LDDFactory.LDD> xs1 = new ArrayList<LDDFactory.LDD>();
		List<LDDFactory.LDD> xs2 = new ArrayList<LDDFactory.LDD>();
		
		for (Map<Integer, Integer> m1 : ms1) {
			LDDFactory.LDD x1 = f.mapToLDD(m1);
			xs1.add(x1);
		}
		
		for (Map<Integer, Integer> m2 : ms2) {
			LDDFactory.LDD x2 = f.mapToLDD(m2);
			xs2.add(x2);
		}
		
		System.out.println("test 6/b");
		
		LDDFactory.LDD y1 = f.emptyLDD();
		LDDFactory.LDD y2 = f.emptyLDD();
		
		for (LDDFactory.LDD x1 : xs1) {
			y1 = f.union(y1, x1);
		}
		
		for (LDDFactory.LDD x2 : xs2) {
			y2 = f.union(y2, x2);
		}
		
		System.out.println("test 6/c");
		
		for (LDDFactory.LDD x1 : xs1) {
			if (!f.containsAll(y1, x1)) {
				y1.printGraphvizFile("ldd-test-y1");
				x1.printGraphvizFile("ldd-test-x1");
				f.intersection(x1, y1).printGraphvizFile("ldd-test-intersection");
				throw new Error();
			}
		}
		
		for (LDDFactory.LDD x2 : xs2) {
			if (!f.containsAll(y2, x2)) {
				y2.printGraphvizFile("ldd-test-y2");
				x2.printGraphvizFile("ldd-test-x2");
				f.intersection(x2, y2).printGraphvizFile("ldd-test-intersection");
				throw new Error();
			}
		}
	}
}

