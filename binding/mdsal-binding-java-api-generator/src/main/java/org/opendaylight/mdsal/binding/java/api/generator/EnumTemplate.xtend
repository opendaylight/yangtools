/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import org.opendaylight.mdsal.binding.model.api.Enumeration
import org.opendaylight.mdsal.binding.model.api.GeneratedType

import static org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil.encodeAngleBrackets

/**
 * Template for generating JAVA enumeration type.
 */
class EnumTemplate extends BaseTemplate {


    /**
     * Enumeration which will be transformed to JAVA source code for enumeration
     */
    val Enumeration enums

    /**
     * Constructs instance of this class with concrete <code>enums</code>.
     *
     * @param enums enumeration which will be transformed to JAVA source code
     */
    new(Enumeration enums) {
        super(enums as GeneratedType )
        this.enums = enums
    }


    /**
     * Generates only JAVA enumeration source code.
     *
     * @return string with JAVA enumeration source code
     */
    def generateAsInnerClass() {
        return body
    }

    def writeEnumItem(String name, String mappedName, int value, String description) '''
        «asJavadoc(encodeAngleBrackets(description))»
        «mappedName»(«value», "«name»")
    '''

    /**
     * Template method which generates enumeration body (declaration + enumeration items).
     *
     * @return string with the enumeration body
     */
    override body() '''
        «wrapToDocumentation(formatDataForJavaDoc(enums))»
        public enum «enums.name» {
            «writeEnumeration(enums)»

            private static final java.util.Map<java.lang.Integer, «enums.name»> VALUE_MAP;

            static {
                final com.google.common.collect.ImmutableMap.Builder<java.lang.Integer, «enums.name»> b = com.google.common.collect.ImmutableMap.builder();
                for («enums.name» enumItem : «enums.name».values()) {
                    b.put(enumItem.value, enumItem);
                }

                VALUE_MAP = b.build();
            }

            private final «String.importedName» name;
            private final int value;

            private «enums.name»(int value, «String.importedName» name) {
                this.value = value;
                this.name = name;
            }

            /**
             * Returns the name of the enumeration item as it is specified in the input yang.
             *
             * @return the name of the enumeration item as it is specified in the input yang
             */
            public «String.importedName» getName() {
                return name;
            }

            /**
             * @return integer value
             */
            public int getIntValue() {
                return value;
            }

            /**
             * @param valueArg integer value
             * @return corresponding «enums.name» item
             */
            public static «enums.name» forValue(int valueArg) {
                return VALUE_MAP.get(valueArg);
            }
        }
    '''

    def writeEnumeration(Enumeration enumeration)
    '''
    «FOR v : enumeration.values SEPARATOR ",\n" AFTER ";"»
    «writeEnumItem(v.name, v.mappedName, v.value, v.description.orElse(null))»«
    ENDFOR»
    '''
}
