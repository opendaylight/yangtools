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
import com.google.common.collect.ImmutableList
import com.google.common.collect.Range
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Arrays
import java.util.Collection
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.StringTokenizer
import java.util.regex.Pattern
import org.opendaylight.yangtools.binding.generator.util.Types
import org.opendaylight.yangtools.sal.binding.model.api.ConcreteType
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions
import org.opendaylight.yangtools.sal.binding.model.api.Type

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

    protected def getFullyQualifiedName() {
        return type.fullyQualifiedName
    }

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
    private static final CharMatcher GT_MATCHER = CharMatcher.is('>');
    private static final CharMatcher LT_MATCHER = CharMatcher.is('<');

    def encodeJavadocSymbols(String description) {
        if (description.nullOrEmpty) {
            return description;
        }

        var ret = description.replace("*/", "&#42;&#47;")

        // FIXME: Use Guava's HtmlEscapers once we have it available
        ret = AMP_MATCHER.replaceFrom(ret, "&amp;");
        ret = GT_MATCHER.replaceFrom(ret, "&gt;");
        ret = LT_MATCHER.replaceFrom(ret, "&lt;");
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

        formattedText = formattedText.encodeJavadocSymbols
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

    /**
     * Print length constraint.
     * This should always be a BigInteger (only string and binary can have length restriction)
     */
    def printLengthConstraint(Type returnType, Class<? extends Number> clazz, String paramName, boolean isNestedType, boolean isArray) '''
        «clazz.importedNumber» _constraint = «clazz.importedNumber».valueOf(«paramName»«IF isNestedType».getValue()«ENDIF».length«IF !isArray»()«ENDIF»);
    '''

    def printRangeConstraint(Type returnType, String paramName, boolean isNestedType) '''
        «IF BigDecimal.canonicalName.equals(returnType.fullyQualifiedName)»
            «BigDecimal.importedName» _constraint = new «BigDecimal.importedName»(«paramName»«IF isNestedType».getValue()«ENDIF».toString());
        «ELSE»
            «IF isNestedType»
                «val propReturnType = findProperty(returnType as GeneratedTransferObject, "value").returnType»
                «IF propReturnType.fullyQualifiedName.equals(BigInteger.canonicalName)»
                    «BigInteger.importedName» _constraint = «paramName».getValue();
                «ELSE»
                    «BigInteger.importedName» _constraint = «BigInteger.importedName».valueOf(«paramName».getValue());
                «ENDIF»
            «ELSE»
                «IF returnType.fullyQualifiedName.equals(BigInteger.canonicalName)»
                    «BigInteger.importedName» _constraint = «paramName»;
                «ELSE»
                    «BigInteger.importedName» _constraint = «BigInteger.importedName».valueOf(«paramName»);
                «ENDIF»
            «ENDIF»
        «ENDIF»
    '''

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

    def boolean isArrayType(GeneratedTransferObject type) {
        var isArray = false
        val GeneratedProperty value = findProperty(type, "value")
        if (value != null && value.returnType.name.contains("[")) {
            isArray = true
        }
        return isArray
    }

    def String toQuote(Object obj) {
        return "\"" + obj.toString + "\"";
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

    def protected generateLengthMethod(String methodName, Type type, String className, String varName) '''
        «val Restrictions restrictions = type.restrictions»
        «IF restrictions != null && !(restrictions.lengthConstraints.empty)»
            «val numberClass = restrictions.lengthConstraints.iterator.next.min.class»
            public static «List.importedName»<«Range.importedName»<«numberClass.importedNumber»>> «methodName»() {
                «IF numberClass.equals(typeof(BigDecimal))»
                    «lengthBody(restrictions, numberClass, className, varName)»
                «ELSE»
                    «lengthBody(restrictions, typeof(BigInteger), className, varName)»
                «ENDIF»
            }
        «ENDIF»
    '''

    def private lengthBody(Restrictions restrictions, Class<? extends Number> numberClass, String className, String varName) '''
        if («varName» == null) {
            synchronized («className».class) {
                if («varName» == null) {
                    «ImmutableList.importedName».Builder<«Range.importedName»<«numberClass.importedName»>> builder = «ImmutableList.importedName».builder();
                    «FOR r : restrictions.lengthConstraints»
                        builder.add(«Range.importedName».closed(«numericValue(numberClass, r.min)», «numericValue(numberClass, r.max)»));
                    «ENDFOR»
                    «varName» = builder.build();
                }
            }
        }
        return «varName»;
    '''

    def protected String importedNumber(Class<? extends Number> clazz) {
        if (clazz.equals(typeof(BigDecimal))) {
            return BigDecimal.importedName
        }
        return BigInteger.importedName
    }

    def protected String importedNumber(Type clazz) {
        if (clazz.fullyQualifiedName.equals(BigDecimal.canonicalName)) {
            return BigDecimal.importedName
        }
        return BigInteger.importedName
    }

    def protected String numericValue(Class<? extends Number> clazz, Object numberValue) {
        val number = clazz.importedName;
        val value = numberValue.toString
        if (clazz.equals(typeof(BigInteger)) || clazz.equals(typeof(BigDecimal))) {
            if (value.equals("0")) {
                return number + ".ZERO"
            } else if (value.equals("1")) {
                return number + ".ONE"
            } else if (value.equals("10")) {
                return number + ".TEN"
            } else {
                try {
                    val Long longVal = Long.valueOf(value)
                    return number + ".valueOf(" + longVal + "L)"
                } catch (NumberFormatException e) {
                    if (clazz.equals(typeof(BigDecimal))) {
                        try {
                            val Double doubleVal = Double.valueOf(value);
                            return number + ".valueOf(" + doubleVal + ")"
                        } catch (NumberFormatException e2) {
                        }
                    }
                }
            }
        }
        return "new " + number + "(\"" + value + "\")"
    }

    def private GeneratedProperty findProperty(GeneratedTransferObject gto, String name) {
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

}
