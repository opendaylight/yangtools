/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator

import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import java.beans.ConstructorProperties
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration
import static org.opendaylight.yangtools.binding.generator.util.Types.*

/**
 * Template for generating JAVA class.
 */
class UnionTemplate extends ClassTemplate {

    /**
     * Creates instance of this class with concrete <code>genType</code>.
     *
     * @param genType generated transfer object which will be transformed to JAVA class source code
     */
    new(GeneratedTransferObject genType) {
        super(genType)
        if(isBaseEncodingImportRequired) {
            this.importMap.put("BaseEncoding","com.google.common.io")
        }
    }

    final private def boolean isBaseEncodingImportRequired() {
        for (property : finalProperties) {
            val propRet = property.returnType
            if (propRet instanceof GeneratedTransferObject && (propRet as GeneratedTransferObject).typedef &&
                (propRet as GeneratedTransferObject).properties != null &&
                !(propRet as GeneratedTransferObject).properties.empty &&
                ((propRet as GeneratedTransferObject).properties.size == 1) &&
                (propRet as GeneratedTransferObject).properties.get(0).name.equals("value") &&
                "byte[]".equals((propRet as GeneratedTransferObject).properties.get(0).returnType.name)) {
                return true;
            }
        }
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
    '''

    private def unionConstructors() '''
        «FOR property : finalProperties SEPARATOR "\n"»
            «val propRet = property.returnType»
            «val isCharArray = "char[]".equals(propRet.name)»
            «IF isCharArray»
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
                «val propertyAndTopParentProperties = parentProperties + #[property]»
                public «type.name»(«propertyAndTopParentProperties.asArgumentsDeclaration») {
                    super(«parentProperties.asArguments»);
                    this.«property.fieldName» = «property.fieldName»;
                    «FOR other : finalProperties»
                        «IF property != other»
                            «IF "value".equals(other.name)»
                                «IF "java.lang.String".equals(propRet.fullyQualifiedName)»
                                    ««« type string
                                    this.«other.fieldName» = «property.fieldName».toCharArray();
                                «ELSEIF "byte[]".equals(propRet.name)»
                                    ««« type binary
                                    this.«other.fieldName» = new «String.importedName»(«property.fieldName»).toCharArray();
                                «ELSEIF propRet.fullyQualifiedName.startsWith("java.lang")
                                    || propRet instanceof Enumeration
                                    || propRet.fullyQualifiedName.startsWith("java.math")»
                                    ««« type int*, uint, decimal64 or enumeration*
                                    this.«other.fieldName» = «property.fieldName».toString().toCharArray();
                                «ELSEIF propRet instanceof GeneratedTransferObject && (propRet as GeneratedTransferObject).unionType»
                                    ««« union type
                                    this.«other.fieldName» = «property.fieldName».getValue();
                                «ELSEIF propRet instanceof GeneratedTransferObject // Is it a GeneratedTransferObject
                                        && (propRet as GeneratedTransferObject).typedef  // Is it a typedef
                                        && (propRet as GeneratedTransferObject).properties != null
                                        && !(propRet as GeneratedTransferObject).properties.empty
                                        && ((propRet as GeneratedTransferObject).properties.size == 1)
                                        && (propRet as GeneratedTransferObject).properties.get(0).name.equals("value")
                                        && BOOLEAN.equals((propRet as GeneratedTransferObject).properties.get(0).returnType)» // And the property value is of type boolean
                                    ««« generated boolean typedef
                                    this.«other.fieldName» = «property.fieldName».isValue().toString().toCharArray();
                                «ELSEIF propRet instanceof GeneratedTransferObject // Is it a GeneratedTransferObject
                                        && (propRet as GeneratedTransferObject).typedef  // Is it a typedef
                                        && (propRet as GeneratedTransferObject).properties != null
                                        && !(propRet as GeneratedTransferObject).properties.empty
                                        && ((propRet as GeneratedTransferObject).properties.size == 1)
                                        && (propRet as GeneratedTransferObject).properties.get(0).name.equals("value")
                                        && "byte[]".equals((propRet as GeneratedTransferObject).properties.get(0).returnType.name)»
                                    ««« generated byte[] typedef
                                    this.«other.fieldName» = BaseEncoding.base64().encode(«property.fieldName».getValue()).toCharArray();
                                «ELSE»
                                    ««« generated type
                                    this.«other.fieldName» = «property.fieldName».getValue().toString().toCharArray();
                                «ENDIF»
                            «ELSE»
                                this.«other.fieldName» = null;
                            «ENDIF»
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
                    «IF p.returnType.importedName.contains("[]")»
                    this.«p.fieldName» = source.«p.fieldName» == null ? null : source.«p.fieldName».clone();
                    «ELSE»
                    this.«p.fieldName» = source.«p.fieldName»;
                    «ENDIF»
                «ENDFOR»
            «ENDIF»
        }
    '''

}
