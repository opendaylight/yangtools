package org.opendaylight.yangtools.sal.java.api.generator

import java.util.LinkedHashSet
import java.util.List
import java.util.Map
import java.util.Set
import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl
import org.opendaylight.yangtools.binding.generator.util.Types
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTOBuilderImpl
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature
import org.opendaylight.yangtools.sal.binding.model.api.Type
import org.opendaylight.yangtools.yang.binding.Augmentable

/**
 * Template for generating JAVA builder classes. 
 */
class BuilderTemplate {

	/**
	 * Constant with prefix for getter methods.
	 */
    val static GET_PREFIX = "get"
    
    /**
     * Constant with the name of the concrete package prefix. 
     */
    val static JAVA_UTIL = "java.util"
    
    /**
     * Constant with the name of the concrete JAVA type
     */
    val static HASH_MAP = "HashMap"
    
    /**
     * Constant with the name of the concrete JAVA interface.
     */
    val static MAP = "Map"
    
    /**
     * Constant with the name of the concrete method.
     */
    val static GET_AUGMENTATION_METHOD_NAME = "getAugmentation"
    
    /**
     * Constant with the suffix for builder classes.
     */
    val static BUILDER = 'Builder'
    
    /**
     * Constant with suffix for the classes which are generated from the builder classes.
     */
    val static IMPL = 'Impl'
    
    /**
     * Reference to type for which is generated builder class
     */
    val GeneratedType genType
    
    /**
     * Map of imports. The keys are type names and the values are package names.
     */
    val Map<String, String> imports
    
    /**
     * Generated property is set if among methods is found one with the name GET_AUGMENTATION_METHOD_NAME
     */
    var GeneratedProperty augmentField
    
    /**
     * Set of class attributes (fields) which are derived from the getter methods names
     */
    val Set<GeneratedProperty> fields
    
    /**
     * Constructs new instance of this class.
     * @throws IllegalArgumentException if <code>genType</code> equals <code>null</code>
     */
    new(GeneratedType genType) {
        if (genType == null) {
            throw new IllegalArgumentException("Generated type reference cannot be NULL!")
        }
        
        this.genType = genType
        this.imports = GeneratorUtil.createChildImports(genType)
        this.fields = createFieldsFromMethods(createMethods)
    }
    
    /**
     * Returns set of method signature instances which contains all the methods of the <code>genType</code>
     * and all the methods of the implemented interfaces.
     * 
     * @returns set of method signature instances
     */
    def private Set<MethodSignature> createMethods() {
        val Set<MethodSignature> methods = new LinkedHashSet
        methods.addAll(genType.methodDefinitions)
        storeMethodsOfImplementedIfcs(methods, genType.implements)
        return methods
    }
    
    /**
     * Adds to the <code>methods</code> set all the methods of the <code>implementedIfcs</code> 
     * and recursivelly their implemented interfaces.
     * 
     * @param methods set of method signatures
     * @param implementedIfcs list of implemented interfaces
     */
    def private void storeMethodsOfImplementedIfcs(Set<MethodSignature> methods, List<Type> implementedIfcs) {
        if (implementedIfcs == null || implementedIfcs.empty) {
            return
        }
        for (implementedIfc : implementedIfcs) {
            if ((implementedIfc instanceof GeneratedType && !(implementedIfc instanceof GeneratedTransferObject))) {
                val ifc = implementedIfc as GeneratedType
                methods.addAll(ifc.methodDefinitions)
                storeMethodsOfImplementedIfcs(methods, ifc.implements)
            } else if (implementedIfc.fullyQualifiedName == Augmentable.name) {
                for (m : Augmentable.methods) {
                    if (m.name == GET_AUGMENTATION_METHOD_NAME) {
                        addToImports(JAVA_UTIL, HASH_MAP)
                        addToImports(JAVA_UTIL, MAP)
                        val fullyQualifiedName = m.returnType.name
                        val pkg = fullyQualifiedName.package
                        val name = fullyQualifiedName.name
                        addToImports(pkg, name)
                        val tmpGenTO = new GeneratedTOBuilderImpl(pkg, name)
                        val type = new ReferencedTypeImpl(pkg, name)
                        val generic = new ReferencedTypeImpl(genType.packageName, genType.name)
                        val parametrizedReturnType = Types.parameterizedTypeFor(type, generic)
                        tmpGenTO.addMethod(m.name).setReturnType(parametrizedReturnType)
                        augmentField = tmpGenTO.toInstance.methodDefinitions.first.createFieldFromGetter
                    }
                }
            }
        }
    }
    
    /**
     * Adds to the <code>imports</code> map the package <code>typePackageName</code>.
     * 
     * @param typePackageName 
     * string with the name of the package which is added to <code>imports</code> as a value
     * @param typeName 
     * string with the name of the package which is added to <code>imports</code> as a key
     */
    def private void addToImports(String typePackageName,String typeName) {
        if (typePackageName.startsWith("java.lang") || typePackageName.isEmpty()) {
            return
        }
        if (!imports.containsKey(typeName)) {
            imports.put(typeName, typePackageName)
        }
    }
    
    /**
     * Returns the first element of the list <code>elements</code>.
     * 
     * @param list of elements
     */
    def private <E> first(List<E> elements) {
        elements.get(0)
    }
    
    /**
     * Returns the name of the package from <code>fullyQualifiedName</code>.
     * 
     * @param fullyQualifiedName string with fully qualified type name (package + type)
     * @return string with the package name
     */
    def private String getPackage(String fullyQualifiedName) {
        val lastDotIndex = fullyQualifiedName.lastIndexOf(Constants.DOT)
        return if (lastDotIndex == -1) "" else fullyQualifiedName.substring(0, lastDotIndex)
    }

	/**
	 * Returns the name of tye type from <code>fullyQualifiedName</code>
	 * 
	 * @param fullyQualifiedName string with fully qualified type name (package + type)
	 * @return string with the name of the type
	 */
    def private String getName(String fullyQualifiedName) {
        val lastDotIndex = fullyQualifiedName.lastIndexOf(Constants.DOT)
        return if (lastDotIndex == -1) fullyQualifiedName else fullyQualifiedName.substring(lastDotIndex + 1)
    }
    
    /**
     * Creates set of generated property instances from getter <code>methods</code>.
     * 
     * @param set of method signature instances which should be transformed to list of properties 
     * @return set of generated property instances which represents the getter <code>methods</code>
     */
    def private createFieldsFromMethods(Set<MethodSignature> methods) {
        val Set<GeneratedProperty> result = new LinkedHashSet

        if (methods == null || methods.isEmpty()) {
            return result
        }

        for (m : methods) {
            val createdField = m.createFieldFromGetter
            if (createdField != null) {
                result.add(createdField)
            }
        }
        return result
    }
    
    /**
     * Creates generated property instance from the getter <code>method</code> name and return type.
     * 
     * @param method method signature from which is the method name and return type obtained
     * @return generated property instance for the getter <code>method</code>
     * @throws IllegalArgumentException<ul>
     * 	<li>if the <code>method</code> equals <code>null</code></li>
     * 	<li>if the name of the <code>method</code> equals <code>null</code></li>
     * 	<li>if the name of the <code>method</code> is empty</li>
     * 	<li>if the return type of the <code>method</code> equals <code>null</code></li>
     * </ul>
     */
    def private GeneratedProperty createFieldFromGetter(MethodSignature method) {
        if (method == null || method.name == null || method.name.empty || method.returnType == null) {
            throw new IllegalArgumentException("Method, method name, method return type reference cannot be NULL or empty!")
        }
        if (method.name.startsWith(GET_PREFIX)) {
            val fieldName = method.getName().substring(GET_PREFIX.length()).toFirstLower
            val tmpGenTO = new GeneratedTOBuilderImpl("foo", "foo")
            tmpGenTO.addProperty(fieldName).setReturnType(method.returnType)
            return tmpGenTO.toInstance.properties.first
        }
    }

	/**
	 * Builds string which contains JAVA source code.
	 * 
	 * @return string with JAVA source code
	 */
    def String generate() {
        val body = generateBody
        val pkgAndImports = generatePkgAndImports
        return pkgAndImports.toString + body.toString
    }
    
    /**
     * Template method which generates JAVA class body for builder class and for IMPL class. 
     * 
     * @return string with JAVA source code
     */
    def private generateBody() '''
        public class «genType.name»«BUILDER» {
        
            «generateFields(false)»

            «generateSetters»

            public «genType.name» build() {
                return new «genType.name»«IMPL»();
            }

            private class «genType.name»«IMPL» implements «genType.name» {

                «generateFields(true)»

                «generateConstructor»

                «generateGetters»

            }

        }
    '''

	/**
	 * Template method which generates class attributes.
	 * 
	 * @param boolean value which specify whether field is|isn't final
	 * @return string with class attributes and their types
	 */
    def private generateFields(boolean _final) '''
        «IF !fields.empty»
            «FOR f : fields»
                private  «IF _final»final«ENDIF»  «f.returnType.resolveName» «f.name»;
            «ENDFOR»
        «ENDIF»
        «IF augmentField != null»
            private Map<Class<? extends «augmentField.returnType.resolveName»>, «augmentField.returnType.resolveName»> «augmentField.name» = new HashMap<>();
        «ENDIF»
    '''

	/**
	 * Template method which generates setter methods
	 * 
	 * @return string with the setter methods 
	 */
    def private generateSetters() '''
        «FOR field : fields SEPARATOR '\n'»
            public «genType.name»«BUILDER» set«field.name.toFirstUpper»(«field.returnType.resolveName» «field.name») {
                this.«field.name» = «field.name»;
                return this;
            }
        «ENDFOR»
        «IF augmentField != null»
            
            public «genType.name»«BUILDER» add«augmentField.name.toFirstUpper»(Class<? extends «augmentField.returnType.resolveName»> augmentationType, «augmentField.returnType.resolveName» augmentation) {
                this.«augmentField.name».put(augmentationType, augmentation);
                return this;
            }
        «ENDIF»
    '''
    
    /**
     * Template method which generate constructor for IMPL class.
     * 
     * @return string with IMPL class constructor
     */
    def private generateConstructor() '''
        private «genType.name»«IMPL»() {
            «IF !fields.empty»
                «FOR field : fields»
                    this.«field.name» = «genType.name»«BUILDER».this.«field.name»;
                «ENDFOR»
            «ENDIF»
            «IF augmentField != null»
                this.«augmentField.name».putAll(«genType.name»«BUILDER».this.«augmentField.name»);
            «ENDIF»
        }
    '''
    
    /**
     * Template method which generate getter methods for IMPL class.
     * 
     * @return string with getter methods
     */
    def private generateGetters() '''
        «IF !fields.empty»
            «FOR field : fields SEPARATOR '\n'»
                @Override
                public «field.returnType.resolveName» get«field.name.toFirstUpper»() {
                    return «field.name»;
                }
            «ENDFOR»
        «ENDIF»
        «IF augmentField != null»

            @SuppressWarnings("unchecked")
            @Override
            public <E extends «augmentField.returnType.resolveName»> E get«augmentField.name.toFirstUpper»(Class<E> augmentationType) {
                if (augmentationType == null) {
                    throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
                }
                return (E) «augmentField.name».get(augmentationType);
            }
        «ENDIF»
    '''    
    
    /**
     * Template method which generate package name line and import lines.
     * 
     * @result string with package and import lines in JAVA format
     */
    def private generatePkgAndImports() '''
        package «genType.packageName»;
        
        
        «IF !imports.empty»
            «FOR entry : imports.entrySet»
                import «entry.value».«entry.key»;
            «ENDFOR»
        «ENDIF»
        
    '''
    
    /**
     * Adds package to imports if it is necessary and returns necessary type name (with or without package name)
     * 
     * @param type JAVA <code>Type</code>
     * @return string with the type name (with or without package name)
     */
    def private resolveName(Type type) {
        GeneratorUtil.putTypeIntoImports(genType, type, imports);
        GeneratorUtil.getExplicitType(genType, type, imports)
    }
    
}

