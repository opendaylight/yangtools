/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen

import java.util.List
import org.opendaylight.yangtools.binding.model.api.AnnotationType
import org.opendaylight.yangtools.binding.model.api.GeneratedType

/**
 * Template for generating JAVA interfaces.
 */
 // FIXME: YANGTOOLS-1805: convert to Java
package class InterfaceTemplate extends AbstractInterfaceTemplate {
    /**
     * Creates the instance of this class which is used for generating the interface file source
     * code from <code>genType</code>.
     *
     * @throws NullPointerException if <code>genType</code> is <code>null</code>
     */
    new(GeneratedType genType) {
        super(genType)
    }

    /**
     * Template method which generate the whole body of the interface.
     *
     * @return string with code for interface body in JAVA format
     */
    override body() '''
        «type.formatDataForJavaDoc.wrapToDocumentation»
        «type.annotations.generateAnnotations»
        «generatedAnnotation»
        public interface «type.simpleName»
            «superInterfaces»
        {

            «generateInnerClasses»

            «generateInnerEnumTypeObjects(enums)»

            «generateConstants»

            «generateMethods»

        }

    '''

    def private generateAnnotations(List<AnnotationType> annotations) '''
        «IF annotations !== null && !annotations.empty»
            «FOR annotation : annotations»
                «annotation.generateAnnotation»
            «ENDFOR»
        «ENDIF»
    '''

    /**
     * Template method which generates the interface name declaration.
     *
     * @return string with the code for the interface declaration in JAVA format
     */
    def private superInterfaces()
    '''
    «IF (!type.implements.empty)»
         extends
         «FOR type : type.implements SEPARATOR ","»
             «type.importedName»
         «ENDFOR»
     « ENDIF»
     '''

    /**
     * Template method which generates inner classes inside this interface.
     *
     * @return string with the source code for inner classes in JAVA format
     */
    def private generateInnerClasses() '''
        «IF !enclosedGeneratedTypes.empty»
            «FOR innerClass : enclosedGeneratedTypes SEPARATOR "\n"»
                «generateInnerClass(innerClass)»
            «ENDFOR»
        «ENDIF»
    '''
}
