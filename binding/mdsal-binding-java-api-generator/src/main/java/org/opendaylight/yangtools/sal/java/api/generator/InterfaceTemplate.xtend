/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator

import java.util.List
import org.opendaylight.yangtools.binding.generator.util.TypeConstants
import org.opendaylight.yangtools.sal.binding.model.api.Constant
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature
import org.opendaylight.yangtools.sal.binding.model.api.AnnotationType

/**
 * Template for generating JAVA interfaces.
 */
class InterfaceTemplate extends BaseTemplate {

    /**
     * List of constant instances which are generated as JAVA public static final attributes.
     */
    val List<Constant> consts

    /**
     * List of method signatures which are generated as method declarations.
     */
    val List<MethodSignature> methods

    /**
     * List of enumeration which are generated as JAVA enum type.
     */
    val List<Enumeration> enums

    /**
     * List of generated types which are enclosed inside <code>genType</code>
     */
    val List<GeneratedType> enclosedGeneratedTypes

    /**
     * Creates the instance of this class which is used for generating the interface file source
     * code from <code>genType</code>.
     *
     * @throws IllegalArgumentException if <code>genType</code> equals <code>null</code>
     */
    new(GeneratedType genType) {
        super(genType)
        if (genType == null) {
            throw new IllegalArgumentException("Generated type reference cannot be NULL!")
        }

        consts = genType.constantDefinitions
        methods = genType.methodDefinitions
        enums = genType.enumerations
        enclosedGeneratedTypes = genType.enclosedTypes
    }

    /**
     * Template method which generate the whole body of the interface.
     *
     * @return string with code for interface body in JAVA format
     */
    override body() '''
        «wrapToDocumentation(formatDataForJavaDoc(type))»
        «type.annotations.generateAnnotations»
        public interface «type.name»
            «superInterfaces»
        {

            «generateInnerClasses»

            «generateEnums»

            «generateConstants»

            «generateMethods»

        }

    '''


    def private generateAnnotations(List<AnnotationType> annotations) '''
        «IF annotations != null && !annotations.empty»
            «FOR annotation : annotations»
                @«annotation.importedName»
                «IF annotation.parameters != null && !annotation.parameters.empty»
                (
                «FOR param : annotation.parameters SEPARATOR ","»
                    «param.name»=«param.value»
                «ENDFOR»
                )
                «ENDIF»
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
                «IF (innerClass instanceof GeneratedTransferObject)»
                    «IF innerClass.unionType»
                        «val unionTemplate = new UnionTemplate(innerClass)»
                        «unionTemplate.generateAsInnerClass»
                        «this.importMap.putAll(unionTemplate.importMap)»
                    «ELSE»
                        «val classTemplate = new ClassTemplate(innerClass)»
                        «classTemplate.generateAsInnerClass»
                        «this.importMap.putAll(classTemplate.importMap)»
                    «ENDIF»

                «ENDIF»
            «ENDFOR»
        «ENDIF»
    '''

    /**
     * Template method which generates JAVA enum type.
     *
     * @return string with inner enum source code in JAVA format
     */
    def private generateEnums() '''
        «IF !enums.empty»
            «FOR e : enums SEPARATOR "\n"»
                «val enumTemplate = new EnumTemplate(e)»
                «enumTemplate.generateAsInnerClass»
            «ENDFOR»
        «ENDIF»
    '''

    /**
     * Template method wich generates JAVA constants.
     *
     * @return string with constants in JAVA format
     */
    def private generateConstants() '''
        «IF !consts.empty»
            «FOR c : consts»
                «IF c.name != TypeConstants.PATTERN_CONSTANT_NAME»
                    «emitConstant(c)»
                «ENDIF»
            «ENDFOR»
        «ENDIF»
    '''

    /**
     * Template method which generates the declaration of the methods.
     *
     * @return string with the declaration of methods source code in JAVA format
     */
    def private generateMethods() '''
        «IF !methods.empty»
            «FOR m : methods SEPARATOR "\n"»
                «IF !m.isAccessor»
                    «m.comment.asJavadoc»
                «ELSE»
                    «formatDataForJavaDoc(m, "@return " + asCode(m.returnType.fullyQualifiedName) + " "
                    + asCode(propertyNameFromGetter(m)) + ", or " + asCode("null") + " if not present")»
                «ENDIF»
                «m.annotations.generateAnnotations»
                «m.returnType.importedName» «m.name»(«m.parameters.generateParameters»);
            «ENDFOR»
        «ENDIF»
    '''

}

