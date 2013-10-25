package org.opendaylight.yangtools.sal.java.api.generator

import java.util.Arrays;
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
import static org.opendaylight.yangtools.binding.generator.util.Types.*
import java.util.HashMap
import java.util.Collections

/**
 * Template for generating JAVA builder classes. 
 */

class BuilderTemplate extends BaseTemplate {
    
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
     * Generated property is set if among methods is found one with the name GET_AUGMENTATION_METHOD_NAME
     */
    var GeneratedProperty augmentField
    
    /**
     * Set of class attributes (fields) which are derived from the getter methods names
     */
    val Set<GeneratedProperty> properties
    
    /**
     * Constructs new instance of this class.
     * @throws IllegalArgumentException if <code>genType</code> equals <code>null</code>
     */
    new(GeneratedType genType) {
        super(genType)
        this.properties = propertiesFromMethods(createMethods)
    }
    
    /**
     * Returns set of method signature instances which contains all the methods of the <code>genType</code>
     * and all the methods of the implemented interfaces.
     * 
     * @returns set of method signature instances
     */
    def private Set<MethodSignature> createMethods() {
        val Set<MethodSignature> methods = new LinkedHashSet
        methods.addAll(type.methodDefinitions)
        collectImplementedMethods(methods, type.implements)
        return methods
    }
    

    /**
     * Adds to the <code>methods</code> set all the methods of the <code>implementedIfcs</code> 
     * and recursivelly their implemented interfaces.
     * 
     * @param methods set of method signatures
     * @param implementedIfcs list of implemented interfaces
     */
    def private void collectImplementedMethods(Set<MethodSignature> methods, List<Type> implementedIfcs) {
        if (implementedIfcs == null || implementedIfcs.empty) {
            return
        }
        for (implementedIfc : implementedIfcs) {
            if ((implementedIfc instanceof GeneratedType && !(implementedIfc instanceof GeneratedTransferObject))) {
                val ifc = implementedIfc as GeneratedType
                methods.addAll(ifc.methodDefinitions)
                collectImplementedMethods(methods, ifc.implements)
            } else if (implementedIfc.fullyQualifiedName == Augmentable.name) {
                for (m : Augmentable.methods) {
                    if (m.name == GET_AUGMENTATION_METHOD_NAME) {
                        //addToImports(JAVA_UTIL, HASH_MAP)
                        //addToImports(JAVA_UTIL, MAP)
                        val fullyQualifiedName = m.returnType.name
                        val pkg = fullyQualifiedName.package
                        val name = fullyQualifiedName.name
                        //addToImports(pkg, name)
                        val tmpGenTO = new GeneratedTOBuilderImpl(pkg, name)
                        val refType = new ReferencedTypeImpl(pkg, name)
                        val generic = new ReferencedTypeImpl(type.packageName, type.name)
                        val parametrizedReturnType = Types.parameterizedTypeFor(refType, generic)
                        tmpGenTO.addMethod(m.name).setReturnType(parametrizedReturnType)
                        augmentField = tmpGenTO.toInstance.methodDefinitions.first.propertyFromGetter
                    }
                }
            }
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
    def private propertiesFromMethods(Set<MethodSignature> methods) {
        

        if (methods == null || methods.isEmpty()) {
            return Collections.emptySet
        }
        val Set<GeneratedProperty> result = new LinkedHashSet
        for (m : methods) {
            val createdField = m.propertyFromGetter
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
    def private GeneratedProperty propertyFromGetter(MethodSignature method) {

        if (method == null || method.name == null || method.name.empty || method.returnType == null) {
            throw new IllegalArgumentException("Method, method name, method return type reference cannot be NULL or empty!")
        }
        var prefix = "get";
        if(BOOLEAN.equals(method.returnType)) {
            prefix = "is";
        } 
        if (method.name.startsWith(prefix)) {
            val fieldName = method.getName().substring(prefix.length()).toFirstLower
            val tmpGenTO = new GeneratedTOBuilderImpl("foo", "foo")
            tmpGenTO.addProperty(fieldName).setReturnType(method.returnType)
            return tmpGenTO.toInstance.properties.first
        }
    }

    /**
     * Template method which generates JAVA class body for builder class and for IMPL class. 
     * 
     * @return string with JAVA source code
     */
    override body() '''

        public class «type.name»«BUILDER» {

            «generateFields(false)»

            «generateBuilderConstructor(type)»

            «generateGetters(false)»

            «generateSetters»

            public «type.name» build() {
                return new «type.name»«IMPL»(this);
            }

            private static final class «type.name»«IMPL» implements «type.name» {

                «implementedInterfaceGetter»

                «generateFields(true)»

                «generateConstructor»

                «generateGetters(true)»

                «generateHashCode()»

                «generateEquals()»
            }

        }
    '''


    def private generateBuilderConstructor(Type implementedIfc) '''
        public «type.name»«BUILDER»() {
        }

        «IF (implementedIfc instanceof GeneratedType && !(implementedIfc instanceof GeneratedTransferObject))»
        «val ifc = implementedIfc as GeneratedType»
        «FOR impl : ifc.implements»
            «IF (impl instanceof GeneratedType) &&  !((impl as GeneratedType).methodDefinitions.empty)»
                public «type.name»«BUILDER»(«impl.fullyQualifiedName» arg) {
                    «printBuilderConstructorProperties(impl)»
                }
            «ENDIF»
        «ENDFOR»
        «ENDIF»
    '''

    def private printBuilderConstructorProperties(Type implementedIfc) '''
        «IF (implementedIfc instanceof GeneratedType && !(implementedIfc instanceof GeneratedTransferObject))»
        «val ifc = implementedIfc as GeneratedType»
        «FOR getter : ifc.methodDefinitions»
            this._«getter.propertyNameFromGetter» = arg.«getter.name»();
        «ENDFOR»
        «FOR impl : ifc.implements»
        «printBuilderConstructorProperties(impl)»
        «ENDFOR»
        «ENDIF»
    '''

	/**
	 * Template method which generates class attributes.
	 * 
	 * @param boolean value which specify whether field is|isn't final
	 * @return string with class attributes and their types
	 */
    def private generateFields(boolean _final) '''
        «IF !properties.empty»
            «FOR f : properties»
                private  «IF _final»final«ENDIF»  «f.returnType.importedName» «f.fieldName»;
            «ENDFOR»
        «ENDIF»
        «IF augmentField != null»
            private «Map.importedName»<Class<? extends «augmentField.returnType.importedName»>, «augmentField.returnType.importedName»> «augmentField.name» = new «HashMap.importedName»<>();
        «ENDIF»
    '''

	/**
	 * Template method which generates setter methods
	 * 
	 * @return string with the setter methods 
	 */
    def private generateSetters() '''
        «FOR field : properties SEPARATOR '\n'»
            public «type.name»«BUILDER» set«field.name.toFirstUpper»(«field.returnType.importedName» value) {
                this.«field.fieldName» = value;
                return this;
            }
        «ENDFOR»
        «IF augmentField != null»
            
            public «type.name»«BUILDER» add«augmentField.name.toFirstUpper»(Class<? extends «augmentField.returnType.importedName»> augmentationType, «augmentField.returnType.importedName» augmentation) {
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
        private «type.name»«IMPL»(«type.name»«BUILDER» builder) {
            «IF !properties.empty»
                «FOR field : properties»
                    this.«field.fieldName» = builder.«field.getterMethodName»();
                «ENDFOR»
            «ENDIF»
            «IF augmentField != null»
                this.«augmentField.name».putAll(builder.«augmentField.name»);
            «ENDIF»
        }
    '''
    

    /**
     * Template method which generate getter methods for IMPL class.
     * 
     * @return string with getter methods
     */
    def private generateGetters(boolean addOverride) '''
        «IF !properties.empty»
            «FOR field : properties SEPARATOR '\n'»
                «IF addOverride»@Override«ENDIF»
                «field.getterMethod»
            «ENDFOR»
        «ENDIF»
        «IF augmentField != null»

            @SuppressWarnings("unchecked")
            «IF addOverride»@Override«ENDIF»
            public <E extends «augmentField.returnType.importedName»> E get«augmentField.name.toFirstUpper»(Class<E> augmentationType) {
                if (augmentationType == null) {
                    throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
                }
                return (E) «augmentField.name».get(augmentationType);
            }
        «ENDIF»
    '''

    /**
     * Template method which generates the method <code>hashCode()</code>.
     * 
     * @return string with the <code>hashCode()</code> method definition in JAVA format
     */
    def protected generateHashCode() '''
        «IF !properties.empty || augmentField != null»
            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                «FOR property : properties»
                    «IF property.returnType.name.contains("[")»
                    result = prime * result + ((«property.fieldName» == null) ? 0 : «Arrays.importedName».hashCode(«property.fieldName»));
                    «ELSE»
                    result = prime * result + ((«property.fieldName» == null) ? 0 : «property.fieldName».hashCode());
                    «ENDIF»
                «ENDFOR»
                «IF augmentField != null»
                    result = prime * result + ((«augmentField.name» == null) ? 0 : «augmentField.name».hashCode());
                «ENDIF»
                return result;
            }
        «ENDIF»
    '''

    /**
     * Template method which generates the method <code>equals()</code>.
     * 
     * @return string with the <code>equals()</code> method definition in JAVA format     
     */
    def protected generateEquals() '''
        «IF !properties.empty || augmentField != null»
            @Override
            public boolean equals(java.lang.Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                «type.name»«IMPL» other = («type.name»«IMPL») obj;
                «FOR property : properties»
                    «val fieldName = property.fieldName»
                    if («fieldName» == null) {
                        if (other.«fieldName» != null) {
                            return false;
                        }
                    «IF property.returnType.name.contains("[")»
                    } else if(!«Arrays.importedName».equals(«fieldName», other.«fieldName»)) {
                    «ELSE»
                    } else if(!«fieldName».equals(other.«fieldName»)) {
                    «ENDIF»
                        return false;
                    }
                «ENDFOR»
                «IF augmentField != null»
                    «val fieldName = augmentField.name»
                    if («fieldName» == null) {
                        if (other.«fieldName» != null) {
                            return false;
                        }
                    } else if(!«fieldName».equals(other.«fieldName»)) {
                        return false;
                    }
                «ENDIF»
                return true;
            }
        «ENDIF»
    '''

    override protected getFullyQualifiedName() {
        '''«type.fullyQualifiedName»Builder'''.toString
    }
    
    def implementedInterfaceGetter() '''
    public «Class.importedName»<«type.importedName»> getImplementedInterface() {
        return «type.importedName».class;
    }
    '''
    
}

