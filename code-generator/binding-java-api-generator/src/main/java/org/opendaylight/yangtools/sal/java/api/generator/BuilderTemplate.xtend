/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator

import com.google.common.collect.ImmutableSortedSet
import java.util.ArrayList
import java.util.Arrays
import java.util.Collection
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashSet
import java.util.List
import java.util.Map
import java.util.Set
import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl
import org.opendaylight.yangtools.binding.generator.util.Types
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTOBuilderImpl
import org.opendaylight.yangtools.concepts.Builder
import org.opendaylight.yangtools.sal.binding.model.api.ConcreteType
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature
import org.opendaylight.yangtools.sal.binding.model.api.Type
import org.opendaylight.yangtools.yang.binding.Augmentable
import org.opendaylight.yangtools.yang.binding.AugmentationHolder
import org.opendaylight.yangtools.yang.binding.DataObject
import org.opendaylight.yangtools.yang.binding.Identifiable

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
     * Constant with the name of the BuilderFor interface
     */
     val static BUILDERFOR = Builder.simpleName;

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

    private static val METHOD_COMPARATOR = new AlphabeticallyTypeMemberComparator<MethodSignature>();

    /**
     * Constructs new instance of this class.
     * @throws IllegalArgumentException if <code>genType</code> equals <code>null</code>
     */
    new(GeneratedType genType) {
        super(genType)
        this.properties = propertiesFromMethods(createMethods)
        importMap.put(Builder.simpleName, Builder.package.name)
    }

    /**
     * Returns set of method signature instances which contains all the methods of the <code>genType</code>
     * and all the methods of the implemented interfaces.
     *
     * @returns set of method signature instances
     */
    def private Set<MethodSignature> createMethods() {
        val Set<MethodSignature> methods = new LinkedHashSet();
        methods.addAll(type.methodDefinitions)
        collectImplementedMethods(methods, type.implements)
        val Set<MethodSignature> sortedMethods = ImmutableSortedSet.orderedBy(METHOD_COMPARATOR).addAll(methods).build()

        return sortedMethods
    }

    /**
     * Adds to the <code>methods</code> set all the methods of the <code>implementedIfcs</code>
     * and recursively their implemented interfaces.
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
                        val fullyQualifiedName = m.returnType.name
                        val pkg = fullyQualifiedName.package
                        val name = fullyQualifiedName.name
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
    def private propertiesFromMethods(Collection<MethodSignature> methods) {
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
     *  <li>if the <code>method</code> equals <code>null</code></li>
     *  <li>if the name of the <code>method</code> equals <code>null</code></li>
     *  <li>if the name of the <code>method</code> is empty</li>
     *  <li>if the return type of the <code>method</code> equals <code>null</code></li>
     * </ul>
     */
    def private GeneratedProperty propertyFromGetter(MethodSignature method) {
        if (method == null || method.name == null || method.name.empty || method.returnType == null) {
            throw new IllegalArgumentException("Method, method name, method return type reference cannot be NULL or empty!")
        }
        var prefix = "get";
        if(Types.BOOLEAN.equals(method.returnType)) {
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
        «wrapToDocumentation(formatDataForJavaDoc(type))»
        public class «type.name»«BUILDER» implements «BUILDERFOR» <«type.importedName»> {

            «generateFields(false)»

            «generateAugmentField(false)»

            «generateConstructorsFromIfcs(type)»

            «generateCopyConstructor(false)»

            «generateMethodFieldsFrom(type)»

            «generateGetters(false)»

            «generateSetters»

            public «type.name» build() {
                return new «type.name»«IMPL»(this);
            }

            private static final class «type.name»«IMPL» implements «type.name» {

                «implementedInterfaceGetter»

                «generateFields(true)»

                «generateAugmentField(true)»

                «generateCopyConstructor(true)»

                «generateGetters(true)»

                «generateHashCode()»

                «generateEquals()»

                «generateToString(properties)»
            }

        }
    '''

    /**
     * Generate default constructor and constructor for every implemented interface from uses statements.
     */
    def private generateConstructorsFromIfcs(Type type) '''
        public «type.name»«BUILDER»() {
        }
        «IF (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject))»
            «val ifc = type as GeneratedType»
            «FOR impl : ifc.implements»
                «generateConstructorFromIfc(impl)»
            «ENDFOR»
        «ENDIF»
    '''

    /**
     * Generate constructor with argument of given type.
     */
    def private Object generateConstructorFromIfc(Type impl) '''
        «IF (impl instanceof GeneratedType)»
            «IF !(impl.methodDefinitions.empty)»
                public «type.name»«BUILDER»(«impl.fullyQualifiedName» arg) {
                    «printConstructorPropertySetter(impl)»
                }
            «ENDIF»
            «FOR implTypeImplement : impl.implements»
                «generateConstructorFromIfc(implTypeImplement)»
            «ENDFOR»
        «ENDIF»
    '''

    def private Object printConstructorPropertySetter(Type implementedIfc) '''
        «IF (implementedIfc instanceof GeneratedType && !(implementedIfc instanceof GeneratedTransferObject))»
            «val ifc = implementedIfc as GeneratedType»
            «FOR getter : ifc.methodDefinitions»
                this._«getter.propertyNameFromGetter» = arg.«getter.name»();
            «ENDFOR»
            «FOR impl : ifc.implements»
                «printConstructorPropertySetter(impl)»
            «ENDFOR»
        «ENDIF»
    '''

    /**
     * Generate 'fieldsFrom' method to set builder properties based on type of given argument.
     */
    def private generateMethodFieldsFrom(Type type) '''
        «IF (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject))»
            «val ifc = type as GeneratedType»
            «IF ifc.hasImplementsFromUses»
                «val List<Type> done = ifc.getBaseIfcs»
                «generateMethodFieldsFromComment(ifc)»
                public void fieldsFrom(«DataObject.importedName» arg) {
                    boolean isValidArg = false;
                    «FOR impl : ifc.getAllIfcs»
                        «generateIfCheck(impl, done)»
                    «ENDFOR»
                    if (!isValidArg) {
                        throw new IllegalArgumentException(
                          "expected one of: «ifc.getAllIfcs.toListOfNames» \n" +
                          "but was: " + arg
                        );
                    }
                }
            «ENDIF»
        «ENDIF»
    '''

    def private generateMethodFieldsFromComment(GeneratedType type) '''
        /**
         *Set fields from given grouping argument. Valid argument is instance of one of following types:
         * <ul>
         «FOR impl : type.getAllIfcs»
         * <li>«impl.fullyQualifiedName»</li>
         «ENDFOR»
         * </ul>
         *
         * @param arg grouping object
         * @throws IllegalArgumentException if given argument is none of valid types
        */
    '''

    /**
     * Method is used to find out if given type implements any interface from uses.
     */
    def boolean hasImplementsFromUses(GeneratedType type) {
        var i = 0
        for (impl : type.getAllIfcs) {
            if ((impl instanceof GeneratedType) &&  !((impl as GeneratedType).methodDefinitions.empty)) {
                i = i + 1
            }
        }
        return i > 0
    }

    def private generateIfCheck(Type impl, List<Type> done) '''
        «IF (impl instanceof GeneratedType) &&  !((impl as GeneratedType).methodDefinitions.empty)»
            «val implType = impl as GeneratedType»
            if (arg instanceof «implType.fullyQualifiedName») {
                «printPropertySetter(implType)»
                isValidArg = true;
            }
        «ENDIF»
    '''

    def private printPropertySetter(Type implementedIfc) '''
        «IF (implementedIfc instanceof GeneratedType && !(implementedIfc instanceof GeneratedTransferObject))»
        «val ifc = implementedIfc as GeneratedType»
        «FOR getter : ifc.methodDefinitions»
            this._«getter.propertyNameFromGetter» = ((«implementedIfc.fullyQualifiedName»)arg).«getter.name»();
        «ENDFOR»
        «ENDIF»
    '''

    private def List<Type> getBaseIfcs(GeneratedType type) {
        val List<Type> baseIfcs = new ArrayList();
        for (ifc : type.implements) {
            if (ifc instanceof GeneratedType && !(ifc as GeneratedType).methodDefinitions.empty) {
                baseIfcs.add(ifc)
            }
        }
        return baseIfcs
    }

    private def Set<Type> getAllIfcs(Type type) {
        val Set<Type> baseIfcs = new HashSet()
        if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
            val ifc = type as GeneratedType
            for (impl : ifc.implements) {
                if (impl instanceof GeneratedType && !(impl as GeneratedType).methodDefinitions.empty) {
                    baseIfcs.add(impl)
                }
                baseIfcs.addAll(impl.getAllIfcs)
            }
        }
        return baseIfcs
    }

    private def List<String> toListOfNames(Collection<Type> types) {
        val List<String> names = new ArrayList
        for (type : types) {
            names.add(type.fullyQualifiedName)
        }
        return names
    }

    /**
     * Template method which generates class attributes.
     *
     * @param boolean value which specify whether field is|isn't final
     * @return string with class attributes and their types
     */
    def private generateFields(boolean _final) '''
        «IF properties !== null»
            «FOR f : properties»
                private«IF _final» final«ENDIF» «f.returnType.importedName» «f.fieldName»;
                «val restrictions = f.returnType.restrictions»
                «IF !_final && restrictions != null && !(restrictions.lengthConstraints.empty)»
                    «LengthGenerator.generateLengthChecker(f.fieldName.toString, f.returnType, restrictions.lengthConstraints)»
                «ENDIF»
            «ENDFOR»
        «ENDIF»
    '''

    def private generateAugmentField(boolean isPrivate) '''
        «IF augmentField != null»
            «IF isPrivate»private «ENDIF»«Map.importedName»<«Class.importedName»<? extends «augmentField.returnType.importedName»>, «augmentField.returnType.importedName»> «augmentField.name» = «Collections.importedName».emptyMap();
        «ENDIF»
    '''

    /**
     * Template method which generates setter methods
     *
     * @return string with the setter methods
     */
    def private generateSetters() '''
        «FOR field : properties SEPARATOR '\n'»
            «val restrictions = field.returnType.restrictions»
            «IF restrictions != null»
                «IF !restrictions.rangeConstraints.nullOrEmpty»
                    «val rangeGenerator = AbstractRangeGenerator.forType(field.returnType)»
                    «rangeGenerator.generateRangeChecker(field.name.toFirstUpper, restrictions.rangeConstraints)»

                «ENDIF»
            «ENDIF»
            public «type.name»«BUILDER» set«field.name.toFirstUpper»(«field.returnType.importedName» value) {
                «IF restrictions != null»
                if (value != null) {
                    «IF !restrictions.rangeConstraints.nullOrEmpty»
                        «val rangeGenerator = AbstractRangeGenerator.forType(field.returnType)»
                        «IF field.returnType instanceof ConcreteType»
                            «rangeGenerator.generateRangeCheckerCall(field.name.toFirstUpper, "value")»
                        «ELSE»
                            «rangeGenerator.generateRangeCheckerCall(field.name.toFirstUpper, "value.getValue()")»
                        «ENDIF»
                    «ENDIF»
                    «generateRestrictions(field, "value")»
                }
                «ENDIF»
                this.«field.fieldName» = value;
                return this;
            }
        «ENDFOR»
        «IF augmentField != null»

            public «type.name»«BUILDER» add«augmentField.name.toFirstUpper»(«Class.importedName»<? extends «augmentField.returnType.importedName»> augmentationType, «augmentField.returnType.importedName» augmentation) {
                if (augmentation == null) {
                    return remove«augmentField.name.toFirstUpper»(augmentationType);
                }

                if (!(this.«augmentField.name» instanceof «HashMap.importedName»)) {
                    this.«augmentField.name» = new «HashMap.importedName»<>();
                }

                this.«augmentField.name».put(augmentationType, augmentation);
                return this;
            }

            public «type.name»«BUILDER» remove«augmentField.name.toFirstUpper»(«Class.importedName»<? extends «augmentField.returnType.importedName»> augmentationType) {
                if (this.«augmentField.name» instanceof «HashMap.importedName») {
                    this.«augmentField.name».remove(augmentationType);
                }
                return this;
            }
        «ENDIF»
    '''

    def private generateRestrictions(GeneratedProperty field, String paramName) '''
        «val Type type = field.returnType»
        «val restrictions = type.getRestrictions»
        «IF restrictions !== null && !restrictions.lengthConstraints.empty»
            «IF type instanceof ConcreteType»
                «LengthGenerator.generateLengthCheckerCall(field.fieldName.toString, paramName)»
            «ELSE»
                «LengthGenerator.generateLengthCheckerCall(field.fieldName.toString, paramName + ".getValue()")»
            «ENDIF»
        «ENDIF»
    '''

    def private CharSequence generateCopyConstructor(boolean impl) '''
        «IF impl»private«ELSE»public«ENDIF» «type.name»«IF impl»«IMPL»«ELSE»«BUILDER»«ENDIF»(«type.name»«IF impl»«BUILDER»«ENDIF» base) {
            «val allProps = new ArrayList(properties)»
            «val isList = implementsIfc(type, Types.parameterizedTypeFor(Types.typeForClass(Identifiable), type))»
            «val keyType = type.getKey»
            «IF isList && keyType != null»
                «val keyProps = new ArrayList((keyType as GeneratedTransferObject).properties)»
                «Collections.sort(keyProps,
                    [ p1, p2 |
                        return p1.name.compareTo(p2.name)
                    ])
                »
                «FOR field : keyProps»
                    «removeProperty(allProps, field.name)»
                «ENDFOR»
                «removeProperty(allProps, "key")»
                if (base.getKey() == null) {
                    this._key = new «keyType.importedName»(
                        «FOR keyProp : keyProps SEPARATOR ", "»
                            base.«keyProp.getterMethodName»()
                        «ENDFOR»
                    );
                    «FOR field : keyProps»
                        this.«field.fieldName» = base.«field.getterMethodName»();
                    «ENDFOR»
                } else {
                    this._key = base.getKey();
                    «FOR field : keyProps»
                           this.«field.fieldName» = _key.«field.getterMethodName»();
                    «ENDFOR»
                }
            «ENDIF»
            «FOR field : allProps»
                this.«field.fieldName» = base.«field.getterMethodName»();
            «ENDFOR»
            «IF augmentField != null»
                «IF impl»
                    switch (base.«augmentField.name».size()) {
                    case 0:
                        this.«augmentField.name» = «Collections.importedName».emptyMap();
                        break;
                    case 1:
                        final «Map.importedName».Entry<«Class.importedName»<? extends «augmentField.returnType.importedName»>, «augmentField.returnType.importedName»> e = base.«augmentField.name».entrySet().iterator().next();
                        this.«augmentField.name» = «Collections.importedName».<«Class.importedName»<? extends «augmentField.returnType.importedName»>, «augmentField.returnType.importedName»>singletonMap(e.getKey(), e.getValue());
                        break;
                    default :
                        this.«augmentField.name» = new «HashMap.importedName»<>(base.«augmentField.name»);
                    }
                «ELSE»
                    if (base instanceof «type.name»«IMPL») {
                        «type.name»«IMPL» impl = («type.name»«IMPL») base;
                        if (!impl.«augmentField.name».isEmpty()) {
                            this.«augmentField.name» = new «HashMap.importedName»<>(impl.«augmentField.name»);
                        }
                    } else if (base instanceof «AugmentationHolder.importedName») {
                        @SuppressWarnings("unchecked")
                        «AugmentationHolder.importedName»<«type.importedName»> casted =(«AugmentationHolder.importedName»<«type.importedName»>) base;
                        if (!casted.augmentations().isEmpty()) {
                            this.«augmentField.name» = new «HashMap.importedName»<>(casted.augmentations());
                        }
                    }
                «ENDIF»
            «ENDIF»
        }
    '''

    private def boolean implementsIfc(GeneratedType type, Type impl) {
        for (Type ifc : type.implements) {
            if (ifc.equals(impl)) {
                return true;
            }
        }
        return false;
    }

    private def Type getKey(GeneratedType type) {
        for (m : type.methodDefinitions) {
            if ("getKey".equals(m.name)) {
                return m.returnType;
            }
        }
        return null;
    }

    private def void removeProperty(Collection<GeneratedProperty> props, String name) {
        var GeneratedProperty toRemove = null
        for (p : props) {
            if (p.name.equals(name)) {
                toRemove = p;
            }
        }
        if (toRemove != null) {
            props.remove(toRemove);
        }
    }

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
            public <E extends «augmentField.returnType.importedName»> E get«augmentField.name.toFirstUpper»(«Class.importedName»<E> augmentationType) {
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
            private int hash = 0;
            private volatile boolean hashValid = false;

            @Override
            public int hashCode() {
                if (hashValid) {
                    return hash;
                }

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

                hash = result;
                hashValid = true;
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
            public boolean equals(«Object.importedName» obj) {
                if (this == obj) {
                    return true;
                }
                if (!(obj instanceof «DataObject.importedName»)) {
                    return false;
                }
                if (!«type.importedName».class.equals(((«DataObject.importedName»)obj).getImplementedInterface())) {
                    return false;
                }
                «type.importedName» other = («type.importedName»)obj;
                «FOR property : properties»
                    «val fieldName = property.fieldName»
                    if («fieldName» == null) {
                        if (other.«property.getterMethodName»() != null) {
                            return false;
                        }
                    «IF property.returnType.name.contains("[")»
                    } else if(!«Arrays.importedName».equals(«fieldName», other.«property.getterMethodName»())) {
                    «ELSE»
                    } else if(!«fieldName».equals(other.«property.getterMethodName»())) {
                    «ENDIF»
                        return false;
                    }
                «ENDFOR»
                «IF augmentField != null»
                    if (getClass() == obj.getClass()) {
                        // Simple case: we are comparing against self
                        «type.name»«IMPL» otherImpl = («type.name»«IMPL») obj;
                        «val fieldName = augmentField.name»
                        if («fieldName» == null) {
                            if (otherImpl.«fieldName» != null) {
                                return false;
                            }
                        } else if(!«fieldName».equals(otherImpl.«fieldName»)) {
                            return false;
                        }
                    } else {
                        // Hard case: compare our augments with presence there...
                        for («Map.importedName».Entry<«Class.importedName»<? extends «augmentField.returnType.importedName»>, «augmentField.returnType.importedName»> e : «augmentField.name».entrySet()) {
                            if (!e.getValue().equals(other.getAugmentation(e.getKey()))) {
                                return false;
                            }
                        }
                        // .. and give the other one the chance to do the same
                        if (!obj.equals(this)) {
                            return false;
                        }
                    }
                «ENDIF»
                return true;
            }
        «ENDIF»
    '''

    def override generateToString(Collection<GeneratedProperty> properties) '''
        «IF !(properties === null)»
            @Override
            public «String.importedName» toString() {
                «StringBuilder.importedName» builder = new «StringBuilder.importedName» ("«type.name» [");
                boolean first = true;

                «FOR property : properties»
                    if («property.fieldName» != null) {
                        if (first) {
                            first = false;
                        } else {
                            builder.append(", ");
                        }
                        builder.append("«property.fieldName»=");
                        «IF property.returnType.name.contains("[")»
                            builder.append(«Arrays.importedName».toString(«property.fieldName»));
                        «ELSE»
                            builder.append(«property.fieldName»);
                        «ENDIF»
                     }
                «ENDFOR»
                «IF augmentField != null»
                    if (first) {
                        first = false;
                    } else {
                        builder.append(", ");
                    }
                    builder.append("«augmentField.name»=");
                    builder.append(«augmentField.name».values());
                «ENDIF»
                return builder.append(']').toString();
            }
        «ENDIF»
    '''

    def implementedInterfaceGetter() '''
    public «Class.importedName»<«type.importedName»> getImplementedInterface() {
        return «type.importedName».class;
    }
    '''

    private def createDescription(GeneratedType type) {
        return '''
        Class that builds {@link «type.importedName»} instances.

        @see «type.importedName»
    '''
    }

    override def protected String formatDataForJavaDoc(GeneratedType type) {
        val typeDescription = createDescription(type)

        return '''
            «IF !typeDescription.nullOrEmpty»
            «typeDescription»
            «ENDIF»
        '''.toString
    }
}

