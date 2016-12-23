//package com.dnake.smart.core.base;
//
//import lombok.AllArgsConstructor;
//import lombok.Setter;
//import lombok.ToString;
//
//import java.lang.reflect.Array;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//
//public class QueueTest {
//
//	public static void array2() {
//		String[] arr = (String[]) new Object[5];
//		System.nanoTime();
//	}
//
//	public static void array() {
//		String[] arr = (String[]) Array.newInstance(String.class, 5);
//		System.out.println(arr.length);
//		for (int i = 0; i < arr.length; i++) {
//			arr[i] = "s" + i;
//		}
//
//		String s = "a";
//		System.out.println(s.getClass().isArray());
//		System.out.println(s.getClass().getComponentType());
//		System.out.println("type check==>" + (arr.getClass().getComponentType() == String.class));
//
//		System.out.println(Array.newInstance(int.class, 0).getClass());
//		System.out.println(Array.newInstance(int.class, 0).getClass().getComponentType());
//	}
//
//	public static void main(String[] args) {
//		testCopy();
//	}
//
//	public static <T, U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
//		@SuppressWarnings("unchecked")
//		T[] copy = (newType == Object[].class) ? (T[]) new Object[newLength] : (T[]) Array.newInstance(newType.getComponentType(), newLength);
//		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
//		return copy;
//	}
//
//	public static void que() {
//		BlockingQueue<String> queue = new LinkedBlockingQueue<>();
//		for (int i = 0; i < 10; i++) {
//			queue.offer("s" + i);
//		}
//		LinkedBlockingQueue<String> copy = new LinkedBlockingQueue<>(queue);
//		queue.clear();
//		System.out.println(copy.size());
//		System.out.println(Object[].class);
//	}
//
//	private static void testCopy() {
//		List<User> list = new ArrayList<>();
//		for (int i = 0; i < 5; i++) {
//			User user = new User(1, "a" + i);
//			list.add(user);
//		}
//
//		List<User> list2 = new ArrayList<>(list);
//		list.get(1).setName("update");
//		System.out.println(Arrays.toString(list2.toArray()));
//		list.clear();
//		System.out.println(Arrays.toString(list2.toArray()));
//	}
//
//	public static native void arraycopy(Object src, int srcPos, Object dest, int destPos, int length);
//
//	@AllArgsConstructor
//	@Setter
//	@ToString
//	private static class User {
//		private int id;
//		private String name;
//	}
//}