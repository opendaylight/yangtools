/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator

import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import org.opendaylight.yangtools.sal.binding.model.api.AccessModifier

/**
 * Template for generating JAVA class.
 */
class UnionBuilderTemplate extends ClassTemplate {

    /**
     * Creates instance of this class with concrete <code>genType</code>.
     *
     * @param genType generated transfer object which will be transformed to JAVA class source code
     */
    new(GeneratedTransferObject genType) {
        super(genType)
    }

    def override body() '''
        «wrapToDocumentation(formatDataForJavaDoc(type))»
        public class «type.name» {

            «generateMethods»

        }
    '''

    def private generateMethods() '''
        «FOR method : genTO.methodDefinitions»
            «method.accessModifier.accessModifier»«IF method.static»static«ENDIF»«IF method.final» final«ENDIF» «method.
            returnType.importedName» «method.name»(«method.parameters.generateParameters») {
                throw new «UnsupportedOperationException.importedName»("Not yet implemented");
            }
        «ENDFOR»
    '''

    def private String getAccessModifier(AccessModifier modifier) {
        switch (modifier) {
            case AccessModifier.PUBLIC: return "public "
            case AccessModifier.PROTECTED: return "protected "
            case AccessModifier.PRIVATE: return "private "
            default: return ""
        }
    }

}
