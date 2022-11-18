/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static extension org.opendaylight.mdsal.binding.generator.BindingGeneratorUtil.encodeAngleBrackets
import static org.opendaylight.mdsal.binding.model.ri.Types.STRING;

import org.opendaylight.mdsal.binding.model.api.Enumeration
import org.opendaylight.mdsal.binding.model.api.GeneratedType
import org.opendaylight.mdsal.binding.model.api.JavaTypeName
import org.opendaylight.yangtools.yang.binding.EnumTypeObject

/**
 * Template for generating JAVA enumeration type.
 */
class EnumTemplate extends BaseTemplate {
    static val ENUM_TYPE_OBJECT = JavaTypeName.create(EnumTypeObject)

    /**
     * Enumeration which will be transformed to JAVA source code for enumeration
     */
    val Enumeration enums

    /**
     * Constructs instance of this class with concrete <code>enums</code>.
     *
     * @param enums enumeration which will be transformed to JAVA source code
     */
    new(AbstractJavaGeneratedType javaType, Enumeration enums) {
        super(javaType, enums as GeneratedType)
        this.enums = enums
    }

    /**
     * Constructs instance of this class with concrete <code>enums</code>.
     *
     * @param enums enumeration which will be transformed to JAVA source code
     */
    new(Enumeration enums) {
        super(enums as GeneratedType)
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
        «IF description !== null»
            «description.trim.encodeAngleBrackets.encodeJavadocSymbols.wrapToDocumentation»
        «ENDIF»
        «mappedName»(«value», "«name»")
    '''

    /**
     * Template method which generates enumeration body (declaration + enumeration items).
     *
     * @return string with the enumeration body
     */
    override body() '''
        «enums.formatDataForJavaDoc.wrapToDocumentation»
        «generatedAnnotation»
        public enum «enums.name» implements «ENUM_TYPE_OBJECT.importedName» {
            «writeEnumeration(enums)»

            private final «STRING.importedNonNull» name;
            private final int value;

            private «enums.name»(int value, «STRING.importedNonNull» name) {
                this.value = value;
                this.name = name;
            }

            @«OVERRIDE.importedName»
            public «STRING.importedNonNull» getName() {
                return name;
            }

            @«OVERRIDE.importedName»
            public int getIntValue() {
                return value;
            }

            /**
             * Return the enumeration member whose {@link #getName()} matches specified assigned name.
             *
             * @param name YANG assigned name
             * @return corresponding «enums.name» item, or {@code null} if no such item exists
             * @throws «NPE.importedName» if {@code name} is null
             */
            public static «enums.importedNullable» forName(«STRING.importedName» name) {
                return switch (name) {
                    «FOR v : enums.values»
                    case "«v.name»" -> «v.mappedName»;
                    «ENDFOR»
                    default -> null;
                };
            }

            /**
             * Return the enumeration member whose {@link #getIntValue()} matches specified value.
             *
             * @param intValue integer value
             * @return corresponding «enums.name» item, or {@code null} if no such item exists
             */
            public static «enums.importedNullable» forValue(int intValue) {
                return switch (intValue) {
                    «FOR v : enums.values»
                    case «v.value» -> «v.mappedName»;
                    «ENDFOR»
                    default -> null;
                };
            }

            /**
             * Return the enumeration member whose {@link #getName()} matches specified assigned name.
             *
             * @param name YANG assigned name
             * @return corresponding «enums.name» item
             * @throws «NPE.importedName» if {@code name} is null
             * @throws «IAE.importedName» if {@code name} does not match any item
             */
            public static «enums.importedNonNull» ofName(«STRING.importedName» name) {
                return «CODEHELPERS.importedName».checkEnum(forName(name), name);
            }

            /**
             * Return the enumeration member whose {@link #getIntValue()} matches specified value.
             *
             * @param intValue integer value
             * @return corresponding «enums.name» item
             * @throws «IAE.importedName» if {@code intValue} does not match any item
             */
            public static «enums.importedNonNull» ofValue(int intValue) {
                return «CODEHELPERS.importedName».checkEnum(forValue(intValue), intValue);
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
