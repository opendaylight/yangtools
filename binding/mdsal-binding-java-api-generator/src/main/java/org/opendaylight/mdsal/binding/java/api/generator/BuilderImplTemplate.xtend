/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static org.opendaylight.mdsal.binding.model.util.Types.STRING;
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.AUGMENTATION_FIELD
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.BINDING_EQUALS_NAME
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.BINDING_HASHCODE_NAME
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.BINDING_TO_STRING_NAME

import java.util.Collection
import java.util.List
import org.opendaylight.mdsal.binding.model.api.AnnotationType
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty
import org.opendaylight.mdsal.binding.model.api.GeneratedType
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.mdsal.binding.model.util.Types
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping
import org.opendaylight.yangtools.yang.binding.AbstractAugmentable
import org.opendaylight.mdsal.binding.model.api.MethodSignature.ValueMechanics

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

            «generateToString()»
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

                final int result = «targetType.importedName».«BINDING_HASHCODE_NAME»(this);
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
                return «targetType.importedName».«BINDING_EQUALS_NAME»(this, obj);
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
            return «targetType.importedName».«BINDING_TO_STRING_NAME»(this);
        }
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
        super(base.«AUGMENTATION_FIELD»);
    '''
}