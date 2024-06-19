/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil.encodeAngleBrackets
import static org.opendaylight.mdsal.binding.model.util.Types.STRING;

import com.google.common.base.CharMatcher
import com.google.common.base.MoreObjects
import com.google.common.base.Splitter
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Iterables
import java.math.BigInteger
import java.util.Collection
import java.util.List
import java.util.Locale
import java.util.Map.Entry
import java.util.StringTokenizer
import java.util.regex.Pattern
import org.gaul.modernizer_maven_annotations.SuppressModernizer
import org.opendaylight.mdsal.binding.model.api.AnnotationType
import org.opendaylight.mdsal.binding.model.api.ConcreteType
import org.opendaylight.mdsal.binding.model.api.Constant
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty
import org.opendaylight.mdsal.binding.model.api.GeneratedType
import org.opendaylight.mdsal.binding.model.api.JavaTypeName
import org.opendaylight.mdsal.binding.model.api.MethodSignature
import org.opendaylight.mdsal.binding.model.api.Restrictions
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.mdsal.binding.model.api.TypeMember
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition.Single
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition.Multiple
import org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil
import org.opendaylight.mdsal.binding.model.util.TypeConstants
import org.opendaylight.mdsal.binding.model.util.Types
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping
import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.common.Uint8
import org.opendaylight.yangtools.yang.common.Uint16
import org.opendaylight.yangtools.yang.common.Uint32
import org.opendaylight.yangtools.yang.common.Uint64
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition
import org.opendaylight.yangtools.yang.model.api.RpcDefinition
import org.opendaylight.yangtools.yang.model.api.SchemaNode
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement
import org.opendaylight.yangtools.yang.model.export.DeclaredStatementFormatter

@SuppressModernizer
abstract class BaseTemplate extends JavaFileTemplate {
    static final char NEW_LINE = '\n'
    static final char SPACE = ' '
    static val AMP_MATCHER = CharMatcher.is('&')
    static val WS_MATCHER = CharMatcher.anyOf("\n\t")
    static val SPACES_PATTERN = Pattern.compile(" +")
    static val NL_SPLITTER = Splitter.on(NEW_LINE)
    static val TAIL_COMMENT_PATTERN = Pattern.compile("*/", Pattern.LITERAL);
    static val YANG_FORMATTER = DeclaredStatementFormatter.builder()
        .addIgnoredStatement(YangStmtMapping.CONTACT)
        .addIgnoredStatement(YangStmtMapping.DESCRIPTION)
        .addIgnoredStatement(YangStmtMapping.REFERENCE)
        .addIgnoredStatement(YangStmtMapping.ORGANIZATION)
        .build();

    protected static val UINT_TYPES = ImmutableMap.of(
        Types.typeForClass(Uint8), Types.typeForClass(Short),
        Types.typeForClass(Uint16), Types.typeForClass(Integer),
        Types.typeForClass(Uint32), Types.typeForClass(Long),
        Types.typeForClass(Uint64), Types.typeForClass(BigInteger)
    );

    new(GeneratedType type) {
        super(type)
    }

    new(AbstractJavaGeneratedType javaType, GeneratedType type) {
        super(javaType, type)
    }

    final def generate() {
        val _body = body()
        '''
            package «type.packageName»;
            «generateImportBlock»

            «_body»
        '''.toString
    }

    protected abstract def CharSequence body();

    // Helper patterns
    final protected def fieldName(GeneratedProperty property) {
        "_" + property.name
    }

    final protected def propertyNameFromGetter(MethodSignature getter) {
        var String prefix;
        if (getter.name.startsWith(BindingMapping.BOOLEAN_GETTER_PREFIX)) {
            prefix = BindingMapping.BOOLEAN_GETTER_PREFIX
        } else if (getter.name.startsWith(BindingMapping.GETTER_PREFIX)) {
            prefix = BindingMapping.GETTER_PREFIX
        } else if (getter.name.startsWith(BindingMapping.NONNULL_PREFIX)) {
            prefix = BindingMapping.NONNULL_PREFIX
        } else {
            throw new IllegalArgumentException(getter + " is not a getter")
        }
        return getter.name.substring(prefix.length).toFirstLower;
    }

    /**
     * Template method which generates the getter method for <code>field</code>
     *
     * @param field
     * generated property with data about field which is generated as the getter method
     * @return string with the getter method source code in JAVA format
     */
    protected def getterMethod(GeneratedProperty field) {
        '''
            public «field.returnType.importedName» «field.getterMethodName»() {
                «val fieldName = field.fieldName»
                «IF field.returnType.name.endsWith("[]")»
                return «fieldName» == null ? null : «fieldName».clone();
                «ELSE»
                return «fieldName»;
                «ENDIF»
            }
        '''
    }

    final protected def getterMethodName(GeneratedProperty field) {
        val prefix = if(field.returnType.equals(Types.BOOLEAN)) "is" else "get"
        return '''«prefix»«field.name.toFirstUpper»'''
    }

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
     * Template method which generates method parameters with their types from <code>parameters</code>.
     *
     * @param parameters
     * group of generated property instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    def final protected asArgumentsDeclaration(Iterable<GeneratedProperty> parameters) '''«IF !parameters.empty»«FOR parameter : parameters SEPARATOR ", "»«parameter.
        returnType.importedName» «parameter.fieldName»«ENDFOR»«ENDIF»'''

    /**
     * Template method which generates sequence of the names of the class attributes from <code>parameters</code>.
     *
     * @param parameters
     * group of generated property instances which are transformed to the sequence of parameter names
     * @return string with the list of the parameter names of the <code>parameters</code>
     */
    def final protected asArguments(Collection<GeneratedProperty> parameters) '''«IF !parameters.empty»«FOR parameter : parameters SEPARATOR ", "»«parameter.
        fieldName»«ENDFOR»«ENDIF»'''

    /**
     * Template method which generates JAVA comments.
     *
     * @param comment string with the comment for whole JAVA class
     * @return string with comment in JAVA format
     */
    def protected CharSequence asJavadoc(String comment) {
        if (comment === null) {
            return ''
        }
        return '''
            «wrapToDocumentation(formatToParagraph(comment.trim))»
        '''
    }

    def static String wrapToDocumentation(String text) {
        if (text.empty)
            return ""

        val StringBuilder sb = new StringBuilder().append("/**\n")
        for (String t : NL_SPLITTER.split(text)) {
            sb.append(" *")
            if (!t.isEmpty()) {
                sb.append(SPACE).append(t)
            }
            sb.append(NEW_LINE)
        }
        sb.append(" */")

        return sb.toString
    }

    def protected String formatDataForJavaDoc(GeneratedType type) {
        val sb = new StringBuilder()
        val comment = type.comment
        if (comment !== null) {
            sb.append(comment.javadoc)
        }

        appendSnippet(sb, type)

        return '''
            «IF sb.length != 0»
            «sb.toString»
            «ENDIF»
        '''.toString
    }

    def static encodeJavadocSymbols(String description) {
        if (description.nullOrEmpty) {
            return description;
        }

        return TAIL_COMMENT_PATTERN.matcher(AMP_MATCHER.replaceFrom(description, "&amp;")).replaceAll("&#42;&#47;")
    }

    def protected String formatDataForJavaDoc(GeneratedType type, String additionalComment) {
        val comment = type.comment
        if (comment === null) {
            return '''
                «additionalComment»
            '''
        }

        val sb = new StringBuilder().append(comment.javadoc)
        appendSnippet(sb, type)

        sb.append(NEW_LINE)
        .append(NEW_LINE)
        .append(NEW_LINE)
        .append(additionalComment)

        return '''
            «sb.toString»
        '''
    }

    def private static void appendSnippet(StringBuilder sb, GeneratedType type) {
        val optDef = type.yangSourceDefinition
        if (optDef.present) {
            val def = optDef.get
            sb.append(NEW_LINE)

            if (def instanceof Single) {
                val node = def.node
                sb.append("<p>\n")
                .append("This class represents the following YANG schema fragment defined in module <b>")
                .append(def.module.argument).append("</b>\n")
                .append("<pre>\n")
                appendYangSnippet(sb, def.module, (node as EffectiveStatement<?, ?>).declared)
                sb.append("</pre>")

                if (node instanceof SchemaNode) {
                    sb.append("The schema path to identify an instance is\n")
                    .append("<i>")
                    .append(formatSchemaPath(def.module.argument, node.path.pathFromRoot))
                    .append("</i>\n")

                    if (hasBuilderClass(node)) {
                        val builderName = type.name + "Builder";

                        sb.append("\n<p>To create instances of this class use {@link ").append(builderName)
                        .append("}.\n")
                        .append("@see ").append(builderName).append('\n')
                        if (node instanceof ListSchemaNode) {
                            val keyDef = node.keyDefinition
                            if (keyDef !== null && !keyDef.empty) {
                                sb.append("@see ").append(type.name).append("Key")
                            }
                            sb.append('\n');
                        }
                    }
                }
            } else if (def instanceof Multiple) {
                sb.append("<pre>\n")
                for (SchemaNode node : def.nodes) {
                    appendYangSnippet(sb, def.module, (node as EffectiveStatement<?, ?>).declared)
                }
                sb.append("</pre>\n")
            }
        }
    }

    def private static void appendYangSnippet(StringBuilder sb, ModuleEffectiveStatement module,
            DeclaredStatement<?> stmt) {
        for (String str : YANG_FORMATTER.toYangTextSnippet(module, stmt)) {
            sb.append(BindingGeneratorUtil.replaceAllIllegalChars(encodeAngleBrackets(encodeJavadocSymbols(str))))
        }
    }

    def private static boolean hasBuilderClass(SchemaNode schemaNode) {
        return schemaNode instanceof ContainerSchemaNode || schemaNode instanceof ListSchemaNode
                || schemaNode instanceof RpcDefinition || schemaNode instanceof NotificationDefinition;
    }

    def private static String formatSchemaPath(String moduleName, Iterable<QName> schemaPath) {
        val sb = new StringBuilder().append(moduleName);

        var currentElement = Iterables.getFirst(schemaPath, null);
        for (QName pathElement : schemaPath) {
            sb.append('/')
            if (!currentElement.namespace.equals(pathElement.namespace)) {
                currentElement = pathElement
                sb.append(pathElement)
            } else {
                sb.append(pathElement.getLocalName())
            }
        }
        return sb.toString();
    }

    def protected static String formatDataForJavaDoc(TypeMember type, String additionalComment) {
        val StringBuilder typeDescriptionBuilder = new StringBuilder();
        if (!type.comment.nullOrEmpty) {
            typeDescriptionBuilder.append(formatToParagraph(type.comment))
            typeDescriptionBuilder.append(NEW_LINE)
            typeDescriptionBuilder.append(NEW_LINE)
            typeDescriptionBuilder.append(NEW_LINE)
        }
        typeDescriptionBuilder.append(additionalComment)
        var typeDescription = wrapToDocumentation(typeDescriptionBuilder.toString)
        return '''
            «typeDescription»
        '''.toString
    }

    def asCode(String text) {
        return "<code>" + text + "</code>"
    }

    def asLink(String text) {
        val StringBuilder sb = new StringBuilder()
        var tempText = text
        var char lastChar = SPACE
        var boolean badEnding = false

        if (text.endsWith('.') || text.endsWith(':') || text.endsWith(',')) {
            tempText = text.substring(0, text.length - 1)
            lastChar = text.charAt(text.length - 1)
            badEnding = true
        }
        sb.append("<a href = \"")
        sb.append(tempText)
        sb.append("\">")
        sb.append(tempText)
        sb.append("</a>")

        if(badEnding)
            sb.append(lastChar)

        return sb.toString
    }

    protected static def formatToParagraph(String text) {
        if(text === null || text.isEmpty)
            return text

        var formattedText = text
        val StringBuilder sb = new StringBuilder();
        var StringBuilder lineBuilder = new StringBuilder();
        var boolean isFirstElementOnNewLineEmptyChar = false;

        formattedText = encodeJavadocSymbols(formattedText)
        formattedText = WS_MATCHER.replaceFrom(formattedText, SPACE)
        formattedText = SPACES_PATTERN.matcher(formattedText).replaceAll(" ")

        val StringTokenizer tokenizer = new StringTokenizer(formattedText, " ", true);

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

                if (nextElement == " ") {
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

    def protected generateToString(Collection<? extends GeneratedProperty> properties) '''
        «IF !properties.empty»
            @«OVERRIDE.importedName»
            public «STRING.importedName» toString() {
                final «MoreObjects.importedName».ToStringHelper helper = «MoreObjects.importedName».toStringHelper(«type.importedName».class);
                «FOR property : properties»
                    «CODEHELPERS.importedName».appendValue(helper, "«property.fieldName»", «property.fieldName»);
                «ENDFOR»
                return helper.toString();
            }
        «ENDIF»
    '''

    /**
     * Template method which generates method parameters with their types from <code>parameters</code>.
     *
     * @param parameters
     * list of parameter instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    def protected generateParameters(List<MethodSignature.Parameter> parameters) '''«
        IF !parameters.empty»«
            FOR parameter : parameters SEPARATOR ", "»«
                parameter.type.importedName» «parameter.name»«
            ENDFOR»«
        ENDIF
    »'''

    def protected emitConstant(Constant c) '''
        «IF BindingMapping.QNAME_STATIC_FIELD_NAME.equals(c.name)»
            «val entry = c.value as Entry<JavaTypeName, String>»
            public static final «c.type.importedNonNull» «c.name» = «entry.key.importedName».«BindingMapping.MODULE_INFO_QNAMEOF_METHOD_NAME»("«entry.value»");
        «ELSE»
            public static final «c.type.importedName» «c.name» = «c.value»;
        «ENDIF»
    '''

    def protected generateCheckers(GeneratedProperty field, Restrictions restrictions, Type actualType) '''
       «IF restrictions.rangeConstraint.present»
           «AbstractRangeGenerator.forType(actualType).generateRangeChecker(field.name.toFirstUpper,
               restrictions.rangeConstraint.get, this)»
       «ENDIF»
       «IF restrictions.lengthConstraint.present»
           «LengthGenerator.generateLengthChecker(field.fieldName, actualType, restrictions.lengthConstraint.get, this)»
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

       «val fieldUpperCase = fieldName.toUpperCase(Locale.ENGLISH)»
       «FOR currentConstant : type.getConstantDefinitions»
           «IF currentConstant.getName.startsWith(TypeConstants.PATTERN_CONSTANT_NAME)
               && fieldUpperCase.equals(currentConstant.getName.substring(TypeConstants.PATTERN_CONSTANT_NAME.length))»
           «CODEHELPERS.importedName».checkPattern(value, «Constants.MEMBER_PATTERN_LIST»«fieldName», «Constants.MEMBER_REGEX_LIST»«fieldName»);
           «ENDIF»
       «ENDFOR»
    '''

    def protected hashCodeResult(Collection<? extends GeneratedProperty> properties) '''
        final int prime = 31;
        int result = 1;
        «FOR property : properties»
            result = prime * result + «property.importedUtilClass».hashCode(«property.fieldName»);
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
