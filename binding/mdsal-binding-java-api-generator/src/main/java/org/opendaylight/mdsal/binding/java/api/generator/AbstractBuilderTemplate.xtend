/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.AUGMENTABLE_AUGMENTATION_NAME
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.AUGMENTATION_FIELD

import com.google.common.base.MoreObjects
import java.util.Collection
import java.util.Collections
import java.util.Map
import java.util.Set
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty
import org.opendaylight.mdsal.binding.model.api.GeneratedType
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.yangtools.yang.binding.CodeHelpers
import java.util.ArrayList
import org.opendaylight.mdsal.binding.model.util.Types
import org.opendaylight.yangtools.yang.binding.Identifiable
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping
import com.google.common.collect.ImmutableMap
import org.opendaylight.yangtools.yang.binding.AugmentationHolder
import java.util.HashMap

abstract class AbstractBuilderTemplate extends BaseTemplate {
    /**
     * Generated property is set if among methods is found one with the name GET_AUGMENTATION_METHOD_NAME.
     */
    protected val Type augmentType

    /**
     * Set of class attributes (fields) which are derived from the getter methods names.
     */
    protected val Set<GeneratedProperty> properties

    /**
     * GeneratedType for key type, null if this type does not have a key.
     */
    protected val Type keyType

    protected val GeneratedType targetType;

    new(AbstractJavaGeneratedType javaType, GeneratedType type, GeneratedType targetType,
            Set<GeneratedProperty> properties, Type augmentType, Type keyType) {
        super(javaType, type)
        this.targetType = targetType
        this.properties = properties
        this.augmentType = augmentType
        this.keyType = keyType
    }

    new(GeneratedType type, GeneratedType targetType, Set<GeneratedProperty> properties, Type augmentType,
            Type keyType) {
        super(type)
        this.targetType = targetType
        this.properties = properties
        this.augmentType = augmentType
        this.keyType = keyType
    }

    /**
     * Template method which generates class attributes.
     *
     * @param makeFinal value which specify whether field is|isn't final
     * @return string with class attributes and their types
     */
    def generateFields(boolean makeFinal) '''
        «IF properties !== null»
            «FOR f : properties»
                private«IF makeFinal» final«ENDIF» «f.returnType.importedName» «f.fieldName»;
            «ENDFOR»
        «ENDIF»
        «IF keyType !== null»
            private«IF makeFinal» final«ENDIF» «keyType.importedName» key;
        «ENDIF»
    '''

    def generateAugmentField(boolean isPrivate) '''
        «IF augmentType !== null»
            «IF isPrivate»private «ENDIF»«Map.importedName»<«Class.importedName»<? extends «augmentType.importedName»>, «augmentType.importedName»> «AUGMENTATION_FIELD» = «Collections.importedName».emptyMap();
        «ENDIF»
    '''

    override generateToString(Collection<GeneratedProperty> properties) '''
        «IF properties !== null»
            @«Override.importedName»
            public «String.importedName» toString() {
                final «MoreObjects.importedName».ToStringHelper helper = «MoreObjects.importedName».toStringHelper("«targetType.name»");
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

    /**
     * Template method which generate getter methods for IMPL class.
     *
     * @return string with getter methods
     */
    def generateGetters(boolean addOverride) '''
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

    def CharSequence generateCopyConstructor(boolean impl, Type fromType, Type implType) '''
        «IF impl»private«ELSE»public«ENDIF» «type.name»(«fromType.importedName» base) {
            «val allProps = new ArrayList(properties)»
            «val isList = implementsIfc(targetType, Types.parameterizedTypeFor(Types.typeForClass(Identifiable), targetType))»
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
                    if (base instanceof «implType.importedName») {
                        «implType.importedName» impl = («implType.importedName») base;
                        if (!impl.«AUGMENTATION_FIELD».isEmpty()) {
                            this.«AUGMENTATION_FIELD» = new «HashMap.importedName»<>(impl.«AUGMENTATION_FIELD»);
                        }
                    } else if (base instanceof «AugmentationHolder.importedName») {
                        @SuppressWarnings("unchecked")
                        «AugmentationHolder.importedName»<«fromType.importedName»> casted =(«AugmentationHolder.importedName»<«fromType.importedName»>) base;
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
}