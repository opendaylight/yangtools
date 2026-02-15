/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen

import static extension org.opendaylight.yangtools.binding.generator.BindingGeneratorUtil.encodeAngleBrackets
import static org.opendaylight.yangtools.binding.model.ri.Types.STRING;
import static org.opendaylight.yangtools.binding.model.ri.Types.objectType;

import com.google.common.base.CharMatcher
import java.util.Locale
import java.util.Map.Entry
import java.util.StringTokenizer
import java.util.regex.Pattern
import org.opendaylight.yangtools.binding.model.api.AnnotationType
import org.opendaylight.yangtools.binding.model.api.ConcreteType
import org.opendaylight.yangtools.binding.model.api.Constant
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.binding.model.api.GeneratedType
import org.opendaylight.yangtools.binding.model.api.JavaTypeName
import org.opendaylight.yangtools.binding.model.api.Restrictions
import org.opendaylight.yangtools.binding.model.api.Type
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment
import org.opendaylight.yangtools.binding.model.ri.TypeConstants
import org.opendaylight.yangtools.binding.BaseIdentity
import org.opendaylight.yangtools.binding.contract.Naming
import org.opendaylight.yangtools.yang.common.YangDataName

// FIXME: YANGTOOLS-1618: convert to Java
abstract class BaseTemplate extends AbstractBaseTemplate {
    static final char NEW_LINE = '\n'
    static final char SPACE = ' '
    static val WS_MATCHER = CharMatcher.anyOf("\n\t")
    static val SPACES_PATTERN = Pattern.compile(" +")

    new(GeneratedType type) {
        super(type)
    }

    new(AbstractJavaGeneratedType javaType, GeneratedType type) {
        super(javaType, type)
    }

    // Helper patterns
    /**
     * Template method which generates the getter method for <code>field</code>
     *
     * @param field
     * generated property with data about field which is generated as the getter method
     * @return string with the getter method source code in JAVA format
     */
    def package getterMethod(GeneratedProperty field) '''
        «val methodName = field.getterMethodName»
        public «field.returnType.importedName» «methodName»() {
            «val fieldName = field.fieldName»
            «IF field.returnType.name.endsWith("[]")»
            return «fieldName» == null ? null : «fieldName».clone();
            «ELSE»
            return «fieldName»;
            «ENDIF»
        }
    '''

    /**
     * Template method which generates the setter method for <code>field</code>
     *
     * @param field
     * generated property with data about field which is generated as the setter method
     * @return string with the setter method source code in JAVA format
     */
    final protected def setterMethod(GeneratedProperty field) '''
        «val returnType = field.returnType.importedName»
        public «type.name» set«field.name.toFirstUpper»(«returnType» value) {
            this.«field.fieldName» = value;
            return this;
        }
    '''

    /**
     * Template method which generates JAVA comments.
     *
     * @param comment string with the comment for whole JAVA class
     * @return string with comment in JAVA format
     */
    def final protected asJavadoc(TypeMemberComment comment) {
        if (comment === null) {
            return ''
        }
        return wrapToDocumentation('''
           «comment.contractDescription»

           «comment.referenceDescription.formatReference»

           «comment.typeSignature»
        ''')
    }

    def static formatReference(String reference) '''
        «IF reference !== null»
            <pre>
                <code>
                    «reference.encodeAngleBrackets.formatToParagraph»
                </code>
            </pre>

        «ENDIF»
    '''

    protected static def formatToParagraph(String inputText) {
        val StringBuilder sb = new StringBuilder();
        var StringBuilder lineBuilder = new StringBuilder();
        var boolean isFirstElementOnNewLineEmptyChar = false;

        var formattedText = WS_MATCHER.replaceFrom(inputText.encodeJavadocSymbols, SPACE)
        formattedText = SPACES_PATTERN.matcher(formattedText).replaceAll(" ")

        val StringTokenizer tokenizer = new StringTokenizer(formattedText, " ", true)
        while (tokenizer.hasMoreTokens) {
            val nextElement = tokenizer.nextToken

            if (lineBuilder.length != 0 && lineBuilder.length + nextElement.length > 80) {
                if (lineBuilder.charAt(lineBuilder.length - 1) == SPACE) {
                    lineBuilder.setLength(lineBuilder.length - 1)
                }
                if (lineBuilder.length != 0 && lineBuilder.charAt(0) == SPACE) {
                    lineBuilder.deleteCharAt(0)
                }

                sb.append(lineBuilder).append(NEW_LINE)
                lineBuilder.setLength(0)

                if (" ".equals(nextElement)) {
                    isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar;
                }
            }
            if (isFirstElementOnNewLineEmptyChar) {
                isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar
            } else {
                lineBuilder.append(nextElement)
            }
        }

        return sb.append(lineBuilder).append(NEW_LINE).toString
    }

    def protected emitConstant(Constant c) '''
        «IF Naming.QNAME_STATIC_FIELD_NAME.equals(c.name)»
            «val entry = c.value as Entry<JavaTypeName, String>»
            /**
             * YANG identifier of the statement represented by this class.
             */
            public static final «c.type.importedNonNull» «c.name» = «entry.key.importedName».«Naming.MODULE_INFO_QNAMEOF_METHOD_NAME»("«entry.value»");
        «ELSEIF Naming.NAME_STATIC_FIELD_NAME.equals(c.name)»
            «val entry = c.value as Entry<JavaTypeName, YangDataName>»
            /**
             * Yang Data template name of the statement represented by this class.
             */
            public static final «c.type.importedNonNull» «c.name» = «entry.key.importedName».«Naming.MODULE_INFO_YANGDATANAMEOF_METHOD_NAME»("«entry.value.name»");
        «ELSEIF Naming.VALUE_STATIC_FIELD_NAME.equals(c.name) && BaseIdentity.equals(c.value)»
            «val typeName = c.type.importedName»
            «val override = OVERRIDE.importedName»
            /**
             * Singleton value representing the {@link «typeName»} identity.
             */
            public static final «c.type.importedNonNull» «c.name» = new «typeName»() {
                @java.io.Serial
                private static final long serialVersionUID = 1L;

                @«override»
                public «CLASS.importedName»<«typeName»> «Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME»() {
                    return «typeName».class;
                }

                @«override»
                public int hashCode() {
                    return «typeName».class.hashCode();
                }

                @«override»
                public boolean equals(final «objectType.importedName» obj) {
                    return obj == this || obj instanceof «typeName» other
                        && «typeName».class.equals(other.«Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME»());
                }

                @«override»
                public «STRING.importedName» toString() {
                    return «MOREOBJECTS.importedName».toStringHelper("«c.type.name»").add("qname", QNAME).toString();
                }

                @java.io.Serial
                private Object readResolve() throws java.io.ObjectStreamException {
                    return «c.name»;
                }
            };
        «ELSE»
            public static final «c.type.importedName» «c.name» = «c.value»;
        «ENDIF»
    '''

    def protected generateCheckers(GeneratedProperty field, Restrictions restrictions, Type actualType) '''
       «IF restrictions.rangeConstraint.present»
           «AbstractRangeGenerator.forType(actualType).generateRangeChecker(field.name.toFirstUpper,
               restrictions.rangeConstraint.orElseThrow, this)»
       «ENDIF»
       «IF restrictions.lengthConstraint.present»
           «LengthGenerator.generateLengthChecker(field.fieldName, actualType, restrictions.lengthConstraint.orElseThrow, this)»
       «ENDIF»
    '''

    def protected checkArgument(GeneratedProperty property, Restrictions restrictions, Type actualType, String value) '''
       «IF restrictions.getRangeConstraint.isPresent»
           «IF actualType instanceof ConcreteType»
               «AbstractRangeGenerator.forType(actualType).generateRangeCheckerCall(property.getName.toFirstUpper, value)»
           «ELSE»
               «AbstractRangeGenerator.forType(actualType).generateRangeCheckerCall(property.getName.toFirstUpper, value + ".getValue()")»
           «ENDIF»
       «ENDIF»
       «val fieldName = property.fieldName»
       «IF restrictions.getLengthConstraint.isPresent»
           «IF actualType instanceof ConcreteType»
               «LengthGenerator.generateLengthCheckerCall(fieldName, value)»
           «ELSE»
               «LengthGenerator.generateLengthCheckerCall(fieldName, value + ".getValue()")»
           «ENDIF»
       «ENDIF»

       «val fieldUpperCase = fieldName.toUpperCase(Locale.ROOT)»
       «FOR currentConstant : type.getConstantDefinitions»
           «IF currentConstant.getName.startsWith(TypeConstants.PATTERN_CONSTANT_NAME)
               && fieldUpperCase.equals(currentConstant.getName.substring(TypeConstants.PATTERN_CONSTANT_NAME.length))»
           «CODEHELPERS.importedName».checkPattern(value, «Constants.MEMBER_PATTERN_LIST»«fieldName», «Constants.MEMBER_REGEX_LIST»«fieldName»);
           «ENDIF»
       «ENDFOR»
    '''

    def protected final generateAnnotation(AnnotationType annotation) '''
        @«annotation.importedName»
        «IF annotation.parameters !== null && !annotation.parameters.empty»
        (
        «FOR param : annotation.parameters SEPARATOR ","»
            «param.name»=«param.value»
        «ENDFOR»
        )
        «ENDIF»
    '''
}
