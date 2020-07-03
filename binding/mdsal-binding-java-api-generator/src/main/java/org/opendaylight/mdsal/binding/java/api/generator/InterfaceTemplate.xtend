/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static extension org.opendaylight.mdsal.binding.spec.naming.BindingMapping.getGetterMethodForNonnull
import static extension org.opendaylight.mdsal.binding.spec.naming.BindingMapping.isGetterMethodName
import static extension org.opendaylight.mdsal.binding.spec.naming.BindingMapping.isNonnullMethodName
import static org.opendaylight.mdsal.binding.model.util.Types.STRING;
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.AUGMENTATION_FIELD
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.BINDING_EQUALS_NAME
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.BINDING_HASHCODE_NAME
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.BINDING_TO_STRING_NAME
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.DATA_CONTAINER_IMPLEMENTED_INTERFACE_NAME

import com.google.common.base.MoreObjects
import java.util.List
import java.util.Map.Entry
import java.util.Set
import org.gaul.modernizer_maven_annotations.SuppressModernizer
import org.opendaylight.mdsal.binding.model.api.AnnotationType
import org.opendaylight.mdsal.binding.model.api.Constant
import org.opendaylight.mdsal.binding.model.api.Enumeration
import org.opendaylight.mdsal.binding.model.api.GeneratedType
import org.opendaylight.mdsal.binding.model.api.MethodSignature
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.mdsal.binding.model.util.Types
import org.opendaylight.mdsal.binding.model.util.TypeConstants

/**
 * Template for generating JAVA interfaces.
 */
 @SuppressModernizer
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

    var Entry<Type, Set<BuilderGeneratedProperty>> typeAnalysis

    /**
     * Creates the instance of this class which is used for generating the interface file source
     * code from <code>genType</code>.
     *
     * @throws NullPointerException if <code>genType</code> is <code>null</code>
     */
    new(GeneratedType genType) {
        super(genType)
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
                «ELSEIF m.isStatic»
                    «generateStaticMethod(m)»
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

    def private generateStaticMethod(MethodSignature method) {
        switch method.name {
            case BINDING_EQUALS_NAME : generateBindingEquals
            case BINDING_HASHCODE_NAME : generateBindingHashCode
            case BINDING_TO_STRING_NAME : generateBindingToString
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
        @«OVERRIDE.importedName»
        default «CLASS.importedName»<«type.fullyQualifiedName»> «DATA_CONTAINER_IMPLEMENTED_INTERFACE_NAME»() {
            return «type.fullyQualifiedName».class;
        }
    '''

    def private generateBindingHashCode() '''
        «val augmentable = analyzeType»
        /**
         * Default implementation of {@link «Object.importedName»#hashCode()} contract for this interface.
         * Implementations of this interface are encouraged to defer to this method to get consistent hashing
         * results across all implementations.
         *
         «IF augmentable»
         * @param <T$$> implementation type, which has to also implement «AUGMENTATION_HOLDER.importedName» interface
         *              contract.
         «ENDIF»
         * @param obj Object for which to generate hashCode() result.
         * @return Hash code value of data modeled by this interface.
         * @throws «NPE.importedName» if {@code obj} is null
         */
        «IF augmentable»
        static <T$$ extends «type.fullyQualifiedName» & «AUGMENTATION_HOLDER.importedName»<?>> int «BINDING_HASHCODE_NAME»(final @«NONNULL.importedName» T$$ obj) {
        «ELSE»
        static int «BINDING_HASHCODE_NAME»(final «type.fullyQualifiedName» obj) {
        «ENDIF»
            final int prime = 31;
            int result = 1;
            «FOR property : typeAnalysis.value»
                result = prime * result + «property.importedUtilClass».hashCode(obj.«property.getterMethodName»());
            «ENDFOR»
            «IF augmentable»
                result = prime * result + «CODEHELPERS.importedName».hashAugmentations(obj);
            «ENDIF»
            return result;
        }
    '''

    def private generateBindingEquals() '''
        «val augmentable = analyzeType»
        «IF augmentable || !typeAnalysis.value.isEmpty»
            /**
             * Default implementation of {@link «Object.importedName»#equals(«Object.importedName»)} contract for this interface.
             * Implementations of this interface are encouraged to defer to this method to get consistent equality
             * results across all implementations.
             *
             «IF augmentable»
             * @param <T$$> implementation type, which has to also implement «AUGMENTATION_HOLDER.importedName» interface
             *              contract.
             «ENDIF»
             * @param thisObj Object acting as the receiver of equals invocation
             * @param obj Object acting as argument to equals invocation
             * @return True if thisObj and obj are considered equal
             * @throws «NPE.importedName» if {@code thisObj} is null
             */
            «IF augmentable»
            static <T$$ extends «type.fullyQualifiedName» & «AUGMENTATION_HOLDER.importedName»<«type.fullyQualifiedName»>> boolean «BINDING_EQUALS_NAME»(final @«NONNULL.importedName» T$$ thisObj, final «Types.objectType().importedName» obj) {
            «ELSE»
            static boolean «BINDING_EQUALS_NAME»(final «type.fullyQualifiedName» thisObj, final «Types.objectType().importedName» obj) {
            «ENDIF»
                if (thisObj == obj) {
                    return true;
                }
                final «type.fullyQualifiedName» other = «CODEHELPERS.importedName».checkCast(«type.fullyQualifiedName».class, obj);
                if (other == null) {
                    return false;
                }
                «FOR property : typeAnalysis.value»
                    if (!«property.importedUtilClass».equals(thisObj.«property.getterName»(), other.«property.getterName»())) {
                        return false;
                    }
                «ENDFOR»
                return «IF augmentable»«CODEHELPERS.importedName».equalsAugmentations(thisObj, other)«ELSE»true«ENDIF»;
            }
        «ENDIF»
    '''

    def generateBindingToString() '''
        «val augmentable = analyzeType»
        /**
         * Default implementation of {@link «Object.importedName»#toString()} contract for this interface.
         * Implementations of this interface are encouraged to defer to this method to get consistent string
         * representations across all implementations.
         *
         «IF augmentable»
         * @param <T$$> implementation type, which has to also implement «AUGMENTATION_HOLDER.importedName» interface
         *              contract.
         «ENDIF»
         * @param obj Object for which to generate toString() result.
         * @return {@link «STRING.importedName»} value of data modeled by this interface.
         * @throws «NPE.importedName» if {@code obj} is null
         */
        «IF augmentable»
        static <T$$ extends «type.fullyQualifiedName» & «AUGMENTATION_HOLDER.importedName»<«type.fullyQualifiedName»>> «STRING.importedName» «BINDING_TO_STRING_NAME»(final @«NONNULL.importedName» T$$ obj) {
        «ELSE»
        static «STRING.importedName» «BINDING_TO_STRING_NAME»(final «type.fullyQualifiedName» obj) {
        «ENDIF»
            final «MoreObjects.importedName».ToStringHelper helper = «MoreObjects.importedName».toStringHelper("«type.name»");
            «FOR property : typeAnalysis.value»
                «CODEHELPERS.importedName».appendValue(helper, "«property.name»", obj.«property.getterName»());
            «ENDFOR»
            «IF augmentable»
                «CODEHELPERS.importedName».appendValue(helper, "«AUGMENTATION_FIELD»", obj.augmentations().values());
            «ENDIF»
            return helper.toString();
        }
    '''

    def private generateNonnullMethod(MethodSignature method) '''
        «val ret = method.returnType»
        «val name = method.name»
        «formatDataForJavaDoc(method, "@return " + asCode(ret.fullyQualifiedName) + " " + asCode(propertyNameFromGetter(method)) + ", or an empty list if it is not present")»
        «method.annotations.generateAnnotations»
        default «ret.importedNonNull» «name»() {
            return «CODEHELPERS.importedName».nonnull(«name.getGetterMethodForNonnull»());
        }
    '''

    def private String nullableType(Type type) {
        if (type.isObject) {
            return type.importedNullable
        }
        return type.importedName
    }

    def private static boolean isObject(Type type) {
        // The return type has a package, so it's not a primitive type
        return !type.getPackageName().isEmpty()
    }

    private def boolean analyzeType() {
        if (typeAnalysis === null) {
            typeAnalysis = analyzeTypeHierarchy(type)
        }
        typeAnalysis.key !== null
    }
}
