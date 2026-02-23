/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen

import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.BINARY_TYPE
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.BOOLEAN_TYPE
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.EMPTY_TYPE
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.STRING_TYPE
import static org.opendaylight.yangtools.binding.model.ri.Types.STRING
import static org.opendaylight.yangtools.binding.contract.Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME
import static extension org.opendaylight.yangtools.binding.model.ri.BindingTypes.isBitsType
import static extension org.opendaylight.yangtools.binding.model.ri.BindingTypes.isIdentityType

import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject
import org.opendaylight.yangtools.binding.model.api.Type
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype

/**
 * Template for generating JAVA class.
 */
final class UnionTemplate extends ClassTemplate {
    /**
     * Creates instance of this class with concrete <code>genType</code>.
     *
     * @param genType generated transfer object which will be transformed to JAVA class source code
     */
    new(NestedJavaGeneratedType javaType, UnionTypeObjectArchetype genType) {
        super(javaType, genType)
    }

    /**
     * Creates instance of this class with concrete <code>genType</code>.
     *
     * @param genType generated transfer object which will be transformed to JAVA class source code
     */
    new(UnionTypeObjectArchetype genType) {
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
            public «type.simpleName»(«propertyAndTopParentProperties.asArgumentsDeclaration») {
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

    private def unionConstructorsParentProperties() '''
        «FOR property : parentProperties SEPARATOR "\n"»
            public «type.simpleName»(«property.returnType.importedName» «property.fieldName») {
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
                «ELSEIF "org.opendaylight.yangtools.binding.BindingInstanceIdentifier".equals(propRet.fullyQualifiedName)»
                    ««« type instance-identifier
                return «field».toString();
                «ELSEIF BINARY_TYPE.equals(propRet)»
                    ««« type binary
                return new «STRING.importedName»(«field»);
                «ELSEIF propRet.fullyQualifiedName.startsWith("java.lang") || propRet instanceof EnumTypeObjectArchetype»
                    ««« type int* or enumeration*
                return «field».toString();
                «ELSEIF "org.opendaylight.yangtools.yang.common".equals(propRet.packageName)
                        && (propRet.simpleName.startsWith("Uint") || "Decimal64".equals(propRet.simpleName))»
                    ««« type uint*, decimal64
                return «field».toCanonicalString();
                «ELSEIF propRet instanceof UnionTypeObjectArchetype»
                    ««« union type
                return «field».stringValue();
                «ELSEIF BOOLEAN_TYPE.equals(propRet.typedefReturnType)»
                    ««« generated boolean typedef
                return «field».isValue().toString();
                «ELSEIF BINARY_TYPE.equals(propRet.typedefReturnType)»
                    ««« generated byte[] typedef
                return «JU_BASE64.importedName».getEncoder().encodeToString(«field».getValue());
                «ELSEIF EMPTY_TYPE.equals(propRet) || EMPTY_TYPE.equals(propRet.typedefReturnType)»
                    ««« generated empty typedef
                return "";
                «ELSEIF propRet.isBitsType»
                    ««« generated bits typedef
                return «JU_ARRAYS.importedName».toString(«field».values());
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
        val prop = gto.properties.first
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
        public «type.simpleName»(«type.simpleName» source) {
            «IF !parentProperties.empty»
                super(source);
            «ENDIF»
            «FOR p : properties»
                «val fieldName = p.fieldName»
                «IF p.returnType.simpleName.endsWith("[]")»
                this.«fieldName» = source.«fieldName» == null ? null : source.«fieldName».clone();
                «ELSE»
                this.«fieldName» = source.«fieldName»;
                «ENDIF»
            «ENDFOR»
        }
    '''
}
