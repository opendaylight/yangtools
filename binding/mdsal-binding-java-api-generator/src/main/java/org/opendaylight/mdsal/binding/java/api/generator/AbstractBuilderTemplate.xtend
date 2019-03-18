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
import java.util.ArrayList
import java.util.Collection
import java.util.Collections
import java.util.Comparator
import java.util.List
import java.util.Map
import java.util.Set
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject
import org.opendaylight.mdsal.binding.model.api.GeneratedType
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.mdsal.binding.model.util.Types
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping
import org.opendaylight.yangtools.yang.binding.CodeHelpers
import org.opendaylight.yangtools.yang.binding.Identifiable

abstract class AbstractBuilderTemplate extends BaseTemplate {
    static val Comparator<GeneratedProperty> KEY_PROPS_COMPARATOR = [ p1, p2 | return p1.name.compareTo(p2.name) ]

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
    def protected final generateFields(boolean makeFinal) '''
        «IF properties !== null»
            «FOR f : properties»
                private«IF makeFinal» final«ENDIF» «f.returnType.importedName» «f.fieldName»;
            «ENDFOR»
        «ENDIF»
        «IF keyType !== null»
            private«IF makeFinal» final«ENDIF» «keyType.importedName» key;
        «ENDIF»
    '''

    def protected final generateAugmentField() {
        val augmentTypeRef = augmentType.importedName
        return '''
           «Map.importedName»<«Class.importedName»<? extends «augmentTypeRef»>, «augmentTypeRef»> «AUGMENTATION_FIELD» = «Collections.importedName».emptyMap();
        '''
    }

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
    def final generateGetters(boolean addOverride) '''
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

            @«SuppressWarnings.importedName»({ "unchecked", "checkstyle:methodTypeParameterName"})
            «IF addOverride»@«Override.importedName»«ENDIF»
            public <E$$ extends «augmentType.importedName»> E$$ «AUGMENTABLE_AUGMENTATION_NAME»(«Class.importedName»<E$$> augmentationType) {
                return (E$$) «AUGMENTATION_FIELD».get(«CodeHelpers.importedName».nonNullValue(augmentationType, "augmentationType"));
            }
        «ENDIF»
    '''

    def protected final CharSequence generateCopyConstructor(Type fromType, Type implType) '''
        «type.name»(«fromType.importedName» base) {
            «val allProps = new ArrayList(properties)»
            «val isList = implementsIfc(targetType, Types.parameterizedTypeFor(Types.typeForClass(Identifiable), targetType))»
            «IF isList && keyType !== null»
                «val keyProps = new ArrayList((keyType as GeneratedTransferObject).properties)»
                «keyProps.sort(KEY_PROPS_COMPARATOR)»
                «FOR field : keyProps»
                    «removeProperty(allProps, field.name)»
                «ENDFOR»
                «generateCopyKeys(keyProps)»
            «ENDIF»
            «FOR field : allProps»
                this.«field.fieldName» = base.«field.getterMethodName»();
            «ENDFOR»
            «IF augmentType !== null»
                «generateCopyAugmentation(implType)»
            «ENDIF»
        }
    '''

    def protected abstract CharSequence generateCopyKeys(List<GeneratedProperty> keyProps)

    def protected abstract CharSequence generateCopyAugmentation(Type implType);

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
