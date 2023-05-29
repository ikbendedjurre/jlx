package jlx.asal.j;

import java.lang.reflect.*;
import java.util.*;

import jlx.asal.vars.ASALVariable;
import jlx.common.ReflectionUtils;
import jlx.utils.Texts;

public class JTypePrinter {
	private static class TargetLines {
		public final JScope scope;
		public final boolean useNewLines;
		public final List<String> dest;
		
		public TargetLines(JScope scope, boolean useNewLines, List<String> dest) {
			this.scope = scope;
			this.useNewLines = useNewLines;
			this.dest = dest;
		}
		
		public void append(String s) {
			int lastIndex = dest.size() - 1;
			dest.set(lastIndex, dest.get(lastIndex) + s);
		}
	}
	
	private abstract static class Fragment {
		public abstract int append(JType instance, TargetLines targetLines, int indentation, int depth);
	}
	
	private static class LiteralFragment extends Fragment {
		public final String literal;
		
		public LiteralFragment(String literal) {
			this.literal = literal;
		}
		
		@Override
		public int append(JType instance, TargetLines targetLines, int indentation, int depth) {
			targetLines.append(literal);
			return indentation;
		}
	}
	
	private static class FieldFragment extends Fragment {
		public final Field field;
		public final String requiredLabel;
		
		public FieldFragment(Field field, String requiredLabel) {
			this.field = field;
			this.requiredLabel = requiredLabel;
		}
		
		@Override
		public int append(JType instance, TargetLines targetLines, int indentation, int depth) {
			try {
				if (ReflectionUtils.isPrimitive(field)) {
					Object o = field.get(instance);
					targetLines.append(String.valueOf(o).toUpperCase());
					return indentation;
				}
				
				JType fieldValue = JType.class.cast(field.get(instance));
				return toStringList(fieldValue, instance, requiredLabel, targetLines, indentation, depth);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new Error("Should not happen!", e);
			}
		}
	}
	
	private static class IndentFragment extends Fragment {
		public final int delta;
		
		public IndentFragment(int delta) {
			this.delta = delta;
		}
		
		@Override
		public int append(JType instance, TargetLines targetLines, int indentation, int depth) {
			return indentation + delta;
		}
	}
	
	private static class NewLineFragment extends Fragment {
		public NewLineFragment() {
			//Empty.
		}
		
		@Override
		public int append(JType instance, TargetLines targetLines, int indentation, int depth) {
			if (targetLines.useNewLines) {
				targetLines.dest.add("\t".repeat(indentation));
			} else {
				targetLines.append(" ");
			}
			
			return indentation;
		}
	}
	
	private final static Map<Class<? extends JType>, JTypePrinter> cache = new HashMap<>();
	
	public final Class<? extends JType> xtypeClz;
	public final String strFormat;
	public final Set<String> labels;
	public final List<Fragment> fragments;
	
	public static String toString(JType start, JScope scope, boolean useNewLines) {
		List<String> stringList = toStringList(start, scope, useNewLines);
		
		if (stringList.isEmpty()) {
			return "";
		}
		
		return Texts.concat(stringList, "\n");
	}
	
	public static List<String> toStringList(JType start, JScope scope, boolean useNewLines) {
		List<String> result = new ArrayList<String>();
		result.add("");
		toStringList(start, null, null, new TargetLines(scope, useNewLines, result), 0, 0);
		return result;
	}
	
	/**
	 * Returns the new indentation.
	 */
	private static int toStringList(JType start, JType superOfStart, String requiredLabel, TargetLines targetLines, int indentation, int depth) {
//		if (depth > 100) {
//			System.exit(0);
//		}
		
		if (targetLines.scope != null) {
			ASALVariable foundElemName = targetLines.scope.getVarInScope(start);
			
			if (foundElemName != null) {
				targetLines.append(foundElemName.getName());
				//We added this much later, was this a bug though:
				return indentation;
			}
		}
		
		if (ReflectionUtils.isFinal(start.getClass())) {
			JTypePrinter f = cache.get(start.getClass());
			
			if (f == null) {
				f = new JTypePrinter(start.getClass());
				cache.put(start.getClass(), f);
			}
			
			if (requiredLabel != null && !f.labels.contains(requiredLabel)) {
				return indentation;
			}
			
			int newIndentation = indentation;
			
			for (Fragment ff : f.fragments) {
				newIndentation = ff.append(start, targetLines, newIndentation, depth + 1);
			}
			
			return newIndentation;
		}
		
//		if (start.isInited()) {
//			if (start.getAccessedInstance() != null) {
//				if (start.getAccessedInstance() == superOfStart) {
//					return indentation;
//				}
//				
//				data.append("(");
//				int newIndentation = toStringList(start.getAccessedInstance(), start, null, data, indentation, depth + 1);
//				data.append("." + start.getAccessorField().getName()); 
//				return newIndentation;
//			}
//			
//			if (start.getCastInstance() != null) {
//				if (start.getCastInstance() == superOfStart) {
//					return indentation;
//				}
//				
//				data.append("(" + start.getCastTargetClz().getSimpleName() + ")(");
//				int newIndentation = toStringList(start.getCastInstance(), start, null, data, indentation, depth + 1);
//				data.append(")"); 
//				return newIndentation;
//			}
//			
//			throw new Error("Should not happen; no support for special use of " + JType.class.getSimpleName() + "!");
//		}
		
		targetLines.append("???");
		return indentation;
	}
	
	private JTypePrinter(Class<? extends JType> xtypeClz) {
		this.xtypeClz = xtypeClz;
		
		strFormat = extractStrFormat();
		System.out.println(xtypeClz.getCanonicalName() + " -> " + strFormat);
		labels = extractLabels();
		fragments = extractFragments();
	}
	
	private String extractStrFormat() {
		JTypeName typeName = xtypeClz.getAnnotation(JTypeName.class);
		JTypeTextify typeTextify = xtypeClz.getAnnotation(JTypeTextify.class);
		
		if (typeName != null && typeTextify != null) {
			throw new Error("Cannot have both " + JTypeName.class.getCanonicalName() + " and " + JTypeTextify.class.getCanonicalName() + "!");
		}
		
		if (typeTextify != null) {
			if (typeTextify.format() == null) {
				throw new Error("Should not be NULL!");
			}
			
			return typeTextify.format();
		}
		
		if (typeName != null) {
			if (typeName.s() == null) {
				throw new Error("Should not be NULL!");
			}
			
			return "\"" + escapeWhitespace(typeName.s()) + addDefaultStrFormat() + "\"";
		}
		
		return "\"" + JType.getType(xtypeClz).getSimpleName() + "." + xtypeClz.getSimpleName() + addDefaultStrFormat() + "\"";
	}
	
	private static String escapeWhitespace(String s) {
		String[] split = s.split(" ");
		String result = split[0];
		
		for (int i = 1; i < split.length; i++) {
			result = result + " _ " + split[i];
		}
		
		return result;
	}
	
	private String addDefaultStrFormat() {
		List<String> fieldNames = new ArrayList<String>();
		
		for (Field f : xtypeClz.getFields()) {
			if (!ReflectionUtils.isStatic(f)) {
				fieldNames.add("#" + f.getName());
			}
		}
		
		if (fieldNames.isEmpty()) {
			return "";
		}
		
		return " ( " + Texts.concat(fieldNames, " , _ ") + " )";
	}
	
	private Set<String> extractLabels() {
		Set<String> result = new HashSet<String>();
		
		for (String s : strFormat.split(" ")) {
			if (s.startsWith("[") && s.endsWith("]") && !s.startsWith("[super.")) {
				result.add(s.substring(1, s.length() - 1));
			}
		}
		
		return Collections.unmodifiableSet(result);
	}
	
	private Field getFieldByName(String fieldName) {
		try {
			return xtypeClz.getField(fieldName);
		} catch (NoSuchFieldException e) {
			return null;
		} catch (SecurityException e) {
			throw new Error("Should not happen!", e);
		}
	}
	
	private List<Fragment> extractFragments() {
		List<Fragment> result = new ArrayList<Fragment>();
		
		for (String s : strFormat.split(" ")) {
			if (s.equals("_")) {
				result.add(new LiteralFragment(" "));
				continue;
			}
			
			if (s.equals("\n")) {
				result.add(new NewLineFragment());
				continue;
			}
			
			if (s.startsWith("+") || s.startsWith("-")) {
				try {
					result.add(new IndentFragment(Integer.parseInt(s)));
				} catch (NumberFormatException e) {
					result.add(new LiteralFragment(s));
				}
				
				continue;
			}
			
			if (s.startsWith("[") && s.endsWith("]")) {
				continue;
			}
			
			if (s.startsWith("#")) {
				String[] ss = s.substring(1).split(":");
				
				if (ss.length == 1) {
					Field f = getFieldByName(ss[0]);
					
					if (f != null) {
						result.add(new FieldFragment(f, null));
					} else {
						result.add(new LiteralFragment(s));
					}
				} else {
					if (ss.length > 1) {
						Field f = getFieldByName(ss[0]);
						
						if (f != null) {
							result.add(new FieldFragment(f, ss[1]));
						} else {
							result.add(new LiteralFragment(s));
						}
					} else {
						result.add(new LiteralFragment(s));
					}
				}
			} else {
				result.add(new LiteralFragment(s));
			}
		}
		
		return Collections.unmodifiableList(result);
	}
}

