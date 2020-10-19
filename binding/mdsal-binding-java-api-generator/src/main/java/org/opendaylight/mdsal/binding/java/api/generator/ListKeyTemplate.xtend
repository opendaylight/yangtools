/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import org.opendaylight.mdsal.binding.model.api.GeneratedProperty
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject

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
        public «field.returnType.importedNonNull» «field.getterMethodName»() {
            return «field.fieldName»«field.cloneCall»;
        }
    '''
}
