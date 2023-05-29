package jlx.common;

import java.util.*;

public class FileLocations {
	private List<String> names;
	private List<FileLocation> fileLocations;
	
	private FileLocations() {
		names = Collections.emptyList();
		fileLocations = Collections.emptyList();
	}
	
	private static <T> List<T> add(List<T> elems, T elem) {
		List<T> elemsCopy = new ArrayList<T>(elems);
		elemsCopy.add(elem);
		return Collections.unmodifiableList(elemsCopy);
	}
	
	public static FileLocations from(String name, FileLocation fileLocation) {
		FileLocations result = new FileLocations();
		result.and(name, fileLocation);
		return result;
	}
	
	public static FileLocations from(String name, Collection<FileLocation> fileLocations) {
		FileLocations result = new FileLocations();
		result.and(name, fileLocations);
		return result;
	}
	
	public static FileLocations from(Map<String, Collection<FileLocation>> fileLocationsPerName) {
		FileLocations result = new FileLocations();
		result.and(fileLocationsPerName);
		return result;
	}
	
	public FileLocations and(String name, FileLocation fileLocation) {
		names = add(names, name);
		fileLocations = add(fileLocations, fileLocation);
		return this;
	}
	
	public FileLocations and(String name, Collection<FileLocation> fileLocations) {
		for (FileLocation fileLocation : fileLocations) {
			and(name, fileLocation);
		}
		
		return this;
	}
	
	public FileLocations and(Map<String, Collection<FileLocation>> fileLocationsPerName) {
		SortedMap<String, Collection<FileLocation>> temp = new TreeMap<String, Collection<FileLocation>>();
		temp.putAll(fileLocationsPerName);
		
		for (Map.Entry<String, Collection<FileLocation>> entry : temp.entrySet()) {
			and(entry.getKey(), entry.getValue());
		}
		
		return this;
	}
	
	public List<String> names() {
		return names;
	}
	
	public List<FileLocation> fileLocations() {
		return fileLocations;
	}
	
	@Override
	public String toString() {
		String result = "";
		
		for (int index = 0; index < names.size(); index++) {
			result += "\n\t" + names.get(index) + ":" + fileLocations.get(index);
		}
		
		return result;
	}
}

