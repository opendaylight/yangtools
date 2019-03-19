/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static org.opendaylight.mdsal.binding.model.util.Types.BOOLEAN;
import static org.opendaylight.mdsal.binding.model.util.Types.BYTE_ARRAY;
import static org.opendaylight.mdsal.binding.model.util.Types.getOuterClassName;

import java.util.Arrays
import java.util.Base64;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject
import org.opendaylight.mdsal.binding.model.api.Enumeration
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition

/**
 * Template for generating JAVA class.
 */
class UnionTemplate extends ClassTemplate {

    /**
     * Creates instance of this class with concrete <code>genType</code>.
     *
     * @param genType generated transfer object which will be transformed to JAVA class source code
     */
    new(NestedJavaGeneratedType javaType, GeneratedTransferObject genType) {
        super(javaType, genType)
    }

    /**
     * Creates instance of this class with concrete <code>genType</code>.
     *
     * @param genType generated transfer object which will be transformed to JAVA class source code
     */
    new(GeneratedTransferObject genType) {
        super(genType)
    }

    override constructors() '''
        «unionConstructorsParentProperties»
        «unionConstructors»
        «IF !allProperties.empty»
            «copyConstructor»
        «ENDIF»
        «IF properties.empty && !parentProperties.empty»
            «parentConstructor»
        «ENDIF»

        «generateStringValue»
    '''

    private def unionConstructors() '''
        «FOR property : finalProperties SEPARATOR "\n"»
            «val actualType = property.returnType»
            «val restrictions = restrictionsForSetter(actualType)»
            «IF restrictions !== null»
                «generateCheckers(property, restrictions, actualType)»
            «ENDIF»
            «val propertyAndTopParentProperties = parentProperties + #[property]»
            public «type.name»(«propertyAndTopParentProperties.asArgumentsDeclaration») {
                super(«parentProperties.asArguments»);
                «IF restrictions !== null»
                    «checkArgument(property, restrictions, actualType, property.fieldName.toString)»
                «ENDIF»
                this.«property.fieldName» = «property.fieldName»;
                «FOR other : finalProperties»
                    «IF property != other»
                         this.«other.fieldName» = null;
                    «ENDIF»
                «ENDFOR»
            }
        «ENDFOR»
    '''

    def typeBuilder() {
        val outerCls = getOuterClassName(type);
        if(outerCls !== null) {
            return outerCls + type.name + "Builder"
        }
        return type.name + "Builder"
    }

    private def unionConstructorsParentProperties() '''
        «FOR property : parentProperties SEPARATOR "\n"»
            public «type.name»(«property.returnType.importedName» «property.fieldName») {
                super(«property.fieldName»);
            }
        «ENDFOR»
    '''

    def generateStringValue()
    '''
        /**
         * Return a String representing the value of this union.
         *
         * @return String representation of this union's value.
         */
        public «String.importedName» stringValue() {
            «FOR property : finalProperties»
                «val field = property.fieldName»
            if («field» != null) {
                «val propRet = property.returnType»
                «IF "java.lang.String".equals(propRet.fullyQualifiedName)»
                    ««« type string
                return «field»;
                «ELSEIF "org.opendaylight.yangtools.yang.binding.InstanceIdentifier".equals(propRet.fullyQualifiedName)»
                    ««« type instance-identifier
                return «field».toString();
                «ELSEIF "byte[]".equals(propRet.name)»
                    ««« type binary
                return new «String.importedName»(«field»);
                «ELSEIF propRet.fullyQualifiedName.startsWith("java.lang") || propRet instanceof Enumeration
                        || propRet.fullyQualifiedName.startsWith("java.math")»
                    ««« type int*, uint, decimal64 or enumeration*
                return «field».toString();
                «ELSEIF propRet instanceof GeneratedTransferObject && (propRet as GeneratedTransferObject).unionType»
                    ««« union type
                return «field».stringValue();
                «ELSEIF BOOLEAN.equals(propRet.typedefReturnType)»
                    ««« generated boolean typedef
                return «field».isValue().toString();
                «ELSEIF BYTE_ARRAY.equals(propRet.typedefReturnType)»
                    ««« generated byte[] typedef
                return «Base64.importedName».getEncoder().encodeToString(«field».getValue());
                «ELSEIF propRet instanceof GeneratedTransferObject // Is it a GeneratedTransferObject
                        && (propRet as GeneratedTransferObject).typedef  // Is it a typedef
                        && (propRet as GeneratedTransferObject).baseType instanceof BitsTypeDefinition»
                    ««« generated bits typedef
                return «Arrays.importedName».toString(«field».getValue());
                «ELSE»
                    ««« generated type
                return «field».getValue().toString();
                «ENDIF»
            }
            «ENDFOR»

            throw new IllegalStateException("No value assinged");
        }
    '''

    private static def Type typedefReturnType(Type type) {
        if (!(type instanceof GeneratedTransferObject)) {
            return null
        }
        val gto = type as GeneratedTransferObject
        if (!gto.typedef || gto.properties === null || gto.properties.size != 1) {
            return null
        }
        val prop = gto.properties.get(0)
        if (prop.name.equals("value")) {
            return prop.returnType
        }
        return null
    }

    override protected copyConstructor() '''
        /**
         * Creates a copy from Source Object.
         *
         * @param source Source object
         */
        public «type.name»(«type.name» source) {
            «IF !parentProperties.empty»
                super(source);
            «ENDIF»
            «FOR p : properties»
                «IF p.returnType.importedName.contains("[]")»
                this.«p.fieldName» = source.«p.fieldName» == null ? null : source.«p.fieldName».clone();
                «ELSE»
                this.«p.fieldName» = source.«p.fieldName»;
                «ENDIF»
            «ENDFOR»
        }
    '''

}
