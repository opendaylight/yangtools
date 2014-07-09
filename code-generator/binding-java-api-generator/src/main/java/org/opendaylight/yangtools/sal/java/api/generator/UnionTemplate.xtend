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
                    «type.name» defInst = «type.name»Builder.getDefaultInstance(defVal);
                    «FOR other : finalProperties»
                        «IF other.name.equals("value")»
                            this.«other.fieldName» = «other.fieldName»;
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
                                «ELSEIF propRet.fullyQualifiedName.startsWith("java.lang") || propRet instanceof Enumeration»
                                    ««« type int*, uint or enumeration*
                                    this.«other.fieldName» = «property.fieldName».toString().toCharArray();
                                «ELSEIF propRet instanceof GeneratedTransferObject && (propRet as GeneratedTransferObject).unionType»
                                    ««« union type
                                    this.«other.fieldName» = «property.fieldName».getValue();
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
                    this.«p.fieldName» = source.«p.fieldName»;
                «ENDFOR»
            «ENDIF»
        }
    '''

}
