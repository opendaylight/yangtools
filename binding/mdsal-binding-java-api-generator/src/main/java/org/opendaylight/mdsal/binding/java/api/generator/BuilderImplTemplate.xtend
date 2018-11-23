/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.AUGMENTATION_FIELD
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.AUGMENTABLE_AUGMENTATION_NAME
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.DATA_CONTAINER_IMPLEMENTED_INTERFACE_NAME

import com.google.common.collect.ImmutableMap
import java.util.List
import java.util.Map
import java.util.Objects
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty
import org.opendaylight.mdsal.binding.model.api.GeneratedType
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping
import org.opendaylight.yangtools.yang.binding.DataObject

class BuilderImplTemplate extends AbstractBuilderTemplate {
    val Type builderType;

    new(BuilderTemplate builder, GeneratedType type) {
        super(builder.javaType.getEnclosedType(type.identifier), type, builder.targetType, builder.properties,
            builder.augmentType, builder.keyType)
        this.builderType = builder.type
    }

    override body() '''
        private static final class «type.name» implements «targetType.importedName» {

            «generateFields(true)»

            «IF augmentType !== null»
                private «generateAugmentField()»
            «ENDIF»

            «generateCopyConstructor(builderType, type)»

            @«Deprecated.importedName»
            @«Override.importedName»
            public «Class.importedName»<«targetType.importedName»> getImplementedInterface() {
                return «targetType.importedName».class;
            }

            «generateGetters(true)»

            «generateHashCode()»

            «generateEquals()»

            «generateToString(properties)»
        }
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

                «hashCodeResult(properties)»
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
                if (!«targetType.importedName».class.equals(((«DataObject.importedName»)obj).«DATA_CONTAINER_IMPLEMENTED_INTERFACE_NAME»())) {
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

    override protected generateCopyAugmentation(Type implType) '''
        this.«AUGMENTATION_FIELD» = «ImmutableMap.importedName».copyOf(base.«AUGMENTATION_FIELD»);
    '''
}