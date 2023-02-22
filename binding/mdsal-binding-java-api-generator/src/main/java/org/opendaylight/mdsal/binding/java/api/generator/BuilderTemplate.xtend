/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static extension org.apache.commons.text.StringEscapeUtils.escapeJava
import static extension org.opendaylight.mdsal.binding.java.api.generator.GeneratorUtil.isNonPresenceContainer;
import static org.opendaylight.mdsal.binding.model.ri.BindingTypes.DATA_OBJECT
import static org.opendaylight.yangtools.yang.binding.contract.Naming.AUGMENTABLE_AUGMENTATION_NAME
import static org.opendaylight.yangtools.yang.binding.contract.Naming.AUGMENTATION_FIELD
import static org.opendaylight.yangtools.yang.binding.contract.Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME
import static org.opendaylight.yangtools.yang.binding.contract.Naming.IDENTIFIABLE_KEY_NAME

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import java.util.ArrayList
import java.util.Collection
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set
import org.opendaylight.mdsal.binding.model.api.AnnotationType
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject
import org.opendaylight.mdsal.binding.model.api.GeneratedType
import org.opendaylight.mdsal.binding.model.api.JavaTypeName
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.mdsal.binding.model.ri.TypeConstants
import org.opendaylight.mdsal.binding.model.ri.Types
import org.opendaylight.yangtools.yang.binding.contract.Naming

/**
 * Template for generating JAVA builder classes.
 */
class BuilderTemplate extends AbstractBuilderTemplate {
    val BuilderImplTemplate implTemplate

    /**
     * Constructs new instance of this class.
     * @throws IllegalArgumentException if <code>genType</code> equals <code>null</code>
     */
    new(GeneratedType genType, GeneratedType targetType, Type keyType) {
        super(genType, targetType, keyType)
        implTemplate = new BuilderImplTemplate(this, type.enclosedTypes.get(0))
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
        «wrapToDocumentation(formatDataForJavaDoc(targetType))»
        «targetType.annotations.generateDeprecatedAnnotation»
        «generatedAnnotation»
        public class «type.name» {

            «generateFields(false)»

            «constantsDeclarations()»

            «IF augmentType !== null»
                «val augmentTypeRef = augmentType.importedName»
                «val mapTypeRef = JU_MAP.importedName»
                «mapTypeRef»<«CLASS.importedName»<? extends «augmentTypeRef»>, «augmentTypeRef»> «AUGMENTATION_FIELD» = «mapTypeRef».of();
            «ENDIF»

            /**
             * Construct an empty builder.
             */
            public «type.name»() {
                // No-op
            }

            «generateConstructorsFromIfcs()»

            «val targetTypeName = targetType.importedName»
            /**
             * Construct a builder initialized with state from specified {@link «targetTypeName»}.
             *
             * @param base «targetTypeName» from which the builder should be initialized
             */
            public «generateCopyConstructor(targetType, type.enclosedTypes.get(0))»

            «generateMethodFieldsFrom()»

            «IF isNonPresenceContainer(targetType)»
                «generateEmptyInstance()»
            «ENDIF»

            «generateGetters(false)»
            «IF augmentType !== null»

                «generateAugmentation()»
            «ENDIF»

            «generateSetters»

            /**
             * A new {@link «targetTypeName»} instance.
             *
             * @return A new {@link «targetTypeName»} instance.
             */
            public «targetType.importedNonNull» build() {
                return new «type.enclosedTypes.get(0).importedName»(this);
            }

            «implTemplate.body»
        }
    '''

    override generateDeprecatedAnnotation(AnnotationType ann) {
        val forRemoval = ann.getParameter("forRemoval")
        if (forRemoval !== null) {
            return "@" + DEPRECATED.importedName + "(forRemoval = " + forRemoval.value + ")"
        }
        return "@" + SUPPRESS_WARNINGS.importedName + "(\"deprecation\")"
    }

    /**
     * Generate default constructor and constructor for every implemented interface from uses statements.
     */
    def private generateConstructorsFromIfcs() '''
        «IF (!(targetType instanceof GeneratedTransferObject))»
            «FOR impl : targetType.implements SEPARATOR "\n"»
                «generateConstructorFromIfc(impl)»
            «ENDFOR»
        «ENDIF»
    '''

    /**
     * Generate constructor with argument of given type.
     */
    def private Object generateConstructorFromIfc(Type impl) '''
        «IF (impl instanceof GeneratedType)»
            «IF impl.hasNonDefaultMethods»
                «val typeName = impl.importedName»
                /**
                 * Construct a new builder initialized from specified {@link «typeName»}.
                 *
                 * @param arg «typeName» from which the builder should be initialized
                 */
                public «type.name»(«typeName» arg) {
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
            «FOR getter : ifc.nonDefaultMethods»
                «IF Naming.isGetterMethodName(getter.name)»
                    «val propertyName = getter.propertyNameFromGetter»
                    «printPropertySetter(getter, '''arg.«getter.name»()''', propertyName)»;
                «ENDIF»
            «ENDFOR»
            «FOR impl : ifc.implements»
                «printConstructorPropertySetter(impl, getSpecifiedGetters(ifc))»
            «ENDFOR»
        «ENDIF»
    '''

    def private Object printConstructorPropertySetter(Type implementedIfc, Set<MethodSignature> alreadySetProperties) '''
        «IF (implementedIfc instanceof GeneratedType && !(implementedIfc instanceof GeneratedTransferObject))»
            «val ifc = implementedIfc as GeneratedType»
            «FOR getter : ifc.nonDefaultMethods»
                «IF Naming.isGetterMethodName(getter.name) && getterByName(alreadySetProperties, getter.name).isEmpty»
                    «val propertyName = getter.propertyNameFromGetter»
                    «printPropertySetter(getter, '''arg.«getter.name»()''', propertyName)»;
                «ENDIF»
            «ENDFOR»
            «FOR descendant : ifc.implements»
                «printConstructorPropertySetter(descendant, Sets.union(alreadySetProperties, getSpecifiedGetters(ifc)))»
            «ENDFOR»
        «ENDIF»
    '''

    def static Set<MethodSignature> getSpecifiedGetters(GeneratedType type) {
        val ImmutableSet.Builder<MethodSignature> setBuilder = new ImmutableSet.Builder
        for (MethodSignature method : type.getMethodDefinitions()) {
            if (method.hasOverrideAnnotation) {
                setBuilder.add(method)
            }
        }
        return setBuilder.build()
    }

    /**
     * Generate 'fieldsFrom' method to set builder properties based on type of given argument.
     */
    def private generateMethodFieldsFrom() '''
        «IF (!(targetType instanceof GeneratedTransferObject))»
            «IF targetType.hasImplementsFromUses»
                «val List<Type> done = targetType.getBaseIfcs»
                «generateMethodFieldsFromComment(targetType)»
                public void fieldsFrom(«DATA_OBJECT.importedName» arg) {
                    boolean isValidArg = false;
                    «FOR impl : targetType.getAllIfcs»
                        «generateIfCheck(impl, done)»
                    «ENDFOR»
                    «CODEHELPERS.importedName».validValue(isValidArg, arg, "«targetType.getAllIfcs.toListOfNames»");
                }
            «ENDIF»
        «ENDIF»
    '''

    /**
     * Generate EMPTY instance which is lazily initialized in empty() method.
     */
    // TODO: use lazy static field once we have https://openjdk.org/jeps/8209964
    def private generateEmptyInstance() '''
        «val nonnullTarget = targetType.importedNonNull»
        private static final class LazyEmpty {
            static final «nonnullTarget» INSTANCE = new «type.name»().build();

            private LazyEmpty() {
                // Hidden on purpose
            }
        }

        /**
         * Get empty instance of «targetType.name».
         *
         * @return An empty {@link «targetType.name»}
         */
        public static «nonnullTarget» empty() {
            return LazyEmpty.INSTANCE;
        }
    '''

    def private generateMethodFieldsFromComment(GeneratedType type) '''
        /**
         * Set fields from given grouping argument. Valid argument is instance of one of following types:
         * <ul>
         «FOR impl : type.getAllIfcs»
         *   <li>{@link «impl.importedName»}</li>
         «ENDFOR»
         * </ul>
         *
         * @param arg grouping object
         * @throws «IAE.importedName» if given argument is none of valid types or has property with incompatible value
        */
    '''

    /**
     * Method is used to find out if given type implements any interface from uses.
     */
    def boolean hasImplementsFromUses(GeneratedType type) {
        var i = 0
        for (impl : type.getAllIfcs) {
            if (impl instanceof GeneratedType && (impl as GeneratedType).hasNonDefaultMethods) {
                i = i + 1
            }
        }
        return i > 0
    }

    def private generateIfCheck(Type impl, List<Type> done) '''
        «IF (impl instanceof GeneratedType && (impl as GeneratedType).hasNonDefaultMethods)»
            «val implType = impl as GeneratedType»
            if (arg instanceof «implType.importedName» castArg) {
                «printPropertySetter(implType)»
                isValidArg = true;
            }
        «ENDIF»
    '''

    def private printPropertySetter(Type implementedIfc) '''
        «IF (implementedIfc instanceof GeneratedType && !(implementedIfc instanceof GeneratedTransferObject))»
        «val ifc = implementedIfc as GeneratedType»
        «FOR getter : ifc.nonDefaultMethods»
            «IF Naming.isGetterMethodName(getter.name) && !hasOverrideAnnotation(getter)»
                «printPropertySetter(getter, '''castArg.«getter.name»()''', getter.propertyNameFromGetter)»;
            «ENDIF»
        «ENDFOR»
        «ENDIF»
    '''

    def private printPropertySetter(MethodSignature getter, String retrieveProperty, String propertyName) {
        val ownGetter = implTemplate.findGetter(getter.name)
        val ownGetterType = ownGetter.returnType
        if (Types.strictTypeEquals(getter.returnType, ownGetterType)) {
            return "this._" + propertyName + " = " + retrieveProperty
        }
        if (ownGetterType instanceof ParameterizedType) {
            val itemType = ownGetterType.actualTypeArguments.get(0)
            if (Types.isListType(ownGetterType)) {
                return printPropertySetter(retrieveProperty, propertyName, "checkListFieldCast", itemType.importedName)
            }
            if (Types.isSetType(ownGetterType)) {
                return printPropertySetter(retrieveProperty, propertyName, "checkSetFieldCast", itemType.importedName)
            }
        }
        return printPropertySetter(retrieveProperty, propertyName, "checkFieldCast", ownGetterType.importedName)
    }

    def private printPropertySetter(String retrieveProperty, String propertyName, String checkerName, String className) '''
            this._«propertyName» = «CODEHELPERS.importedName».«checkerName»(«className».class, "«propertyName»", «retrieveProperty»)'''

    private def List<Type> getBaseIfcs(GeneratedType type) {
        val List<Type> baseIfcs = new ArrayList();
        for (ifc : type.implements) {
            if (ifc instanceof GeneratedType && (ifc as GeneratedType).hasNonDefaultMethods) {
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
                if (impl instanceof GeneratedType && (impl as GeneratedType).hasNonDefaultMethods) {
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
            names.add(type.importedName)
        }
        return names
    }

    def private constantsDeclarations() '''
        «FOR c : type.getConstantDefinitions»
            «IF c.getName.startsWith(TypeConstants.PATTERN_CONSTANT_NAME)»
                «val cValue = c.value as Map<String, String>»
                «val String fieldSuffix = c.getName.substring(TypeConstants.PATTERN_CONSTANT_NAME.length)»
                «val jurPatternRef = JUR_PATTERN.importedName»
                «IF cValue.size == 1»
                   «val firstEntry = cValue.entrySet.iterator.next»
                   private static final «jurPatternRef» «Constants.MEMBER_PATTERN_LIST»«fieldSuffix» = «jurPatternRef».compile("«firstEntry.key.escapeJava»");
                   private static final String «Constants.MEMBER_REGEX_LIST»«fieldSuffix» = "«firstEntry.value.escapeJava»";
                «ELSE»
                   private static final «jurPatternRef»[] «Constants.MEMBER_PATTERN_LIST»«fieldSuffix» = «CODEHELPERS.importedName».compilePatterns(«ImmutableList.importedName».of(
                   «FOR v : cValue.keySet SEPARATOR ", "»"«v.escapeJava»"«ENDFOR»));
                   private static final String[] «Constants.MEMBER_REGEX_LIST»«fieldSuffix» = { «
                   FOR v : cValue.values SEPARATOR ", "»"«v.escapeJava»"«ENDFOR» };
                «ENDIF»
            «ELSE»
                «emitConstant(c)»
            «ENDIF»
        «ENDFOR»
    '''

    def private generateSetter(BuilderGeneratedProperty field) {
        val returnType = field.returnType
        if (returnType instanceof ParameterizedType) {
            if (Types.isListType(returnType) || Types.isSetType(returnType)) {
                val arguments = returnType.actualTypeArguments
                if (arguments.isEmpty) {
                    return generateListSetter(field, Types.objectType)
                }
                return generateListSetter(field, arguments.get(0))
            } else if (Types.isMapType(returnType)) {
                return generateMapSetter(field, returnType.actualTypeArguments.get(1))
            }
        }
        return generateSimpleSetter(field, returnType)
    }

    def private generateListSetter(BuilderGeneratedProperty field, Type actualType) '''
        «val restrictions = restrictionsForSetter(actualType)»
        «IF restrictions !== null»
            «generateCheckers(field, restrictions, actualType)»
        «ENDIF»

        /**
         * Set the property corresponding to {@link «targetType.importedName»#«field.getterName»()} to the specified
         * value.
         *
         * @param values desired value
         * @return this builder
         */
        public «type.getName» set«field.getName.toFirstUpper»(final «field.returnType.importedName» values) {
        «IF restrictions !== null»
            if (values != null) {
               for («actualType.importedName» value : values) {
                   «checkArgument(field, restrictions, actualType, "value")»
               }
            }
        «ENDIF»
            this.«field.fieldName» = values;
            return this;
        }

    '''

    def private generateMapSetter(BuilderGeneratedProperty field, Type actualType) '''
        «val restrictions = restrictionsForSetter(actualType)»
        «IF restrictions !== null»
            «generateCheckers(field, restrictions, actualType)»
        «ENDIF»

        /**
         * Set the property corresponding to {@link «targetType.importedName»#«field.getterName»()} to the specified
         * value.
         *
         * @param values desired value
         * @return this builder
         */
        public «type.getName» set«field.name.toFirstUpper»(final «field.returnType.importedName» values) {
        «IF restrictions !== null»
            if (values != null) {
               for («actualType.importedName» value : values.values()) {
                   «checkArgument(field, restrictions, actualType, "value")»
               }
            }
        «ENDIF»
            this.«field.fieldName» = values;
            return this;
        }
    '''

    def private generateSimpleSetter(BuilderGeneratedProperty field, Type actualType) '''
        «val restrictions = restrictionsForSetter(actualType)»
        «IF restrictions !== null»

            «generateCheckers(field, restrictions, actualType)»
        «ENDIF»

        /**
         * Set the property corresponding to {@link «targetType.importedName»#«field.getterName»()} to the specified
         * value.
         *
         * @param value desired value
         * @return this builder
         */
        «val setterName = "set" + field.getName.toFirstUpper»
        public «type.getName» «setterName»(final «field.returnType.importedName» value) {
            «IF restrictions !== null»
                if (value != null) {
                    «checkArgument(field, restrictions, actualType, "value")»
                }
            «ENDIF»
            this.«field.fieldName» = value;
            return this;
        }
    '''

    /**
     * Template method which generates setter methods
     *
     * @return string with the setter methods
     */
    def private generateSetters() '''
        «IF keyType !== null»
            /**
             * Set the key value corresponding to {@link «targetType.importedName»#«IDENTIFIABLE_KEY_NAME»()} to the specified
             * value.
             *
             * @param key desired value
             * @return this builder
             */
            public «type.getName» withKey(final «keyType.importedName» key) {
                this.key = key;
                return this;
            }
        «ENDIF»
        «FOR property : properties»
            «generateSetter(property)»
        «ENDFOR»

        «IF augmentType !== null»
            «val augmentTypeRef = augmentType.importedName»
            «val hashMapRef = JU_HASHMAP.importedName»
            /**
              * Add an augmentation to this builder's product.
              *
              * @param augmentation augmentation to be added
              * @return this builder
              * @throws «NPE.importedName» if {@code augmentation} is null
              */
            public «type.name» addAugmentation(«augmentTypeRef» augmentation) {
                if (!(this.«AUGMENTATION_FIELD» instanceof «hashMapRef»)) {
                    this.«AUGMENTATION_FIELD» = new «hashMapRef»<>();
                }

                this.«AUGMENTATION_FIELD».put(augmentation.«BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME»(), augmentation);
                return this;
            }

            /**
              * Remove an augmentation from this builder's product. If this builder does not track such an augmentation
              * type, this method does nothing.
              *
              * @param augmentationType augmentation type to be removed
              * @return this builder
              */
            public «type.name» removeAugmentation(«CLASS.importedName»<? extends «augmentTypeRef»> augmentationType) {
                if (this.«AUGMENTATION_FIELD» instanceof «hashMapRef») {
                    this.«AUGMENTATION_FIELD».remove(augmentationType);
                }
                return this;
            }
        «ENDIF»
    '''

    private def createDescription(GeneratedType targetType) {
        val target = targetType.importedName
        return '''
        Class that builds {@link «target»} instances. Overall design of the class is that of a
        <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>, where method chaining is used.

        <p>
        In general, this class is supposed to be used like this template:
        <pre>
          <code>
            «target» create«target»(int fooXyzzy, int barBaz) {
                return new «target»Builder()
                    .setFoo(new FooBuilder().setXyzzy(fooXyzzy).build())
                    .setBar(new BarBuilder().setBaz(barBaz).build())
                    .build();
            }
          </code>
        </pre>

        <p>
        This pattern is supported by the immutable nature of «target», as instances can be freely passed around without
        worrying about synchronization issues.

        <p>
        As a side note: method chaining results in:
        <ul>
          <li>very efficient Java bytecode, as the method invocation result, in this case the Builder reference, is
              on the stack, so further method invocations just need to fill method arguments for the next method
              invocation, which is terminated by {@link #build()}, which is then returned from the method</li>
          <li>better understanding by humans, as the scope of mutable state (the builder) is kept to a minimum and is
              very localized</li>
          <li>better optimization opportunities, as the object scope is minimized in terms of invocation (rather than
              method) stack, making <a href="https://en.wikipedia.org/wiki/Escape_analysis">escape analysis</a> a lot
              easier. Given enough compiler (JIT/AOT) prowess, the cost of th builder object can be completely
              eliminated</li>
        </ul>

        @see «target»
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

    private def generateAugmentation() '''
        /**
         * Return the specified augmentation, if it is present in this builder.
         *
         * @param <E$$> augmentation type
         * @param augmentationType augmentation type class
         * @return Augmentation object from this builder, or {@code null} if not present
         * @throws «NPE.importedName» if {@code augmentType} is {@code null}
         */
        @«SUPPRESS_WARNINGS.importedName»({ "unchecked", "checkstyle:methodTypeParameterName"})
        public <E$$ extends «augmentType.importedName»> E$$ «AUGMENTABLE_AUGMENTATION_NAME»(«CLASS.importedName»<E$$> augmentationType) {
            return (E$$) «AUGMENTATION_FIELD».get(«JU_OBJECTS.importedName».requireNonNull(augmentationType));
        }
    '''

    override protected generateCopyKeys(List<GeneratedProperty> keyProps) '''
        this.key = base.«IDENTIFIABLE_KEY_NAME»();
        «FOR field : keyProps»
            this.«field.fieldName» = base.«field.getterMethodName»();
        «ENDFOR»
    '''

    override protected CharSequence generateCopyNonKeys(Collection<BuilderGeneratedProperty> props) '''
        «FOR field : props»
            this.«field.fieldName» = base.«field.getterName»();
        «ENDFOR»
    '''

    override protected generateCopyAugmentation(Type implType) '''
       final var aug = base.augmentations();
       if (!aug.isEmpty()) {
           this.«AUGMENTATION_FIELD» = new «JU_HASHMAP.importedName»<>(aug);
       }
    '''
}
