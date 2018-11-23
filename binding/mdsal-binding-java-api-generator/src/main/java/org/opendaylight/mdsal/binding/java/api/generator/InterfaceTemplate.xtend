/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static extension org.opendaylight.mdsal.binding.spec.naming.BindingMapping.DATA_CONTAINER_IMPLEMENTED_INTERFACE_NAME
import static extension org.opendaylight.mdsal.binding.spec.naming.BindingMapping.getGetterMethodForNonnull
import static extension org.opendaylight.mdsal.binding.spec.naming.BindingMapping.isGetterMethodName
import static extension org.opendaylight.mdsal.binding.spec.naming.BindingMapping.isNonnullMethodName

import java.util.List
import org.opendaylight.mdsal.binding.model.api.AnnotationType
import org.opendaylight.mdsal.binding.model.api.Constant
import org.opendaylight.mdsal.binding.model.api.Enumeration
import org.opendaylight.mdsal.binding.model.api.GeneratedType
import org.opendaylight.mdsal.binding.model.api.JavaTypeName
import org.opendaylight.mdsal.binding.model.api.MethodSignature
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.mdsal.binding.model.util.TypeConstants
import org.opendaylight.yangtools.yang.binding.CodeHelpers

/**
 * Template for generating JAVA interfaces.
 */
class InterfaceTemplate extends BaseTemplate {
    static val JavaTypeName NONNULL = JavaTypeName.create("org.eclipse.jdt.annotation", "NonNull")
    static val JavaTypeName NULLABLE = JavaTypeName.create("org.eclipse.jdt.annotation", "Nullable")

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
        if (genType === null) {
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
        «IF annotations !== null && !annotations.empty»
            «FOR annotation : annotations»
                @«annotation.importedName»
                «IF annotation.parameters !== null && !annotation.parameters.empty»
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
                «generateInnerClass(innerClass)»
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
                «val enumTemplate = new EnumTemplate(javaType.getEnclosedType(e.identifier), e)»
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
                «IF !c.name.startsWith(TypeConstants.PATTERN_CONSTANT_NAME)»
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
                «IF m.isDefault»
                    «generateDefaultMethod(m)»
                «ELSEIF m.parameters.empty && m.name.isGetterMethodName»
                    «generateAccessorMethod(m)»
                «ELSE»
                    «generateMethod(m)»
                «ENDIF»
            «ENDFOR»
        «ENDIF»
    '''

    def private generateDefaultMethod(MethodSignature method) {
        if (method.name.isNonnullMethodName) {
            generateNonnullMethod(method)
        } else {
            switch method.name {
                case DATA_CONTAINER_IMPLEMENTED_INTERFACE_NAME : generateDefaultImplementedInterface
            }
        }
    }

    def private generateMethod(MethodSignature method) '''
        «method.comment.asJavadoc»
        «method.annotations.generateAnnotations»
        «method.returnType.importedName» «method.name»(«method.parameters.generateParameters»);
    '''

    def private generateAccessorMethod(MethodSignature method) '''
        «val ret = method.returnType»
        «formatDataForJavaDoc(method, "@return " + asCode(ret.fullyQualifiedName) + " " + asCode(propertyNameFromGetter(method)) + ", or " + asCode("null") + " if not present")»
        «method.annotations.generateAnnotations»
        «nullableType(ret)» «method.name»();
    '''

    def private generateDefaultImplementedInterface() '''
        @«Override.importedName»
        default «Class.importedName»<«type.fullyQualifiedName»> «DATA_CONTAINER_IMPLEMENTED_INTERFACE_NAME»() {
            return «type.fullyQualifiedName».class;
        }
    '''

    def private generateNonnullMethod(MethodSignature method) '''
        «val ret = method.returnType»
        «val name = method.name»
        «formatDataForJavaDoc(method, "@return " + asCode(ret.fullyQualifiedName) + " " + asCode(propertyNameFromGetter(method)) + ", or an empty list if it is not present")»
        «method.annotations.generateAnnotations»
        default «ret.importedName(NONNULL.importedName)» «name»() {
            return «CodeHelpers.importedName».nonnull(«getGetterMethodForNonnull(name)»());
        }
    '''

    def private String nullableType(Type type) {
        if (type.isObject) {
            return type.importedName(NULLABLE.importedName)
        }
        return type.importedName
    }

    def private static boolean isObject(Type type) {
        // The return type has a package, so it's not a primitive type
        return !type.getPackageName().isEmpty()
    }
}
