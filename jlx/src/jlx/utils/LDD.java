package jlx.utils;

import java.util.*;

/**
 * Stores a data structure that can be used as Set&lt;Map&lt;K, V&gt;&gt; where K is fixed and known in advance.
 * Addition and containment operations are more efficient because it uses List Decision Diagram (LDD) techniques
 * "Symbolic reachability for process algebras with recursive data types" by Stefan Blom, Jan Cornelis van de Pol;
 * "Efficient learning and analysis of system behavior" by Jeroen Meijer).
 */
public class LDD<K, V> implements Set<Map<K, V>> {
	private final static int UNASSIGNED = -1;
	private final static Node FALSE = new Node();
	private final static Node TRUE = new Node();
	
	private static class Node {
		private int id;
		private Node down; //Can never be FALSE; denoted with =
		private Node right; //Can never be TRUE; denoted with >
		
		public Node() {
			id = UNASSIGNED; //Null.
			down = TRUE;
			right = FALSE;
		}
		
		public boolean add(Node other, Map<Integer, Set<Node>> cachePerDepth, int depth) {
			if (other.id == id) {
				//Value is here.
				//Done if this is the last node, otherwise add the rest:
				if (down == TRUE) {
					return false;
				} else {
					return down.add(other.down, cachePerDepth, depth + 1);
				}
			}
			
			if (other.id > id) {
				//Value not found.
				//Add immediately after this node if this node has no horizontal successor.
				//Otherwise, let the horizontal successor deal with the value:
				if (right == FALSE) {
					right = deepcopyFromOtherLDD(other, cachePerDepth, depth);
					return true;
				} else {
					return right.add(other, cachePerDepth, depth);
				}
			}
			
			// [ other.id < id ]
			
			//Insert value before "this" value:
			Node temp = new Node();
			temp.id = other.id;
			temp.down = deepcopyFromOtherLDD(other.down, cachePerDepth, depth + 1);
			temp.right = this;
			
			//There could be more values "to the right", add them as well:
			if (other.right != FALSE) {
				add(other.right, cachePerDepth, depth);
			}
			
			return true;
		}
		
		public int computeSize() {
			return 0;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(down, id, right);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Node)) {
				return false;
			}
			Node other = (Node) obj;
			return Objects.equals(down, other.down) && id == other.id && Objects.equals(right, other.right);
		}
	}
	
	private static Node deepcopyFromOtherLDD(Node n, Map<Integer, Set<Node>> cachePerDepth, int depth) {
		if (n == TRUE) {
			return TRUE;
		}
		
		if (n == FALSE) {
			return FALSE;
		}
		
		Set<Node> cachedNodes = cachePerDepth.get(depth);
		
		for (Node x : cachedNodes) {
			if (x.equals(n)) {
				return x;
			}
		}
		
		Node result = new Node();
		result.id = n.id;
		result.down = deepcopyFromOtherLDD(n.down, cachePerDepth, depth + 1);
		result.right = deepcopyFromOtherLDD(n.right, cachePerDepth, depth);
		return result;
	}
	
	private List<K> orderedKeys;
	private Map<Integer, V> valuePerId;
	private Map<V, Integer> idPerValue;
	private Map<Integer, Set<Node>> cachePerDepth;
	private Node root;
	
	public LDD(List<? extends K> keys) {
		if (keys.isEmpty()) {
			throw new Error("No keys!");
		}
		
		orderedKeys = new ArrayList<K>(keys);
		valuePerId = new HashMap<Integer, V>();
		idPerValue = new HashMap<V, Integer>();
		cachePerDepth = new HashMap<Integer, Set<Node>>();
		
		for (int index = 0; index < keys.size(); index++) {
			cachePerDepth.put(index, new HashSet<Node>());
		}
		
		root = null;
	}
	
	public LDD(List<? extends K> keys, Map<K, V> source) {
		this(keys);
		
		assign(source);
	}
	
	private int getOrCreateId(V v) {
		if (v == null) {
			return UNASSIGNED;
		}
		
		Integer result = idPerValue.get(v);
		
		if (result != null) {
			return result;
		}
		
		result = idPerValue.size();
		idPerValue.put(v, result);
		valuePerId.put(result, v);
		return result;
	}
	
	@Override
	public void clear() {
		root = null; //Clear cache as well?
	}
	
	public void assign(Map<K, V> source) {
		if (!orderedKeys.containsAll(source.keySet())) {
			throw new Error("Unknown keys!");
		}
		
		root.id = getOrCreateId(source.get(orderedKeys.get(0)));
		Node prev = root;
		
		for (int index = 1; index < orderedKeys.size(); index++) {
			Node node = new Node();
			node.id = getOrCreateId(source.get(orderedKeys.get(index)));
			prev.down = node;
		}
	}
	
	@Override
	public boolean add(Map<K, V> e) {
		return addAll(new LDD<K, V>(orderedKeys, e));
	}
	
	public boolean addAll(LDD<K, V> other) {
		if (!other.orderedKeys.equals(orderedKeys)) {
			throw new Error("Different keys!");
		}
		
		if (other.root == null) {
			return false;
		}
		
		if (root == null) {
			root = deepcopyFromOtherLDD(other.root, cachePerDepth, 0);
		}
		
		return root.add(other.root, cachePerDepth, 0);
	}
	
	@Override
	public boolean addAll(Collection<? extends Map<K, V>> c) {
		
		
		return false;
	}
	
	@Override
	public int size() {
		return root.computeSize();
	}
	
	@Override
	public boolean isEmpty() {
		return root == null;
	}
	
	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Iterator<Map<K, V>> iterator() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}
}
