/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen

import static extension org.opendaylight.yangtools.binding.codegen.GeneratorUtil.isNonPresenceContainer;
import static org.opendaylight.yangtools.binding.model.ri.Types.STRING;
import static org.opendaylight.yangtools.binding.contract.Naming.AUGMENTATION_FIELD
import static org.opendaylight.yangtools.binding.contract.Naming.BUILDER_SUFFIX
import static org.opendaylight.yangtools.binding.contract.Naming.HCETS_STATIC_FIELD_NAME
import static org.opendaylight.yangtools.binding.contract.Naming.KEY_AWARE_KEY_NAME
import static org.opendaylight.yangtools.binding.contract.Naming.NONNULL_PREFIX

import java.util.Collection
import java.util.List
import java.util.Optional
import org.opendaylight.yangtools.binding.lib.AbstractAugmentable
import org.opendaylight.yangtools.binding.lib.AbstractEntryObject
import org.opendaylight.yangtools.binding.model.api.AnnotationType
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.binding.model.api.GeneratedType
import org.opendaylight.yangtools.binding.model.api.JavaTypeName
import org.opendaylight.yangtools.binding.model.api.MethodSignature
import org.opendaylight.yangtools.binding.model.api.MethodSignature.ValueMechanics
import org.opendaylight.yangtools.binding.model.api.Type
import org.opendaylight.yangtools.binding.model.ri.Types

class BuilderImplTemplate extends AbstractBuilderTemplate {
    /**
     * {@code AbstractAugmentable} as a JavaTypeName.
     */
    static val ABSTRACT_AUGMENTABLE = JavaTypeName.create(AbstractAugmentable)
    /**
     * {@code AbstractAugmentable} as a JavaTypeName.
     */
    static val ABSTRACT_ENTRY_OBJECT = JavaTypeName.create(AbstractEntryObject)

    val BuilderTemplate builder;

    new(BuilderTemplate builder, GeneratedType type) {
        super(builder.javaType.getEnclosedType(type.identifier), type, builder.targetType, builder.properties,
            builder.augmentType, builder.keyType)
        this.builder = builder
    }

    override body() '''
        «targetType.annotations.generateDeprecatedAnnotation»
        private static final class «type.name»
            «val impIface = targetType.importedName»
            «IF keyType !== null»
                extends «ABSTRACT_ENTRY_OBJECT.importedName»<«impIface», «keyType.importedName»>
            «ELSEIF augmentType !== null»
                extends «ABSTRACT_AUGMENTABLE.importedName»<«impIface»>
            «ENDIF»
            implements «impIface» {

            «generateFields(true)»

            «generateCopyConstructor(builder.type, type)»
            «generateExtractKey»

            «generateGetters()»

            «generateNonnullGetters()»

            «generateHashCode()»

            «generateEquals()»

            «generateToString()»
        }
    '''

    // TODO: this is generating a utility static method for use in the (only) constructor. We should be inlining this
    //       code into the constructor once JEP-482 Flexible Constructor Bodies available. We should construct the key
    //       into a 'key' local variable, so that generateCopyKeys() below can reference it
    def private generateExtractKey() '''
        «IF keyType !== null»

            private static «keyType.importedNonNull» extractKey(final «builder.type.importedName» base) {
                «val keyProps = keyConstructorArgs(keyType)»
                final var key = base.«KEY_AWARE_KEY_NAME»();
                return key != null ? key
                    : new «keyType.importedName»(«FOR keyProp : keyProps SEPARATOR ", "»base.«keyProp.getterMethodName»()«ENDFOR»);
            }
        «ENDIF»
    '''

    override generateDeprecatedAnnotation(AnnotationType ann) {
        return generateAnnotation(ann)
    }

    def private generateGetters() '''
        «IF !properties.empty»
            «FOR field : properties SEPARATOR '\n'»
                «field.getterMethod»
            «ENDFOR»
        «ENDIF»
    '''

    def private generateNonnullGetters() '''
        «IF !properties.empty»
            «FOR field : properties SEPARATOR '\n'»
                «IF field.returnType instanceof GeneratedType»
                    «IF isNonPresenceContainer(field.returnType as GeneratedType)»
                        «field.nonNullGetterMethod»
                    «ENDIF»
                «ENDIF»
            «ENDFOR»
        «ENDIF»
    '''

    private static def Optional<MethodSignature> findGetter(GeneratedType implType, String getterName) {
        val getter = getterByName(implType.nonDefaultMethods, getterName);
        if (getter.isPresent) {
            return getter;
        }
        for (ifc : implType.implements) {
            if (ifc instanceof GeneratedType) {
                val getterImpl = findGetter(ifc, getterName)
                if (getterImpl.isPresent) {
                    return (getterImpl)
                }
            }
        }
        return Optional.empty
    }

    override getterMethod(GeneratedProperty field) '''
        @«OVERRIDE.importedName»
        public «field.returnType.importedName» «field.getterMethodName»() {
            «val fieldName = field.fieldName»
            «IF field.returnType.name.endsWith("[]")»
                return «fieldName» == null ? null : «fieldName».clone();
            «ELSE»
                return «fieldName»;
            «ENDIF»
        }
    '''

    def private nonNullGetterMethod(GeneratedProperty field) '''
        @«OVERRIDE.importedName»
        «val type = field.returnType»
        public «type.importedName» «field.nonnullMethodName»() {
            return «JU_OBJECTS.importedName».requireNonNullElse(«field.getterMethodName»(), «type.fullyQualifiedName»«BUILDER_SUFFIX».empty());
        }
    '''

    def private nonnullMethodName(GeneratedProperty field) {
        return '''«NONNULL_PREFIX»«field.name.toFirstUpper»'''
    }

    package def findGetter(String getterName) {
        val ownGetter = getterByName(type.nonDefaultMethods, getterName);
        if (ownGetter.isPresent) {
            return ownGetter.orElseThrow
        }
        for (ifc : type.implements) {
            if (ifc instanceof GeneratedType) {
                val getter = findGetter(ifc, getterName)
                if (getter.isPresent) {
                    return (getter.orElseThrow)
                }
            }
        }
        throw new IllegalStateException(
                String.format("%s should be present in %s type or in one of its ancestors as getter",
                        getterName.propertyNameFromGetter, type));
    }

    /**
     * Template method which generates the method <code>hashCode()</code>.
     *
     * @return string with the <code>hashCode()</code> method definition in JAVA format
     */
    def protected generateHashCode() '''
        «IF !properties.empty || augmentType !== null»
            private int hash = 0;
            private volatile boolean hashValid = false;

            @«OVERRIDE.importedName»
            public int hashCode() {
                if (hashValid) {
                    return hash;
                }

                final int result = «targetType.importedName».«HCETS_STATIC_FIELD_NAME».hashCode(this);
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
            @«OVERRIDE.importedName»
            public boolean equals(«Types.objectType().importedName» obj) {
                return «targetType.importedName».«HCETS_STATIC_FIELD_NAME».bindingEquals(this, obj);
            }
        «ENDIF»
    '''

    /**
     * Template method which generates the method <code>toString()</code>.
     *
     * @return string with the <code>toString()</code> method definition in JAVA format
     */
    def protected generateToString() '''
        @«OVERRIDE.importedName»
        public «STRING.importedName» toString() {
            return «targetType.importedName».«HCETS_STATIC_FIELD_NAME».bindingToString(this);
        }
    '''

    override protected generateCopyKeys(List<GeneratedProperty> keyProps) '''
        final var key = key();
        «FOR field : keyProps»
            this.«field.fieldName» = key.«field.getterMethodName»();
        «ENDFOR»
    '''

    override protected CharSequence generateCopyNonKeys(Collection<BuilderGeneratedProperty> props) '''
        «FOR field : props»
            «IF field.mechanics === ValueMechanics.NULLIFY_EMPTY»
                this.«field.fieldName» = «CODEHELPERS.importedName».emptyToNull(base.«field.getterName»());
            «ELSE»
                this.«field.fieldName» = base.«field.getterName»();
            «ENDIF»
        «ENDFOR»
    '''

    override protected generateCopyAugmentation(Type implType) '''
        «IF keyType !== null»
            super(base.«AUGMENTATION_FIELD», extractKey(base));
        «ELSE»
            super(base.«AUGMENTATION_FIELD»);
        «ENDIF»
    '''
}
