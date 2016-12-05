package com.dnake.smart.core.kit;

public class ConvertKit {

	public static <T> T getEnum(Class<T> clazz, int index) {
		T[] enums = clazz.getEnumConstants();
		if (enums == null) {
			return null;
		}
		if (index > -1 && index < enums.length) {
			return enums[index];
		}
		return null;
	}

	public static <T> T getEnum(Class<T> clazz, String name, boolean strict) {
		if (name == null) {
			return null;
		}
		T[] enums = clazz.getEnumConstants();
		if (enums == null) {
			return null;
		}
		if (strict) {
			for (T t : enums) {
				if (name.equals(t.toString())) {
					return t;
				}
			}
		} else {
			for (T t : enums) {
				if (name.equalsIgnoreCase(t.toString())) {
					return t;
				}
			}
		}

		return null;
	}

	public static <T> T getEnum(Class<T> clazz, String name) {
		return getEnum(clazz, name, true);
	}

}
