/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil.encodeAngleBrackets

import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import com.google.common.collect.Iterables
import java.util.Arrays
import java.util.Collection
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.StringTokenizer
import java.util.regex.Pattern
import org.opendaylight.mdsal.binding.model.api.ConcreteType
import org.opendaylight.mdsal.binding.model.api.Constant
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject
import org.opendaylight.mdsal.binding.model.api.GeneratedType
import org.opendaylight.mdsal.binding.model.api.MethodSignature
import org.opendaylight.mdsal.binding.model.api.Restrictions
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.mdsal.binding.model.api.TypeMember
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition.Single
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition.Multiple
import org.opendaylight.mdsal.binding.model.util.Types
import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode
import org.opendaylight.yangtools.yang.model.api.DocumentedNode
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition
import org.opendaylight.yangtools.yang.model.api.RpcDefinition
import org.opendaylight.yangtools.yang.model.api.SchemaNode

abstract class BaseTemplate {
    protected val Map<String, String> importMap = new HashMap()
    protected val GeneratedType type;

    private static final char NEW_LINE = '\n'
    private static val AMP_MATCHER = CharMatcher.is('&')
    private static val NL_MATCHER = CharMatcher.is(NEW_LINE)
    private static val TAB_MATCHER = CharMatcher.is('\t')
    private static val SPACES_PATTERN = Pattern.compile(" +")
    private static val NL_SPLITTER = Splitter.on(NL_MATCHER)
    private static val TAIL_COMMENT_PATTERN = Pattern.compile("*/", Pattern.LITERAL);

    new(GeneratedType _type) {
        if (_type === null) {
            throw new IllegalArgumentException("Generated type reference cannot be NULL!")
        }
        this.type = _type;
    }

    def packageDefinition() '''package «type.packageName»;'''

    final public def generate() {
        val _body = body()
        '''
            «packageDefinition»
            «imports»

            «_body»
        '''.toString
    }

    protected def imports() '''
        «FOR entry : importMap.entrySet»
            «IF !hasSamePackage(entry.value) && !isLocalInnerClass(entry.value)»
                import «entry.value».«entry.key»;
            «ENDIF»
        «ENDFOR»
    '''

    /**
     * Checks if packages of generated type and imported type is the same
     *
     * @param importedTypePackageName the package name of imported type
     * @return true if the packages are the same false otherwise
     */
    final private def boolean hasSamePackage(String importedTypePackageName) {
        return type.packageName.equals(importedTypePackageName);
    }

    def isLocalInnerClass(String importedTypePackageName) {
        return type.fullyQualifiedName.equals(importedTypePackageName);
    }

    protected abstract def CharSequence body();

    // Helper patterns
    final protected def fieldName(GeneratedProperty property) '''_«property.name»'''

    final protected def propertyNameFromGetter(MethodSignature getter) {
        var int prefix;
        if (getter.name.startsWith("is")) {
            prefix = 2
        } else if (getter.name.startsWith("get")) {
            prefix = 3
        } else {
            throw new IllegalArgumentException("Not a getter")
        }
        return getter.name.substring(prefix).toFirstLower;
    }

    final protected def isAccessor(MethodSignature maybeGetter) {
        return maybeGetter.name.startsWith("is") || maybeGetter.name.startsWith("get");
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
                «IF field.returnType.importedName.contains("[]")»
                return «field.fieldName» == null ? null : «field.fieldName».clone();
                «ELSE»
                return «field.fieldName»;
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

    final protected def importedName(Type intype) {
        GeneratorUtil.putTypeIntoImports(type, intype, importMap);
        GeneratorUtil.getExplicitType(type, intype, importMap)
    }

    final protected def importedName(Class<?> cls) {
        importedName(Types.typeForClass(cls))
    }

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
    def final protected asArguments(Iterable<GeneratedProperty> parameters) '''«IF !parameters.empty»«FOR parameter : parameters SEPARATOR ", "»«parameter.
        fieldName»«ENDFOR»«ENDIF»'''

    /**
     * Template method which generates JAVA comments.
     *
     * @param comment string with the comment for whole JAVA class
     * @return string with comment in JAVA format
     */
    def protected CharSequence asJavadoc(String comment) {
        if(comment === null) return ''
        var txt = comment

        txt = comment.trim
        txt = formatToParagraph(txt)

        return '''
            «wrapToDocumentation(txt)»
        '''
    }

    def String wrapToDocumentation(String text) {
        if (text.empty)
            return ""

        val StringBuilder sb = new StringBuilder().append("/**\n")
        for (String t : NL_SPLITTER.split(text)) {
            sb.append(" *")
            if (!t.isEmpty()) {
                sb.append(' ');
                sb.append(t)
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
                .append(def.module.name).append("</b>\n")
                .append("<pre>\n")
                .append(encodeAngleBrackets(encodeJavadocSymbols(YangTemplate.generateYangSnippet(node))))
                .append("</pre>")

                if (node instanceof SchemaNode) {
                    sb.append("The schema path to identify an instance is\n")
                    .append("<i>")
                    .append(formatSchemaPath(def.module.name, node.path.pathFromRoot))
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
                for (DocumentedNode schemaNode : def.nodes) {
                    sb.append(encodeAngleBrackets(encodeJavadocSymbols(YangTemplate.generateYangSnippet(schemaNode))))
                }
                sb.append("</pre>\n")
            }
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

    def protected String formatDataForJavaDoc(TypeMember type, String additionalComment) {
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
        var char lastChar = ' '
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

    protected def formatToParagraph(String text) {
        if(text === null || text.isEmpty)
            return text

        var formattedText = text
        val StringBuilder sb = new StringBuilder();
        var StringBuilder lineBuilder = new StringBuilder();
        var boolean isFirstElementOnNewLineEmptyChar = false;

        formattedText = encodeJavadocSymbols(formattedText)
        formattedText = NL_MATCHER.removeFrom(formattedText)
        formattedText = TAB_MATCHER.removeFrom(formattedText)
        formattedText = SPACES_PATTERN.matcher(formattedText).replaceAll(" ")

        val StringTokenizer tokenizer = new StringTokenizer(formattedText, " ", true);

        while (tokenizer.hasMoreElements) {
            val nextElement = tokenizer.nextElement.toString

            if (lineBuilder.length != 0 && lineBuilder.length + nextElement.length > 80) {
                if (lineBuilder.charAt(lineBuilder.length - 1) == ' ') {
                    lineBuilder.setLength(0)
                    lineBuilder.append(lineBuilder.substring(0, lineBuilder.length - 1))
                }
                if (lineBuilder.charAt(0) == ' ') {
                    lineBuilder.setLength(0)
                    lineBuilder.append(lineBuilder.substring(1))
                }

                sb.append(lineBuilder);
                lineBuilder.setLength(0)
                sb.append(NEW_LINE)

                if(nextElement.toString == ' ') {
                    isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar;
                }
            }

            if (isFirstElementOnNewLineEmptyChar) {
                isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar
            }

            else {
                lineBuilder.append(nextElement)
            }
        }
        sb.append(lineBuilder)
        sb.append(NEW_LINE)

        return sb.toString
    }

    def protected generateToString(Collection<GeneratedProperty> properties) '''
        «IF !properties.empty»
            @Override
            public «String.importedName» toString() {
                «StringBuilder.importedName» builder = new «StringBuilder.importedName»(«type.importedName».class.getSimpleName()).append(" [");
                boolean first = true;

                «FOR property : properties»
                    if («property.fieldName» != null) {
                        if (first) {
                            first = false;
                        } else {
                            builder.append(", ");
                        }
                        builder.append("«property.fieldName»=");
                        «IF property.returnType.name.contains("[")»
                            builder.append(«Arrays.importedName».toString(«property.fieldName»));
                        «ELSE»
                            builder.append(«property.fieldName»);
                        «ENDIF»
                    }
                «ENDFOR»
                return builder.append(']').toString();
            }
        «ENDIF»
    '''

    def getRestrictions(Type type) {
        var Restrictions restrictions = null
        if (type instanceof ConcreteType) {
            restrictions = type.restrictions
        } else if (type instanceof GeneratedTransferObject) {
            restrictions = type.restrictions
        }
        return restrictions
    }

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

    def protected GeneratedProperty findProperty(GeneratedTransferObject gto, String name) {
        val props = gto.properties
        for (prop : props) {
            if (prop.name.equals(name)) {
                return prop
            }
        }
        val GeneratedTransferObject parent = gto.superType
        if (parent !== null) {
            return findProperty(parent, name)
        }
        return null
    }

    def protected emitConstant(Constant c) '''
        «IF c.value instanceof QName»
            «val qname = c.value as QName»
            «val rev = qname.revision»
            public static final «c.type.importedName» «c.name» = «QName.name».create("«qname.namespace.toString»",
                «IF rev.isPresent»"«rev.get»", «ENDIF»"«qname.localName»").intern();
        «ELSE»
            public static final «c.type.importedName» «c.name» = «c.value»;
        «ENDIF»
    '''
}
