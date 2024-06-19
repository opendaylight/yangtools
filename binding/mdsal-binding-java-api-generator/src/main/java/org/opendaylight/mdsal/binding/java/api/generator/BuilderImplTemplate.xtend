/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static org.opendaylight.mdsal.binding.model.util.BindingTypes.DATA_OBJECT
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.AUGMENTATION_FIELD
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.AUGMENTABLE_AUGMENTATION_NAME
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.DATA_CONTAINER_IMPLEMENTED_INTERFACE_NAME

import java.util.Collection
import java.util.List
import org.opendaylight.mdsal.binding.model.api.AnnotationType
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty
import org.opendaylight.mdsal.binding.model.api.GeneratedType
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.mdsal.binding.model.util.Types
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping
import org.opendaylight.yangtools.yang.binding.AbstractAugmentable

class BuilderImplTemplate extends AbstractBuilderTemplate {
    val Type builderType;

    new(BuilderTemplate builder, GeneratedType type) {
        super(builder.javaType.getEnclosedType(type.identifier), type, builder.targetType, builder.properties,
            builder.augmentType, builder.keyType)
        this.builderType = builder.type
    }

    override body() '''
        «targetType.annotations.generateDeprecatedAnnotation»
        private static final class «type.name»
            «val impIface = targetType.importedName»
            «IF augmentType !== null»
                extends «AbstractAugmentable.importedName»<«impIface»>
            «ENDIF»
            implements «impIface» {

            «generateFields(true)»

            «generateCopyConstructor(builderType, type)»

            «generateGetters(true)»

            «generateHashCode()»

            «generateEquals()»

            «generateToString(properties)»
        }
    '''

    override generateDeprecatedAnnotation(AnnotationType ann) {
        return generateAnnotation(ann)
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

                «hashCodeResult(properties)»
                «IF augmentType !== null»
                    result = prime * result + «JU_OBJECTS.importedName».hashCode(augmentations());
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
            @«OVERRIDE.importedName»
            public boolean equals(«Types.objectType().importedName» obj) {
                if (this == obj) {
                    return true;
                }
                if (!(obj instanceof «DATA_OBJECT.importedName»)) {
                    return false;
                }
                if (!«targetType.importedName».class.equals(((«DATA_OBJECT.importedName»)obj).«DATA_CONTAINER_IMPLEMENTED_INTERFACE_NAME»())) {
                    return false;
                }
                «targetType.importedName» other = («targetType.importedName»)obj;
                «FOR property : properties»
                    «val fieldName = property.fieldName»
                    if (!«property.importedUtilClass».equals(«fieldName», other.«property.getterMethodName»())) {
                        return false;
                    }
                «ENDFOR»
                «IF augmentType !== null»
                    if (getClass() == obj.getClass()) {
                        // Simple case: we are comparing against self
                        «type.name» otherImpl = («type.name») obj;
                        if (!«JU_OBJECTS.importedName».equals(augmentations(), otherImpl.augmentations())) {
                            return false;
                        }
                    } else {
                        // Hard case: compare our augments with presence there...
                        for («JU_MAP.importedName».Entry<«CLASS.importedName»<? extends «augmentType.importedName»>, «augmentType.importedName»> e : augmentations().entrySet()) {
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

    override protected generateCopyKeys(List<GeneratedProperty> keyProps) '''
        if (base.«BindingMapping.IDENTIFIABLE_KEY_NAME»() != null) {
            this.key = base.«BindingMapping.IDENTIFIABLE_KEY_NAME»();
        } else {
            this.key = new «keyType.importedName»(«FOR keyProp : keyProps SEPARATOR ", "»base.«keyProp.getterMethodName»()«ENDFOR»);
        }
        «FOR field : keyProps»
            this.«field.fieldName» = key.«field.getterMethodName»();
        «ENDFOR»
    '''

    override protected  CharSequence generateCopyNonKeys(Collection<GeneratedProperty> props) '''
        «FOR field : props»
            «IF field.nullifyEmpty»
                this.«field.fieldName» = «CODEHELPERS.importedName».emptyToNull(base.«field.getterMethodName»());
            «ELSE»
                this.«field.fieldName» = base.«field.getterMethodName»();
            «ENDIF»
        «ENDFOR»
    '''

    override protected generateCopyAugmentation(Type implType) '''
        super(base.«AUGMENTATION_FIELD»);
    '''
}