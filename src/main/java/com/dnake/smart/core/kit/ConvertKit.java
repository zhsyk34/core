package com.dnake.smart.core.kit;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ConvertKit {

	public static LocalDateTime from(long millis) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
	}

	public static void main(String[] args) {
		System.out.println(from(System.currentTimeMillis()));
	}

	public static String fillZero(int i, int length) {
		if (length < 1) {
			throw new RuntimeException("length must > 0.");
		}
		return String.format("%0" + length + "d", i);
	}

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
