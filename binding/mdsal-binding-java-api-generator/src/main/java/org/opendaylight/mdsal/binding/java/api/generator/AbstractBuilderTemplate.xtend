/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import java.util.ArrayList
import java.util.Collection
import java.util.Comparator
import java.util.List
import java.util.Set
import org.opendaylight.mdsal.binding.model.api.AnnotationType
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject
import org.opendaylight.mdsal.binding.model.api.GeneratedType
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.mdsal.binding.model.ri.BindingTypes
import org.opendaylight.yangtools.yang.binding.contract.Naming

abstract class AbstractBuilderTemplate extends BaseTemplate {
    static val Comparator<GeneratedProperty> KEY_PROPS_COMPARATOR = [ p1, p2 | return p1.name.compareTo(p2.name) ]

    /**
     * Generated property is set if among methods is found one with the name GET_AUGMENTATION_METHOD_NAME.
     */
    protected val Type augmentType

    /**
     * Set of class attributes (fields) which are derived from the getter methods names.
     */
    protected val Set<BuilderGeneratedProperty> properties

    /**
     * GeneratedType for key type, null if this type does not have a key.
     */
    protected val Type keyType

    protected val GeneratedType targetType;

    new(AbstractJavaGeneratedType javaType, GeneratedType type, GeneratedType targetType,
            Set<BuilderGeneratedProperty> properties, Type augmentType, Type keyType) {
        super(javaType, type)
        this.targetType = targetType
        this.properties = properties
        this.augmentType = augmentType
        this.keyType = keyType
    }

    new(GeneratedType type, GeneratedType targetType, Type keyType) {
        super(type)
        this.targetType = targetType
        this.keyType = keyType

        val analysis = analyzeTypeHierarchy(targetType)
        augmentType = analysis.key
        properties = analysis.value
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

    /**
     * Template method which generate getter methods for IMPL class.
     *
     * @return string with getter methods
     */
    def final generateGetters(boolean addOverride) '''
        «IF keyType !== null»
            «IF addOverride»
            @«OVERRIDE.importedName»
            «ELSE»
            /**
             * Return current value associated with the property corresponding to {@link «targetType.importedName»#«Naming.IDENTIFIABLE_KEY_NAME»()}.
             *
             * @return current value
             */
            «ENDIF»
            public «keyType.importedName» «Naming.IDENTIFIABLE_KEY_NAME»() {
                return key;
            }

        «ENDIF»
        «IF !properties.empty»
            «FOR field : properties SEPARATOR '\n'»
                «IF addOverride»
                @«OVERRIDE.importedName»
                «ELSE»
                /**
                 * Return current value associated with the property corresponding to {@link «targetType.importedName»#«field.getterName»()}.
                 *
                 * @return current value
                 */
                «ENDIF»
                «field.getterMethod»
            «ENDFOR»
        «ENDIF»
    '''

    def protected final CharSequence generateCopyConstructor(Type fromType, Type implType) '''
        «type.name»(«fromType.importedName» base) {
            «IF augmentType !== null»
                «generateCopyAugmentation(implType)»
            «ENDIF»
            «IF keyType !== null && implementsIfc(targetType, BindingTypes.identifiable(targetType))»
                «val keyProps = new ArrayList((keyType as GeneratedTransferObject).properties)»
                «keyProps.sort(KEY_PROPS_COMPARATOR)»
                «val allProps = new ArrayList(properties)»
                «FOR field : keyProps»
                    «removeProperty(allProps, field.name)»
                «ENDFOR»
                «generateCopyKeys(keyProps)»
                «generateCopyNonKeys(allProps)»
            «ELSE»
                «generateCopyNonKeys(properties)»
            «ENDIF»
        }
    '''

    def protected final CharSequence generateDeprecatedAnnotation(List<AnnotationType> annotations) {
        var AnnotationType found = annotations.findDeprecatedAnnotation
        if (found === null) {
            return ""
        }
        return generateDeprecatedAnnotation(found)
    }

    def protected abstract CharSequence generateCopyKeys(List<GeneratedProperty> keyProps)

    def protected abstract CharSequence generateCopyNonKeys(Collection<BuilderGeneratedProperty> props)

    def protected abstract CharSequence generateCopyAugmentation(Type implType)

    def protected abstract CharSequence generateDeprecatedAnnotation(AnnotationType ann)

    private def boolean implementsIfc(GeneratedType type, Type impl) {
        for (Type ifc : type.implements) {
            if (ifc.equals(impl)) {
                return true;
            }
        }
        return false;
    }

    private def void removeProperty(Collection<BuilderGeneratedProperty> props, String name) {
        val iter = props.iterator
        while (iter.hasNext) {
            if (name.equals(iter.next.name)) {
                iter.remove
                return
            }
        }
    }

    private static def findDeprecatedAnnotation(List<AnnotationType> annotations) {
        if (annotations !== null) {
            for (annotation : annotations) {
                if (DEPRECATED.equals(annotation.identifier)) {
                    return annotation
                }
            }
        }
        return null
    }

    package static def hasNonDefaultMethods(GeneratedType type) {
        !type.methodDefinitions.isEmpty && type.methodDefinitions.exists([def | !def.isDefault])
    }

    package static def nonDefaultMethods(GeneratedType type) {
        type.methodDefinitions.filter([def | !def.isDefault])
    }
}
