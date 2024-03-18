/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Filename:    ReflectionUtils.java
 * Description:   
 * @author:     fxw@rock-chips.com
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2012-3-11      xwf         1.0         create
 *******************************************************************/

package com.rockchip.devicetest.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class ReflectionUtils {
	
    /**
     * Get specify static field value with the given the name of field
     * @param className the class to introspect
     * @param fieldName the name of field
     * @return the field value if exist
     */
    public static Object getStaticFieldValue(String className, String fieldName) {
    	try {
	        Class<?> cls = Class.forName(className);
	        Field field = cls.getField(fieldName);
	        Object provalue = field.get(cls);
	        return provalue;
    	} catch (Exception ex) {
			//handleReflectionException(ex);
			return null;
		}
    }
    
    /**
     * Get method
     * @param clsName  the class to introspect
     * @param methodName the name of method
     * @param types the type of parameters 
     * @return the method object if exist
     */
    public static Method getMethod(String clsName, String methodName, Class<?>... types){
    	try {
    		Class<?> cls = Class.forName(clsName);
    		return cls.getMethod(methodName, types);
    	} catch (Exception e) {
			//handleReflectionException(ex);
    		return null;
		}
    }
    
	/**
	 * Invoke Method
	 * @param obj the target object
	 * @param methodName the name of method
	 * @param arguments the value of arguments
	 * @return the result of invoke method
	 */
	public static Object invokeMethod(Object obj, String methodName, Object... arguments){
		Class<?> cls = obj.getClass();
		Method method;
		Object result = null;
		try {
			Class<?>[] parameterTypes = null;
			if(arguments!=null){
				parameterTypes = new Class<?>[arguments.length];
				for(int i=0; i<arguments.length; i++){
					parameterTypes[i] = arguments[i].getClass();
				}
			}
			method = cls.getMethod(methodName, parameterTypes);
			result = method.invoke(obj, arguments);
		} catch (Exception ex) {
			ex.printStackTrace();
			//handleReflectionException(ex);
		}
		return result;
	}
	public static Object invokeMethod(Object obj, String methodName, Class<?>[] types, Object... arguments){
		Class<?> cls = obj.getClass();
		Method method;
		Object result = null;
		try {
			method = cls.getMethod(methodName, types);
			result = method.invoke(obj, arguments);
		} catch (Exception ex) {
    		//logger.error("Invoke method error. " + ex.getMessage());
			//handleReflectionException(ex);
		}
		return result;
	}
	public static Object invokeMethod(Class<?> cls, String methodName, Object... arguments) {
		try {
			Object obj = cls.newInstance();
			return invokeMethod(obj, methodName, arguments);
		} catch (Exception ex) {
			//handleReflectionException(ex);
			return null;
		}
	}
	public static Object invokeMethod(String className, String methodName, Object... arguments) {
		try {
			Class<?> cls = Class.forName(className);
			return invokeMethod(cls.newInstance(), methodName, arguments);
		} catch (Exception ex) {
			//handleReflectionException(ex);
			return null;
		}
	}
	
	/**
	 * Invoke static method
	 * @param cls the target class
	 * @param methodName the name of method
	 * @param arguments the value of arguments
	 * @return the result of invoke method
	 */
	public static Object invokeStaticMethod(Class<?> cls, String methodName, Object... arguments) {
		try {
			Class<?>[] parameterTypes = null;
			if(arguments!=null){
				parameterTypes = new Class<?>[arguments.length];
				for(int i=0; i<arguments.length; i++){
					parameterTypes[i] = arguments[i].getClass();
				}
			}
			Method method = cls.getMethod(methodName, parameterTypes);
			return method.invoke(null, arguments);
		}catch (Exception ex) {
			//handleReflectionException(ex);
			return null;
		}
	}
	public static Object invokeStaticMethod(String className, String methodName, Object... arguments) {
		try {
			Class<?> cls = Class.forName(className);
			return invokeStaticMethod(cls, methodName, arguments);
		}catch (Exception ex) {
			ex.printStackTrace();
			//handleReflectionException(ex);
			return null;
		}
	}
	
	public static Object invokeStaticMethod(String className, String methodName, Class<?>[] types , Object... arguments) {
		try {
			Class<?> cls = Class.forName(className);
			return invokeStaticMethod(cls, methodName, types, arguments);
		}catch (Exception ex) {
			//handleReflectionException(ex);
			return null;
		}
	}
	public static Object invokeStaticMethod(Class<?> cls, String methodName, Class<?>[] types, Object... arguments) {
		try {
			Method method = cls.getMethod(methodName, types);
			return method.invoke(null, arguments);
		}catch (Exception ex) {
			//handleReflectionException(ex);
			return null;
		}
	}
	
	public static void setField(Object obj, String fieldName, Object value){
		Class<?> cls = obj.getClass();
		Field field;
		try {
			field = cls.getField(fieldName);
			field.set(obj, value);
		} catch (Exception ex) {
			ex.printStackTrace();
    		//logger.error("Invoke method error. " + ex.getMessage());
			//handleReflectionException(ex);
		}
	}

}
