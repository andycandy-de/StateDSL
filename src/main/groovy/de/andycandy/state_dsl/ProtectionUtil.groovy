package de.andycandy.state_dsl

class ProtectionUtil {

	private static <T> Closure createDelegateMethodClosure(java.lang.reflect.Method method, T t) {
		return { Object[] args -> t."$method.name"(*args) }
	}

	static <T> T protectObject(Class<T> interfaceClass, T toProtect) {

		Map<String, Closure> map = [:]
		
		interfaceClass.methods.each { map."$it.name" = createDelegateMethodClosure(it, toProtect) }
		Object.class.methods.each { map."$it.name" = createDelegateMethodClosure(it, toProtect) }
		
		return map.asType(interfaceClass)
	}
}