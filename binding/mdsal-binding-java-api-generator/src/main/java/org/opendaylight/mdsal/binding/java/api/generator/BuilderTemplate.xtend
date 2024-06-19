/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static extension org.apache.commons.text.StringEscapeUtils.escapeJava
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.DATA_OBJECT
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.AUGMENTABLE_AUGMENTATION_NAME
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.AUGMENTATION_FIELD
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.DATA_CONTAINER_IMPLEMENTED_INTERFACE_NAME

import com.google.common.collect.ImmutableList
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
import org.opendaylight.mdsal.binding.model.api.ParameterizedType
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.mdsal.binding.model.util.TypeConstants
import org.opendaylight.mdsal.binding.model.util.Types
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping
import org.opendaylight.yangtools.concepts.Builder
import org.opendaylight.yangtools.yang.binding.AugmentationHolder

/**
 * Template for generating JAVA builder classes.
 */
class BuilderTemplate extends AbstractBuilderTemplate {
    /**
     * Constant used as suffix for builder name.
     */
    package static val BUILDER_STR = "Builder";

    static val BUILDER = JavaTypeName.create(Builder)

    /**
     * Constructs new instance of this class.
     * @throws IllegalArgumentException if <code>genType</code> equals <code>null</code>
     */
    new(GeneratedType genType, GeneratedType targetType, Set<BuilderGeneratedProperty> properties, Type augmentType,
            Type keyType) {
        super(genType, targetType, properties, augmentType, keyType)
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
        public class «type.name» implements «BUILDER.importedName»<«targetType.importedName»> {

            «generateFields(false)»

            «constantsDeclarations()»

            «IF augmentType !== null»
                «generateAugmentField()»
            «ENDIF»

            «generateConstructorsFromIfcs()»

            public «generateCopyConstructor(targetType, type.enclosedTypes.get(0))»

            «generateMethodFieldsFrom()»

            «generateGetters(false)»
            «IF augmentType !== null»

                «generateAugmentation()»
            «ENDIF»

            «generateSetters»

            @«OVERRIDE.importedName»
            public «targetType.name» build() {
                return new «type.enclosedTypes.get(0).importedName»(this);
            }

            «new BuilderImplTemplate(this, type.enclosedTypes.get(0)).body»
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
        public «type.name»() {
        }
        «IF (!(targetType instanceof GeneratedTransferObject))»
            «FOR impl : targetType.implements»
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
                public «type.name»(«impl.fullyQualifiedName» arg) {
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
                «IF BindingMapping.isGetterMethodName(getter.name)»
                    this._«getter.propertyNameFromGetter» = arg.«getter.name»();
                «ENDIF»
            «ENDFOR»
            «FOR impl : ifc.implements»
                «printConstructorPropertySetter(impl)»
            «ENDFOR»
        «ENDIF»
    '''

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
            if (impl instanceof GeneratedType && (impl as GeneratedType).hasNonDefaultMethods) {
                i = i + 1
            }
        }
        return i > 0
    }

    def private generateIfCheck(Type impl, List<Type> done) '''
        «IF (impl instanceof GeneratedType && (impl as GeneratedType).hasNonDefaultMethods)»
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
        «FOR getter : ifc.nonDefaultMethods»
            «IF BindingMapping.isGetterMethodName(getter.name)»
                this._«getter.propertyNameFromGetter» = ((«implementedIfc.fullyQualifiedName»)arg).«getter.name»();
            «ENDIF»
        «ENDFOR»
        «ENDIF»
    '''

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
            names.add(type.fullyQualifiedName)
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

    def private generateSetter(GeneratedProperty field) {
        val returnType = field.returnType
        if (returnType instanceof ParameterizedType) {
            if (Types.isListType(returnType)) {
                return generateListSetter(field, returnType.actualTypeArguments.get(0), "")
            } else if (Types.isMapType(returnType)) {
                return generateListSetter(field, returnType.actualTypeArguments.get(1), ".values()")
            }
        }
        return generateSimpleSetter(field, returnType)
    }

    def private generateListSetter(GeneratedProperty field, Type actualType, String extractor) '''
        «val restrictions = restrictionsForSetter(actualType)»
        «IF restrictions !== null»
            «generateCheckers(field, restrictions, actualType)»
        «ENDIF»
        public «type.getName» set«field.getName.toFirstUpper»(final «field.returnType.importedName» values) {
        «IF restrictions !== null»
            if (values != null) {
               for («actualType.importedName» value : values«extractor») {
                   «checkArgument(field, restrictions, actualType, "value")»
               }
            }
        «ENDIF»
            this.«field.fieldName» = values;
            return this;
        }

    '''

    def private generateSimpleSetter(GeneratedProperty field, Type actualType) '''
        «val restrictions = restrictionsForSetter(actualType)»
        «IF restrictions !== null»

            «generateCheckers(field, restrictions, actualType)»
        «ENDIF»

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
            «val jlClassRef = CLASS.importedName»
            «val hashMapRef = JU_HASHMAP.importedName»
            /**
              * Add an augmentation to this builder's product.
              *
              * @param augmentation augmentation to be added
              * @return this builder
              * @throws NullPointerException if {@code augmentation} is null
              */
            public «type.name» addAugmentation(«augmentTypeRef» augmentation) {
                return doAddAugmentation(augmentation.«DATA_CONTAINER_IMPLEMENTED_INTERFACE_NAME»(), augmentation);
            }

            /**
              * Add or remove an augmentation to this builder's product.
              *
              * @param augmentationType augmentation type to be added or removed
              * @param augmentationValue augmentation value, null if the augmentation type should be removed
              * @return this builder
              * @deprecated Use either {@link #addAugmentation(«augmentType.importedJavadocName»)} or {@link #removeAugmentation(«CLASS.importedName»)} instead.
              */
            @«DEPRECATED.importedName»
            public «type.name» addAugmentation(«jlClassRef»<? extends «augmentTypeRef»> augmentationType, «augmentTypeRef» augmentationValue) {
                return augmentationValue == null ? removeAugmentation(augmentationType) : doAddAugmentation(augmentationType, augmentationValue);
            }

            /**
              * Remove an augmentation from this builder's product. If this builder does not track such an augmentation
              * type, this method does nothing.
              *
              * @param augmentationType augmentation type to be removed
              * @return this builder
              */
            public «type.name» removeAugmentation(«jlClassRef»<? extends «augmentTypeRef»> augmentationType) {
                if (this.«AUGMENTATION_FIELD» instanceof «hashMapRef») {
                    this.«AUGMENTATION_FIELD».remove(augmentationType);
                }
                return this;
            }

            private «type.name» doAddAugmentation(«jlClassRef»<? extends «augmentTypeRef»> augmentationType, «augmentTypeRef» augmentationValue) {
                if (!(this.«AUGMENTATION_FIELD» instanceof «hashMapRef»)) {
                    this.«AUGMENTATION_FIELD» = new «hashMapRef»<>();
                }

                this.«AUGMENTATION_FIELD».put(augmentationType, augmentationValue);
                return this;
            }
        «ENDIF»
    '''

    private def createDescription(GeneratedType targetType) {
        val target = type.importedName
        return '''
        Class that builds {@link «target»} instances. Overall design of the class is that of a
        <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>, where method chaining is used.

        <p>
        In general, this class is supposed to be used like this template:
        <pre>
          <code>
            «target» createTarget(int fooXyzzy, int barBaz) {
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
          <li>better optimization oportunities, as the object scope is minimized in terms of invocation (rather than
              method) stack, making <a href="https://en.wikipedia.org/wiki/Escape_analysis">escape analysis</a> a lot
              easier. Given enough compiler (JIT/AOT) prowess, the cost of th builder object can be completely
              eliminated</li>
        </ul>

        @see «target»
        @see «BUILDER.importedName»
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
        @«SUPPRESS_WARNINGS.importedName»({ "unchecked", "checkstyle:methodTypeParameterName"})
        public <E$$ extends «augmentType.importedName»> E$$ «AUGMENTABLE_AUGMENTATION_NAME»(«CLASS.importedName»<E$$> augmentationType) {
            return (E$$) «AUGMENTATION_FIELD».get(«JU_OBJECTS.importedName».requireNonNull(augmentationType));
        }
    '''

    override protected generateCopyKeys(List<GeneratedProperty> keyProps) '''
        this.key = base.«BindingMapping.IDENTIFIABLE_KEY_NAME»();
        «FOR field : keyProps»
            this.«field.fieldName» = base.«field.getterMethodName»();
        «ENDFOR»
    '''

    override protected CharSequence generateCopyNonKeys(Collection<BuilderGeneratedProperty> props) '''
        «FOR field : props»
            this.«field.fieldName» = base.«field.getterName»();
        «ENDFOR»
    '''

    override protected generateCopyAugmentation(Type implType) {
        val augmentationHolderRef = AugmentationHolder.importedName
        val typeRef = targetType.importedName
        val hashMapRef = JU_HASHMAP.importedName
        val augmentTypeRef = augmentType.importedName
        return '''
            if (base instanceof «augmentationHolderRef») {
                @SuppressWarnings("unchecked")
                «JU_MAP.importedName»<«CLASS.importedName»<? extends «augmentTypeRef»>, «augmentTypeRef»> aug =((«augmentationHolderRef»<«typeRef»>) base).augmentations();
                if (!aug.isEmpty()) {
                    this.«AUGMENTATION_FIELD» = new «hashMapRef»<>(aug);
                }
            }
        '''
    }

    private static def hasNonDefaultMethods(GeneratedType type) {
        !type.methodDefinitions.isEmpty && type.methodDefinitions.exists([def | !def.isDefault])
    }

    private static def nonDefaultMethods(GeneratedType type) {
        type.methodDefinitions.filter([def | !def.isDefault])
    }
}
