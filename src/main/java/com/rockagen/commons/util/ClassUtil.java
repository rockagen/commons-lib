/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rockagen.commons.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class Utils
 * 
 * @author RA
 * @since JDK1.6
 * @since COMMONS.LANG3
 */
public class ClassUtil extends ClassUtils {

	// ~ Instance fields ==================================================

	/**
	 * 
	 */
	private static final Logger log = LoggerFactory.getLogger(ClassUtil.class);

	// ~ Constructors ==================================================

	/**
	 */
	private ClassUtil() {

	}

	// ~ Methods ==================================================

	/**
	 * Create new instance of specified class and type
	 * 
	 * @param clazz class
	 * @param accessible accessible
	 * @param parameterTypes parameter types
	 * @param paramValue param value
	 * @param <T> t
	 * @return instance
	 */
	public static <T> T getInstance(Class<T> clazz, boolean accessible,
			Class<?>[] parameterTypes, Object[] paramValue) {
		if (clazz == null)
			return null;

		T t = null;

		try {
			if (parameterTypes != null && paramValue != null) {
				Constructor<T> constructor = clazz
						.getDeclaredConstructor(parameterTypes);
				Object[] obj = new Object[parameterTypes.length];

				System.arraycopy(paramValue, 0, obj, 0, parameterTypes.length);
				constructor.setAccessible(accessible);
				t = constructor.newInstance(obj);
			}

		} catch (SecurityException e) {
			log.error("{}",e.getMessage(),e);
		} catch (NoSuchMethodException e) {
			log.error("{}",e.getMessage(),e);
		} catch (IllegalArgumentException e) {
			log.error("{}",e.getMessage(),e);
		} catch (InstantiationException e) {
			log.error("{}",e.getMessage(),e);
		} catch (IllegalAccessException e) {
			log.error("{}",e.getMessage(),e);
		} catch (InvocationTargetException e) {
			log.error("{}",e.getMessage(),e);
		}

		return t;
	}

	/**
	 * Create new instance of specified class and type
	 * 
	 * @param clazz class
	 * @param accessible accessible
	 * @param <T> t                      
	 * @return instance
	 */
	public static <T> T getInstance(Class<T> clazz, boolean accessible) {
		if (clazz == null)
			return null;

		T t = null;

		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(accessible);
			t = constructor.newInstance();
		} catch (InstantiationException e) {
			log.error("{}",e.getMessage(),e);
		} catch (IllegalAccessException e) {
			log.error("{}",e.getMessage(),e);
		} catch (SecurityException e) {
			log.error("{}",e.getMessage(),e);
		} catch (NoSuchMethodException e) {
			log.error("{}",e.getMessage(),e);
		} catch (IllegalArgumentException e) {
			log.error("{}",e.getMessage(),e);
		} catch (InvocationTargetException e) {
			log.error("{}",e.getMessage(),e);
		}

		return t;
	}

	/**
	 * Create new instance of specified class and type
	 * 
	 * @param clazz class
	 * @param <T> t
	 * @return instance
	 */
	public static <T> T getInstance(Class<T> clazz) {
		return getInstance(clazz, false);
	}

	/**
	 * obtain fields list of specified class If recursively is true, obtain
	 * fields from all class hierarchy
	 * 
	 * @param clazz class
	 *            where fields are searching
	 * @param recursively
	 *            param
	 * @return array of fields
	 */
	public static Field[] getDeclaredFields(Class<?> clazz, boolean recursively) {
		List<Field> fields = new LinkedList<Field>();
		Field[] declaredFields = clazz.getDeclaredFields();
		Collections.addAll(fields, declaredFields);

		Class<?> superClass = clazz.getSuperclass();

		if (superClass != null && recursively) {
			Field[] declaredFieldsOfSuper = getDeclaredFields(superClass,
					true);
			if (declaredFieldsOfSuper.length > 0)
				Collections.addAll(fields, declaredFieldsOfSuper);
		}
		return fields.toArray(new Field[fields.size()]);
	}

	/**
	 * obtain field If recursively is true, obtain fields from all class
	 * hierarchy
	 * 
	 * @param clazz class
	 *            where fields are searching
	 * @param fieldName
	 *            field name
	 * @param recursively
	 *            param
	 * @return list of fields
	 */
	public static Field getDeclaredField(Class<?> clazz, String fieldName,
			boolean recursively) {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass != null && recursively) {
				return getDeclaredField(superClass, fieldName, true);
			}
		} catch (SecurityException e) {
			log.error("{}",e.getMessage(),e);
		}
		return null;

	}

	/**
	 * obtain methods list of specified class If recursively is true, obtain
	 * methods from all class hierarchy
	 * 
	 * @param clazz class
	 *            where fields are searching
	 * @param recursively
	 *            param
	 * @return array of methods
	 */
	public static Method[] getDeclaredMethods(Class<?> clazz,
			boolean recursively) {
		List<Method> methods = new LinkedList<Method>();
		Method[] declaredMethods = clazz.getDeclaredMethods();
		Collections.addAll(methods, declaredMethods);

		Class<?> superClass = clazz.getSuperclass();

		if (superClass != null && recursively) {
			Method[] declaredMethodsOfSuper = getDeclaredMethods(superClass,
					true);
			if (declaredMethodsOfSuper.length > 0)
				Collections.addAll(methods, declaredMethodsOfSuper);
		}
		return methods.toArray(new Method[methods.size()]);
	}

	/**
	 * obtain method list of specified class If recursively is true, obtain
	 * method from all class hierarchy
	 * 
	 * @param clazz class
	 * @param recursively recursively
	 * @param methodName method name
	 * @param parameterTypes parameter types
	 * @return method
	 */
	public static Method getDeclaredMethod(Class<?> clazz, boolean recursively,
			String methodName, Class<?>... parameterTypes) {

		try {
			return clazz.getDeclaredMethod(methodName,
					parameterTypes);
		} catch (NoSuchMethodException e) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass != null && recursively) {
				return getDeclaredMethod(superClass, true, methodName,
						parameterTypes);
			}
		} catch (SecurityException e) {
			log.error("{}",e.getMessage(),e);
		}
		return null;
	}

	/**
	 * obtain constructor list of specified class If recursively is true, obtain
	 * constructor from all class hierarchy
	 * 
	 * 
	 * @param clazz class
	 *            where fields are searching
	 * @param recursively
	 *            param
	 * @return array of constructors
	 */
	public static Constructor<?>[] getDeclaredConstructors(Class<?> clazz,
			boolean recursively) {
		List<Constructor<?>> constructors = new LinkedList<Constructor<?>>();
		Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
		Collections.addAll(constructors, declaredConstructors);

		Class<?> superClass = clazz.getSuperclass();

		if (superClass != null && recursively) {
			Constructor<?>[] declaredConstructorsOfSuper = getDeclaredConstructors(
					superClass, true);
			if (declaredConstructorsOfSuper.length > 0)
				Collections.addAll(constructors, declaredConstructorsOfSuper);
		}
		return constructors.toArray(new Constructor<?>[constructors.size()]);
	}

	/**
	 * obtain constructor list of specified class If recursively is true, obtain
	 * constructor from all class hierarchy
	 * 
	 * 
	 * @param clazz class
	 *            where fields are searching
	 * @param recursively
	 *            param
	 * @param parameterTypes parameter types
	 * @return constructor
	 */
	public static Constructor<?> getDeclaredConstructor(Class<?> clazz,
			boolean recursively, Class<?>... parameterTypes) {

		try {
			return clazz
					.getDeclaredConstructor(parameterTypes);
		} catch (NoSuchMethodException e) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass != null && recursively) {
				return getDeclaredConstructor(superClass, true,
						parameterTypes);
			}
		} catch (SecurityException e) {
			log.error("{}",e.getMessage(),e);
		}
		return null;
	}

	/**
	 * obtain fields list of specified class and which are annotated by incoming
	 * annotation class If recursively is true, obtain fields from all class
	 * hierarchy
	 * 
	 * @param clazz class
	 *            - where fields are searching
	 * @param annotationClass
	 *            - specified annotation class
	 * @param recursively
	 *            param
	 * @return list of annotated fields
	 */
	public static Field[] getAnnotatedDeclaredFields(Class<?> clazz,
			Class<? extends Annotation> annotationClass, boolean recursively) {
		Field[] allFields = getDeclaredFields(clazz, recursively);
		List<Field> annotatedFields = new LinkedList<Field>();

		for (Field field : allFields) {
			if (field.isAnnotationPresent(annotationClass))
				annotatedFields.add(field);
		}

		return annotatedFields.toArray(new Field[annotatedFields.size()]);
	}

	/**
	 * obtain methods list of specified class and which are annotated by
	 * incoming annotation class If recursively is true, obtain methods from all
	 * class hierarchy
	 * 
	 * @param clazz class
	 *            - where methods are searching
	 * @param annotationClass
	 *            - specified annotation class
	 * @param recursively
	 *            param
	 * @return list of annotated methods
	 */
	public static Method[] getAnnotatedDeclaredMethods(Class<?> clazz,
			Class<? extends Annotation> annotationClass, boolean recursively) {
		Method[] allMethods = getDeclaredMethods(clazz, recursively);
		List<Method> annotatedMethods = new LinkedList<Method>();

		for (Method method : allMethods) {
			if (method.isAnnotationPresent(annotationClass))
				annotatedMethods.add(method);
		}

		return annotatedMethods.toArray(new Method[annotatedMethods.size()]);
	}

	/**
	 * obtain constructors list of specified class and which are annotated by
	 * incoming annotation class If recursively is true, obtain constructors
	 * from all class hierarchy
	 * 
	 * @param clazz class
	 *            - where constructors are searching
	 * @param annotationClass
	 *            - specified annotation class
	 * @param recursively
	 *            param
	 * @return list of annotated constructors
	 */
	public static Constructor<?>[] getAnnotatedDeclaredConstructors(
			Class<?> clazz, Class<? extends Annotation> annotationClass,
			boolean recursively) {
		Constructor<?>[] allConstructors = getDeclaredConstructors(clazz,
				recursively);
		List<Constructor<?>> annotatedConstructors = new LinkedList<Constructor<?>>();

		for (Constructor<?> field : allConstructors) {
			if (field.isAnnotationPresent(annotationClass))
				annotatedConstructors.add(field);
		}

		return annotatedConstructors
				.toArray(new Constructor<?>[annotatedConstructors.size()]);
	}
}
