/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject
import org.opendaylight.mdsal.binding.model.api.AccessModifier

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

    override body() '''
        «wrapToDocumentation(formatDataForJavaDoc(type, getClarification()))»
        public class «type.name» {
            private «type.name»() {
                //Exists only to defeat instantiation.
            }

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

    def private String getClarification() {
        return
        '''
        The purpose of generated class in src/main/java for Union types is to create new instances of unions from a string representation.
        In some cases it is very difficult to automate it since there can be unions such as (uint32 - uint16), or (string - uint32).

        The reason behind putting it under src/main/java is:
        This class is generated in form of a stub and needs to be finished by the user. This class is generated only once to prevent
        loss of user code.
        '''
    }

}
