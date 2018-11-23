/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static extension org.apache.commons.text.StringEscapeUtils.escapeJava
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.AUGMENTATION_FIELD

import com.google.common.collect.ImmutableList
import java.util.ArrayList
import java.util.Collection
import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set
import java.util.regex.Pattern
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
import org.opendaylight.yangtools.yang.binding.CodeHelpers
import org.opendaylight.yangtools.yang.binding.DataObject

/**
 * Template for generating JAVA builder classes.
 */
class BuilderTemplate extends AbstractBuilderTemplate {
    /**
     * Constant used as suffix for builder name.
     */
    public static val BUILDER = "Builder";

    /**
     * Constructs new instance of this class.
     * @throws IllegalArgumentException if <code>genType</code> equals <code>null</code>
     */
    new(GeneratedType genType, GeneratedType targetType, Set<GeneratedProperty> properties, Type augmentType,
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
        «wrapToDocumentation(formatDataForJavaDoc(type))»
        public class «type.name» implements «Builder.importedName»<«targetType.importedName»> {

            «generateFields(false)»

            «constantsDeclarations()»

            «IF augmentType !== null»
                «generateAugmentField()»
            «ENDIF»

            «generateConstructorsFromIfcs()»

            public «generateCopyConstructor(targetType, type.enclosedTypes.get(0))»

            «generateMethodFieldsFrom()»

            «generateGetters(false)»

            «generateSetters»

            @«Override.importedName»
            public «targetType.name» build() {
                return new «type.enclosedTypes.get(0).importedName»(this);
            }

            «new BuilderImplTemplate(this, type.enclosedTypes.get(0)).body»
        }
    '''

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
                public void fieldsFrom(«DataObject.importedName» arg) {
                    boolean isValidArg = false;
                    «FOR impl : targetType.getAllIfcs»
                        «generateIfCheck(impl, done)»
                    «ENDFOR»
                    «CodeHelpers.importedName».validValue(isValidArg, arg, "«targetType.getAllIfcs.toListOfNames»");
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
        public «type.getName» set«field.getName.toFirstUpper»(final «field.returnType.importedName» values) {
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

        public «type.getName» set«field.getName.toFirstUpper»(final «field.returnType.importedName» value) {
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
            public «type.getName» withKey(final «keyType.importedName» key) {
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
            public «type.name» add«AUGMENTATION_FIELD.toFirstUpper»(«Class.importedName»<? extends «augmentType.importedName»> augmentationType, «augmentType.importedName» augmentationValue) {
                if (augmentationValue == null) {
                    return remove«AUGMENTATION_FIELD.toFirstUpper»(augmentationType);
                }

                if (!(this.«AUGMENTATION_FIELD» instanceof «HashMap.importedName»)) {
                    this.«AUGMENTATION_FIELD» = new «HashMap.importedName»<>();
                }

                this.«AUGMENTATION_FIELD».put(augmentationType, augmentationValue);
                return this;
            }

            public «type.name» remove«AUGMENTATION_FIELD.toFirstUpper»(«Class.importedName»<? extends «augmentType.importedName»> augmentationType) {
                if (this.«AUGMENTATION_FIELD» instanceof «HashMap.importedName») {
                    this.«AUGMENTATION_FIELD».remove(augmentationType);
                }
                return this;
            }
        «ENDIF»
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

    override protected generateCopyKeys(List<GeneratedProperty> keyProps) '''
        this.key = base.«BindingMapping.IDENTIFIABLE_KEY_NAME»();
        «FOR field : keyProps»
            this.«field.fieldName» = base.«field.getterMethodName»();
        «ENDFOR»
    '''

    override protected generateCopyAugmentation(Type implType) {
        val implTypeRef = implType.importedName
        val augmentationHolderRef = AugmentationHolder.importedName
        val typeRef = targetType.importedName
        val hashMapRef = HashMap.importedName
        val augmentTypeRef = augmentType.importedName
        return '''
            if (base instanceof «implTypeRef») {
                «implTypeRef» impl = («implTypeRef») base;
                if (!impl.«AUGMENTATION_FIELD».isEmpty()) {
                    this.«AUGMENTATION_FIELD» = new «hashMapRef»<>(impl.«AUGMENTATION_FIELD»);
                }
            } else if (base instanceof «augmentationHolderRef») {
                @SuppressWarnings("unchecked")
                «Map.importedName»<«Class.importedName»<? extends «augmentTypeRef»>, «augmentTypeRef»> aug =((«augmentationHolderRef»<«typeRef»>) base).augmentations();
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

