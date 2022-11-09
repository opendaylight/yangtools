/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.BINARY_TYPE
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.BOOLEAN_TYPE
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.EMPTY_TYPE
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.STRING_TYPE
import static org.opendaylight.mdsal.binding.model.ri.Types.STRING
import static org.opendaylight.mdsal.binding.model.ri.Types.getOuterClassName
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.BUILDER_SUFFIX
import static extension org.opendaylight.mdsal.binding.model.ri.BindingTypes.isBitsType
import static extension org.opendaylight.mdsal.binding.model.ri.BindingTypes.isIdentityType

import java.util.Base64;
import org.opendaylight.mdsal.binding.model.api.Enumeration
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject
import org.opendaylight.mdsal.binding.model.api.Type

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
            «val propFieldName = property.fieldName»
            public «type.name»(«propertyAndTopParentProperties.asArgumentsDeclaration») {
                «IF !parentProperties.empty»
                    super(«parentProperties.asArguments»);
                «ENDIF»
                «IF restrictions !== null»
                    «checkArgument(property, restrictions, actualType, propFieldName)»
                «ENDIF»
                «FOR other : finalProperties»
                    «IF property.equals(other)»
                        this.«propFieldName» = «JU_OBJECTS.importedName».requireNonNull(«propFieldName»);
                    «ELSE»
                        this.«other.fieldName» = null;
                    «ENDIF»
                «ENDFOR»
            }
        «ENDFOR»
    '''

    def typeBuilder() {
        val outerCls = getOuterClassName(type);
        if (outerCls !== null) {
            return outerCls + type.name + BUILDER_SUFFIX
        }
        return type.name + BUILDER_SUFFIX
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
        public «STRING.importedName» stringValue() {
            «FOR property : finalProperties»
                «val field = property.fieldName»
            if («field» != null) {
                «val propRet = property.returnType»
                «IF STRING_TYPE.equals(propRet)»
                    ««« type string
                return «field»;
                «ELSEIF "org.opendaylight.yangtools.yang.binding.InstanceIdentifier".equals(propRet.fullyQualifiedName)»
                    ««« type instance-identifier
                return «field».toString();
                «ELSEIF BINARY_TYPE.equals(propRet)»
                    ««« type binary
                return new «STRING.importedName»(«field»);
                «ELSEIF propRet.fullyQualifiedName.startsWith("java.lang") || propRet instanceof Enumeration»
                    ««« type int* or enumeration*
                return «field».toString();
                «ELSEIF "org.opendaylight.yangtools.yang.common".equals(propRet.packageName)
                        && (propRet.name.startsWith("Uint") || "Decimal64".equals(propRet.name))»
                    ««« type uint*, decimal64
                return «field».toCanonicalString();
                «ELSEIF propRet instanceof GeneratedTransferObject && (propRet as GeneratedTransferObject).unionType»
                    ««« union type
                return «field».stringValue();
                «ELSEIF BOOLEAN_TYPE.equals(propRet.typedefReturnType)»
                    ««« generated boolean typedef
                return «field».isValue().toString();
                «ELSEIF BINARY_TYPE.equals(propRet.typedefReturnType)»
                    ««« generated byte[] typedef
                return «Base64.importedName».getEncoder().encodeToString(«field».getValue());
                «ELSEIF EMPTY_TYPE.equals(propRet) || EMPTY_TYPE.equals(propRet.typedefReturnType)»
                    ««« generated empty typedef
                return "";
                «ELSEIF propRet.isBitsType»
                    ««« generated bits typedef
                return «JU_ARRAYS.importedName».toString(«field».getValue());
                «ELSEIF propRet.isIdentityType»
                    ««« generated identity
                return «field».«BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME»().toString();
                «ELSE»
                    ««« generated type
                return «field».getValue().toString();
                «ENDIF»
            }
            «ENDFOR»
            throw new IllegalStateException("No value assigned");
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
                «val fieldName = p.fieldName»
                «IF p.returnType.name.endsWith("[]")»
                this.«fieldName» = source.«fieldName» == null ? null : source.«fieldName».clone();
                «ELSE»
                this.«fieldName» = source.«fieldName»;
                «ENDIF»
            «ENDFOR»
        }
    '''
}
