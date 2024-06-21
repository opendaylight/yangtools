/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen

import org.opendaylight.yangtools.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.binding.model.api.GeneratedType
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type
import org.opendaylight.yangtools.binding.model.ri.BindingTypes

/**
 * Template for generating JAVA class.
 */
final class ListKeyTemplate extends ClassTemplate {
    /**
     * Creates instance of this class with concrete <code>genType</code>.
     *
     * @param genType generated transfer object which will be transformed to JAVA class source code
     */
    new(GeneratedTransferObject genType) {
        super(genType)
    }

    override allValuesConstructor() '''
        /**
         * Constructs an instance.
         *
         «FOR p : allProperties»
            * @param «p.fieldName» the entity «p.getName»
         «ENDFOR»
         * @throws NullPointerException if any of the arguments are null
         */
        public «type.name»(«allProperties.asNonNullArgumentsDeclaration») {
            «FOR p : allProperties»
                «val fieldName = p.fieldName»
                this.«fieldName» = «CODEHELPERS.importedName».requireKeyProp(«fieldName», "«p.name»")«p.cloneCall»;
            «ENDFOR»
            «FOR p : properties»
                «generateRestrictions(type, p.fieldName, p.returnType)»
            «ENDFOR»
        }
    '''

    override getterMethod(GeneratedProperty field) '''
        /**
         * Return «field.getName», guaranteed to be non-null.
         *
         * @return {@code «field.returnType.importedName»} «field.getName», guaranteed to be non-null.
         */
        public «field.returnType.importedNonNull» «field.getterMethodName»() {
            return «field.fieldName»«field.cloneCall»;
        }
    '''

    override protected String formatDataForJavaDoc(GeneratedType type) {
        val listType = findListType(type)
        if (listType === null) {
            return ""
        }

        val importedName = listType.importedName
        return '''
            This class represents the key of {@link «importedName»} class.

            @see «importedName»
        '''
    }

    private static def Type findListType(GeneratedType type) {
        for (Type implType : type.getImplements()) {
            if (implType instanceof ParameterizedType) {
                val identifiable = BindingTypes.extractKeyType(implType)
                if (identifiable !== null) {
                    return identifiable
                }
            }
        }
        return null
    }
}
