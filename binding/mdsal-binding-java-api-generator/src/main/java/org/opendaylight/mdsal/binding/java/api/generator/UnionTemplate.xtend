/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static org.opendaylight.mdsal.binding.model.util.Types.BOOLEAN;
import static org.opendaylight.mdsal.binding.model.util.Types.getOuterClassName;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding
import java.beans.ConstructorProperties
import java.util.Arrays
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject
import org.opendaylight.mdsal.binding.model.api.Enumeration
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
            «val propRet = property.returnType»
            «IF "char[]".equals(propRet.name)»
                /**
                 * Constructor provided only for using in JMX. Don't use it for
                 * construction new object of this union type.
                 */
                @«ConstructorProperties.importedName»("«property.name»")
                public «type.name»(«propRet.importedName» «property.fieldName») {
                    «String.importedName» defVal = new «String.importedName»(«property.fieldName»);
                    «type.name» defInst = «typeBuilder()».getDefaultInstance(defVal);
                    «FOR other : finalProperties»
                        «IF other.name.equals("value")»
                            «IF other.returnType.importedName.contains("[]")»
                            this.«other.fieldName» = «other.fieldName» == null ? null : «other.fieldName».clone();
                            «ELSE»
                            this.«other.fieldName» = «other.fieldName»;
                            «ENDIF»
                        «ELSE»
                            this.«other.fieldName» = defInst.«other.fieldName»;
                        «ENDIF»
                    «ENDFOR»
                }
            «ELSE»
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
                        «IF property != other && !"value".equals(other.name)»
                             this.«other.fieldName» = null;
                        «ENDIF»
                    «ENDFOR»
                }
            «ENDIF»
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
        public «String.importedName» stringValue() {
            «FOR property : finalProperties.filter([ p | !"value".equals(p.name)])»
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
                «ELSEIF propRet instanceof GeneratedTransferObject
                        && (propRet as GeneratedTransferObject).unionType»
                    ««« union type
                return «field».stringValue();
                «ELSEIF propRet instanceof GeneratedTransferObject // Is it a GeneratedTransferObject
                        && (propRet as GeneratedTransferObject).typedef  // Is it a typedef
                        && (propRet as GeneratedTransferObject).properties !== null
                        && !(propRet as GeneratedTransferObject).properties.empty
                        && ((propRet as GeneratedTransferObject).properties.size == 1)
                        && (propRet as GeneratedTransferObject).properties.get(0).name.equals("value")
                        && BOOLEAN.equals((propRet as GeneratedTransferObject).properties.get(0).returnType) // And the property value is of type boolean»
                    ««« generated boolean typedef
                return «field».isValue().toString();
                «ELSEIF propRet instanceof GeneratedTransferObject // Is it a GeneratedTransferObject
                        && (propRet as GeneratedTransferObject).typedef  // Is it a typedef
                        && (propRet as GeneratedTransferObject).properties !== null
                        && !(propRet as GeneratedTransferObject).properties.empty
                        && ((propRet as GeneratedTransferObject).properties.size == 1)
                        && (propRet as GeneratedTransferObject).properties.get(0).name.equals("value")
                        && "byte[]".equals((propRet as GeneratedTransferObject).properties.get(0).returnType.name)»
                    ««« generated byte[] typedef
                return «BaseEncoding.importedName».base64().encode(«field».getValue());
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

    override protected getterMethod(GeneratedProperty field) {
        if (!"value".equals(field.name)) {
            return super.getterMethod(field)
        }

        Preconditions.checkArgument("char[]".equals(field.returnType.importedName))

        '''
            @«Deprecated.importedName»
            public char[] «field.getterMethodName»() {
                if («field.fieldName» == null) {
                    «field.fieldName» = stringValue().toCharArray();
                }
                return «field.fieldName».clone();
            }
        '''
    }

    override isReadOnly(GeneratedProperty field) {
        return !"value".equals(field.name) && super.isReadOnly(field)
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
            «IF !properties.empty»
                «FOR p : properties»
                    «IF !"value".equals(p.name) && p.returnType.importedName.contains("[]")»
                    this.«p.fieldName» = source.«p.fieldName» == null ? null : source.«p.fieldName».clone();
                    «ELSE»
                    this.«p.fieldName» = source.«p.fieldName»;
                    «ENDIF»
                «ENDFOR»
            «ENDIF»
        }
    '''

}
