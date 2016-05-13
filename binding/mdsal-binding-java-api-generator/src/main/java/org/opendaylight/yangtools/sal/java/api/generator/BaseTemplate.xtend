/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator

import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import java.util.Arrays
import java.util.Collection
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.StringTokenizer
import java.util.regex.Pattern
import org.opendaylight.yangtools.binding.generator.util.Types
import org.opendaylight.yangtools.sal.binding.model.api.ConcreteType
import org.opendaylight.yangtools.sal.binding.model.api.Constant
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions
import org.opendaylight.yangtools.sal.binding.model.api.Type
import org.opendaylight.yangtools.yang.common.QName

abstract class BaseTemplate {
    protected val GeneratedType type;
    protected val Map<String, String> importMap;

    private static final char NEW_LINE = '\n'
    private static final CharMatcher NL_MATCHER = CharMatcher.is(NEW_LINE)
    private static final CharMatcher TAB_MATCHER = CharMatcher.is('\t')
    private static final Pattern SPACES_PATTERN = Pattern.compile(" +")
    private static final Splitter NL_SPLITTER = Splitter.on(NL_MATCHER)

    new(GeneratedType _type) {
        if (_type == null) {
            throw new IllegalArgumentException("Generated type reference cannot be NULL!")
        }
        this.type = _type;
        this.importMap = new HashMap<String,String>()
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
        «IF !importMap.empty»
            «FOR entry : importMap.entrySet»
                «IF !hasSamePackage(entry.value)»
                    import «entry.value».«entry.key»;
                «ENDIF»
            «ENDFOR»
        «ENDIF»

    '''

    /**
     * Checks if packages of generated type and imported type is the same
     *
     * @param importedTypePackageNam
     * the package name of imported type
     * @return true if the packages are the same false otherwise
     */
    final private def boolean hasSamePackage(String importedTypePackageName) {
        return type.packageName.equals(importedTypePackageName);
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

    /**
     * Template method which generates the getter method for <code>field</code>
     *
     * @param field
     * generated property with data about field which is generated as the getter method
     * @return string with the getter method source code in JAVA format
     */
    final protected def getterMethod(GeneratedProperty field) {
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
        if(comment == null) return ''
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

        val StringBuilder sb = new StringBuilder("/**")
        sb.append(NEW_LINE)

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
        val typeDescription = type.getDescription().encodeJavadocSymbols;

        return '''
            «IF !typeDescription.nullOrEmpty»
            «typeDescription»
            «ENDIF»
        '''.toString
    }

    private static final CharMatcher AMP_MATCHER = CharMatcher.is('&');

    def encodeJavadocSymbols(String description) {
        if (description.nullOrEmpty) {
            return description;
        }

        var ret = description.replace("*/", "&#42;&#47;");
        ret = AMP_MATCHER.replaceFrom(ret, "&amp;");

        return ret;
    }

    def protected String formatDataForJavaDoc(GeneratedType type, String additionalComment) {
        val StringBuilder typeDescription = new StringBuilder();
        if (!type.description.nullOrEmpty) {
            typeDescription.append(type.description)
            typeDescription.append(NEW_LINE)
            typeDescription.append(NEW_LINE)
            typeDescription.append(NEW_LINE)
            typeDescription.append(additionalComment)
        } else {
            typeDescription.append(additionalComment)
        }

        return '''
            «typeDescription.toString»
        '''.toString
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
        if(text == null || text.isEmpty)
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

        while(tokenizer.hasMoreElements) {
            val nextElement = tokenizer.nextElement.toString

            if(lineBuilder.length + nextElement.length > 80) {
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

            if(isFirstElementOnNewLineEmptyChar) {
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
        if (parent != null) {
            return findProperty(parent, name)
        }
        return null
    }

    def protected emitConstant(Constant c) '''
        «IF c.value instanceof QName»
            «val qname = c.value as QName»
            public static final «c.type.importedName» «c.name» = «QName.name».create("«qname.namespace.toString»",
                "«qname.formattedRevision»", "«qname.localName»").intern();
        «ELSE»
            public static final «c.type.importedName» «c.name» = «c.value»;
        «ENDIF»
    '''
}
