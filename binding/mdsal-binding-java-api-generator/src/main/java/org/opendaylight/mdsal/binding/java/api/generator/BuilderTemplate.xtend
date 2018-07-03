/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static extension org.apache.commons.text.StringEscapeUtils.escapeJava;
import static org.opendaylight.yangtools.yang.binding.BindingMapping.AUGMENTATION_FIELD
import static org.opendaylight.yangtools.yang.binding.BindingMapping.AUGMENTABLE_AUGMENTATION_NAME

import com.google.common.base.MoreObjects
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSortedSet
import com.google.common.collect.ImmutableList
import java.util.ArrayList
import java.util.Arrays
import java.util.Collection
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashSet
import java.util.List
import java.util.Map
import java.util.Objects
import java.util.Set
import java.util.regex.Pattern
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject
import org.opendaylight.mdsal.binding.model.api.GeneratedType
import org.opendaylight.mdsal.binding.model.api.JavaTypeName
import org.opendaylight.mdsal.binding.model.api.MethodSignature
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.mdsal.binding.model.api.ParameterizedType
import org.opendaylight.mdsal.binding.model.util.ReferencedTypeImpl
import org.opendaylight.mdsal.binding.model.util.Types
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.CodegenGeneratedTOBuilder
import org.opendaylight.mdsal.binding.model.util.TypeConstants
import org.opendaylight.yangtools.concepts.Builder
import org.opendaylight.yangtools.yang.binding.Augmentable
import org.opendaylight.yangtools.yang.binding.AugmentationHolder
import org.opendaylight.yangtools.yang.binding.BindingMapping
import org.opendaylight.yangtools.yang.binding.CodeHelpers
import org.opendaylight.yangtools.yang.binding.DataObject
import org.opendaylight.yangtools.yang.binding.Identifiable

/**
 * Template for generating JAVA builder classes.
 */

class BuilderTemplate extends BaseTemplate {
    /**
     * Constant with the suffix for builder classes.
     */
    val static BUILDER = 'Builder'

    /**
     * Constant with suffix for the classes which are generated from the builder classes.
     */
    val static IMPL = 'Impl'

    /**
     * Generated property is set if among methods is found one with the name GET_AUGMENTATION_METHOD_NAME.
     */
    var Type augmentType

    /**
     * Set of class attributes (fields) which are derived from the getter methods names.
     */
    val Set<GeneratedProperty> properties

    /**
     * GeneratedType for key type, null if this type does not have a key.
     */
    val Type keyType

    static val METHOD_COMPARATOR = new AlphabeticallyTypeMemberComparator<MethodSignature>();

    /**
     * Constructs new instance of this class.
     * @throws IllegalArgumentException if <code>genType</code> equals <code>null</code>
     */
    new(GeneratedType genType) {
        super(new TopLevelJavaGeneratedType(builderName(genType), genType), genType)
        this.properties = propertiesFromMethods(createMethods)
        keyType = genType.key
    }

    def static builderName(GeneratedType genType) {
        val name = genType.identifier
        name.createSibling(name.simpleName + "Builder")
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
        if (implementedIfcs === null || implementedIfcs.empty) {
            return
        }
        for (implementedIfc : implementedIfcs) {
            if ((implementedIfc instanceof GeneratedType && !(implementedIfc instanceof GeneratedTransferObject))) {
                val ifc = implementedIfc as GeneratedType
                methods.addAll(ifc.methodDefinitions)
                collectImplementedMethods(methods, ifc.implements)
            } else if (implementedIfc.fullyQualifiedName == Augmentable.name) {
                val m = Augmentable.getDeclaredMethod(AUGMENTABLE_AUGMENTATION_NAME, Class)
                val identifier = JavaTypeName.create(m.returnType)
                val refType = new ReferencedTypeImpl(identifier)
                val generic = new ReferencedTypeImpl(type.identifier)
                augmentType = Types.parameterizedTypeFor(refType, generic)
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
     * Creates set of generated property instances from getter <code>methods</code>.
     *
     * @param set of method signature instances which should be transformed to list of properties
     * @return set of generated property instances which represents the getter <code>methods</code>
     */
    def private propertiesFromMethods(Collection<MethodSignature> methods) {
        if (methods === null || methods.isEmpty()) {
            return Collections.emptySet
        }
        val Set<GeneratedProperty> result = new LinkedHashSet
        for (m : methods) {
            val createdField = m.propertyFromGetter
            if (createdField !== null) {
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
        if (method === null || method.name === null || method.name.empty || method.returnType === null) {
            throw new IllegalArgumentException("Method, method name, method return type reference cannot be NULL or empty!")
        }
        var prefix = "get";
        if (Types.BOOLEAN.equals(method.returnType)) {
            prefix = "is";
        }
        if (method.name.startsWith(prefix)) {
            val fieldName = method.getName().substring(prefix.length()).toFirstLower
            val tmpGenTO = new CodegenGeneratedTOBuilder(JavaTypeName.create("foo", "foo"))
            tmpGenTO.addProperty(fieldName).setReturnType(method.returnType)
            return tmpGenTO.build.properties.first
        }
    }

    override isLocalInnerClass(JavaTypeName name) {
        // Builders do not have inner types
        return false;
    }

    /**
     * Template method which generates JAVA class body for builder class and for IMPL class.
     *
     * @return string with JAVA source code
     */
    override body() '''
        «wrapToDocumentation(formatDataForJavaDoc(type))»
        public class «type.name»«BUILDER» implements «Builder.importedName»<«type.importedName»> {

            «generateFields(false)»

            «constantsDeclarations()»

            «generateAugmentField(false)»

            «generateConstructorsFromIfcs(type)»

            «generateCopyConstructor(false)»

            «generateMethodFieldsFrom(type)»

            «generateGetters(false)»

            «generateSetters»

            @«Override.importedName»
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
                    «CodeHelpers.importedName».validValue(isValidArg, arg, "«ifc.getAllIfcs.toListOfNames»");
                }
            «ENDIF»
        «ENDIF»
    '''

    def private generateMethodFieldsFromComment(GeneratedType type) '''
        /**
         * Set fields from given grouping argument. Valid argument is instance of one of following types:
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
            «ENDFOR»
        «ENDIF»
        «IF keyType !== null»
            private«IF _final» final«ENDIF» «keyType.importedName» key;
        «ENDIF»
    '''

    def private generateAugmentField(boolean isPrivate) '''
        «IF augmentType !== null»
            «IF isPrivate»private «ENDIF»«Map.importedName»<«Class.importedName»<? extends «augmentType.importedName»>, «augmentType.importedName»> «AUGMENTATION_FIELD» = «Collections.importedName».emptyMap();
        «ENDIF»
    '''

    def private constantsDeclarations() '''
        «FOR c : type.getConstantDefinitions»
            «IF c.getName.startsWith(TypeConstants.PATTERN_CONSTANT_NAME)»
                «val cValue = c.value as Map<String, String>»
                «val String fieldSuffix = c.getName.substring(TypeConstants.PATTERN_CONSTANT_NAME.length)»
                «IF cValue.size == 1»
                   private static final «Pattern.importedName» «Constants.MEMBER_PATTERN_LIST»«fieldSuffix» = «Pattern.importedName».compile("«cValue.keySet.get(0).escapeJava»");
                   private static final String «Constants.MEMBER_REGEX_LIST»«fieldSuffix» = "«cValue.values.get(0).escapeJava»";
                «ELSE»
                   private static final «Pattern.importedName»[] «Constants.MEMBER_PATTERN_LIST»«fieldSuffix» = «CodeHelpers.importedName».compilePatterns(«ImmutableList.importedName».of(
                   «FOR v : cValue.keySet SEPARATOR ", "»"«v.escapeJava»"«ENDFOR»));
                   private static final String[] «Constants.MEMBER_REGEX_LIST»«fieldSuffix» = { «
                   FOR v : cValue.values SEPARATOR ", "»"«v.escapeJava»"«ENDFOR» };
                «ENDIF»
            «ELSE»
                «emitConstant(c)»
            «ENDIF»
        «ENDFOR»
    '''

    def private generateListSetter(GeneratedProperty field, Type actualType) '''
        «val restrictions = restrictionsForSetter(actualType)»
        «IF restrictions !== null»
            «generateCheckers(field, restrictions, actualType)»
        «ENDIF»
        public «type.getName»Builder set«field.getName.toFirstUpper»(final «field.returnType.importedName» values) {
        «IF restrictions !== null»
            if (values != null) {
               for («actualType.getFullyQualifiedName» value : values) {
                   «checkArgument(field, restrictions, actualType, "value")»
               }
            }
        «ENDIF»
            this.«field.fieldName.toString» = values;
            return this;
        }

    '''

    def private generateSetter(GeneratedProperty field, Type actualType) '''
        «val restrictions = restrictionsForSetter(actualType)»
        «IF restrictions !== null»
            «generateCheckers(field, restrictions, actualType)»
        «ENDIF»

        public «type.getName»Builder set«field.getName.toFirstUpper»(final «field.returnType.importedName» value) {
        «IF restrictions !== null»
            if (value != null) {
                «checkArgument(field, restrictions, actualType, "value")»
            }
        «ENDIF»
            this.«field.fieldName.toString» = value;
            return this;
        }
    '''

    private def Type getActualType(ParameterizedType ptype) {
        return ptype.getActualTypeArguments.get(0)
    }

    /**
     * Template method which generates setter methods
     *
     * @return string with the setter methods
     */
    def private generateSetters() '''
        «IF keyType !== null»
            public «type.getName»Builder withKey(final «keyType.importedName» key) {
                this.key = key;
                return this;
            }
        «ENDIF»
        «FOR property : properties»
            «IF property.returnType instanceof ParameterizedType && Types.isListType(property.returnType)»
                «generateListSetter(property, getActualType(property.returnType as ParameterizedType))»
            «ELSE»
                «generateSetter(property, property.returnType)»
            «ENDIF»
        «ENDFOR»

        «IF augmentType !== null»
            public «type.name»«BUILDER» add«AUGMENTATION_FIELD.toFirstUpper»(«Class.importedName»<? extends «augmentType.importedName»> augmentationType, «augmentType.importedName» augmentationValue) {
                if (augmentationValue == null) {
                    return remove«AUGMENTATION_FIELD.toFirstUpper»(augmentationType);
                }

                if (!(this.«AUGMENTATION_FIELD» instanceof «HashMap.importedName»)) {
                    this.«AUGMENTATION_FIELD» = new «HashMap.importedName»<>();
                }

                this.«AUGMENTATION_FIELD».put(augmentationType, augmentationValue);
                return this;
            }

            public «type.name»«BUILDER» remove«AUGMENTATION_FIELD.toFirstUpper»(«Class.importedName»<? extends «augmentType.importedName»> augmentationType) {
                if (this.«AUGMENTATION_FIELD» instanceof «HashMap.importedName») {
                    this.«AUGMENTATION_FIELD».remove(augmentationType);
                }
                return this;
            }
        «ENDIF»
    '''

    def private CharSequence generateCopyConstructor(boolean impl) '''
        «IF impl»private«ELSE»public«ENDIF» «type.name»«IF impl»«IMPL»«ELSE»«BUILDER»«ENDIF»(«type.name»«IF impl»«BUILDER»«ENDIF» base) {
            «val allProps = new ArrayList(properties)»
            «val isList = implementsIfc(type, Types.parameterizedTypeFor(Types.typeForClass(Identifiable), type))»
            «IF isList && keyType !== null»
                «val keyProps = new ArrayList((keyType as GeneratedTransferObject).properties)»
                «Collections.sort(keyProps, [ p1, p2 | return p1.name.compareTo(p2.name) ])»
                «FOR field : keyProps»
                    «removeProperty(allProps, field.name)»
                «ENDFOR»
                if (base.«BindingMapping.IDENTIFIABLE_KEY_NAME»() == null) {
                    this.key = new «keyType.importedName»(
                        «FOR keyProp : keyProps SEPARATOR ", "»
                            base.«keyProp.getterMethodName»()
                        «ENDFOR»
                    );
                    «FOR field : keyProps»
                        this.«field.fieldName» = base.«field.getterMethodName»();
                    «ENDFOR»
                } else {
                    this.key = base.«BindingMapping.IDENTIFIABLE_KEY_NAME»();
                    «FOR field : keyProps»
                           this.«field.fieldName» = key.«field.getterMethodName»();
                    «ENDFOR»
                }
            «ENDIF»
            «FOR field : allProps»
                this.«field.fieldName» = base.«field.getterMethodName»();
            «ENDFOR»
            «IF augmentType !== null»
                «IF impl»
                    this.«AUGMENTATION_FIELD» = «ImmutableMap.importedName».copyOf(base.«AUGMENTATION_FIELD»);
                «ELSE»
                    if (base instanceof «type.name»«IMPL») {
                        «type.name»«IMPL» impl = («type.name»«IMPL») base;
                        if (!impl.«AUGMENTATION_FIELD».isEmpty()) {
                            this.«AUGMENTATION_FIELD» = new «HashMap.importedName»<>(impl.«AUGMENTATION_FIELD»);
                        }
                    } else if (base instanceof «AugmentationHolder.importedName») {
                        @SuppressWarnings("unchecked")
                        «AugmentationHolder.importedName»<«type.importedName»> casted =(«AugmentationHolder.importedName»<«type.importedName»>) base;
                        if (!casted.augmentations().isEmpty()) {
                            this.«AUGMENTATION_FIELD» = new «HashMap.importedName»<>(casted.augmentations());
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

    private def getKey(GeneratedType type) {
        for (m : type.methodDefinitions) {
            if (BindingMapping.IDENTIFIABLE_KEY_NAME.equals(m.name)) {
                return m.returnType;
            }
        }
    }

    private def void removeProperty(Collection<GeneratedProperty> props, String name) {
        var GeneratedProperty toRemove = null
        for (p : props) {
            if (p.name.equals(name)) {
                toRemove = p;
            }
        }
        if (toRemove !== null) {
            props.remove(toRemove);
        }
    }

    /**
     * Template method which generate getter methods for IMPL class.
     *
     * @return string with getter methods
     */
    def private generateGetters(boolean addOverride) '''
        «IF keyType !== null»
            «IF addOverride»@«Override.importedName»«ENDIF»
            public «keyType.importedName» «BindingMapping.IDENTIFIABLE_KEY_NAME»() {
                return key;
            }

        «ENDIF»
        «IF !properties.empty»
            «FOR field : properties SEPARATOR '\n'»
                «IF addOverride»@«Override.importedName»«ENDIF»
                «field.getterMethod»
            «ENDFOR»
        «ENDIF»
        «IF augmentType !== null»

            @SuppressWarnings("unchecked")
            «IF addOverride»@«Override.importedName»«ENDIF»
            public <E extends «augmentType.importedName»> E «AUGMENTABLE_AUGMENTATION_NAME»(«Class.importedName»<E> augmentationType) {
                return (E) «AUGMENTATION_FIELD».get(«CodeHelpers.importedName».nonNullValue(augmentationType, "augmentationType"));
            }
        «ENDIF»
    '''

    /**
     * Template method which generates the method <code>hashCode()</code>.
     *
     * @return string with the <code>hashCode()</code> method definition in JAVA format
     */
    def protected generateHashCode() '''
        «IF !properties.empty || augmentType !== null»
            private int hash = 0;
            private volatile boolean hashValid = false;

            @«Override.importedName»
            public int hashCode() {
                if (hashValid) {
                    return hash;
                }

                final int prime = 31;
                int result = 1;
                «FOR property : properties»
                    «IF property.returnType.name.contains("[")»
                    result = prime * result + «Arrays.importedName».hashCode(«property.fieldName»);
                    «ELSE»
                    result = prime * result + «Objects.importedName».hashCode(«property.fieldName»);
                    «ENDIF»
                «ENDFOR»
                «IF augmentType !== null»
                    result = prime * result + «Objects.importedName».hashCode(«AUGMENTATION_FIELD»);
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
        «IF !properties.empty || augmentType !== null»
            @«Override.importedName»
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
                    «IF property.returnType.name.contains("[")»
                    if (!«Arrays.importedName».equals(«fieldName», other.«property.getterMethodName»())) {
                    «ELSE»
                    if (!«Objects.importedName».equals(«fieldName», other.«property.getterMethodName»())) {
                    «ENDIF»
                        return false;
                    }
                «ENDFOR»
                «IF augmentType !== null»
                    if (getClass() == obj.getClass()) {
                        // Simple case: we are comparing against self
                        «type.name»«IMPL» otherImpl = («type.name»«IMPL») obj;
                        if (!«Objects.importedName».equals(«AUGMENTATION_FIELD», otherImpl.«AUGMENTATION_FIELD»)) {
                            return false;
                        }
                    } else {
                        // Hard case: compare our augments with presence there...
                        for («Map.importedName».Entry<«Class.importedName»<? extends «augmentType.importedName»>, «augmentType.importedName»> e : «AUGMENTATION_FIELD».entrySet()) {
                            if (!e.getValue().equals(other.«AUGMENTABLE_AUGMENTATION_NAME»(e.getKey()))) {
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

    override generateToString(Collection<GeneratedProperty> properties) '''
        «IF properties !== null»
            @«Override.importedName»
            public «String.importedName» toString() {
                final «MoreObjects.importedName».ToStringHelper helper = «MoreObjects.importedName».toStringHelper("«type.name»");
                «FOR property : properties»
                    «CodeHelpers.importedName».appendValue(helper, "«property.fieldName»", «property.fieldName»);
                «ENDFOR»
                «IF augmentType !== null»
                    «CodeHelpers.importedName».appendValue(helper, "«AUGMENTATION_FIELD»", «AUGMENTATION_FIELD».values());
                «ENDIF»
                return helper.toString();
            }
        «ENDIF»
    '''

    def implementedInterfaceGetter() '''
    @«Override.importedName»
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

    override protected String formatDataForJavaDoc(GeneratedType type) {
        val typeDescription = createDescription(type)

        return '''
            «IF !typeDescription.nullOrEmpty»
            «typeDescription»
            «ENDIF»
        '''.toString
    }
}

