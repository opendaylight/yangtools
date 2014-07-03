/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator

import com.google.common.collect.Range
import java.util.ArrayList
import java.util.Arrays
import java.util.Collection
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.StringTokenizer
import org.opendaylight.yangtools.binding.generator.util.Types
import org.opendaylight.yangtools.sal.binding.model.api.ConcreteType
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
    
    private static final String NEW_LINE = "\n"

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
                «IF entry.value != fullyQualifiedName»
                    import «entry.value».«entry.key»;
                «ENDIF»
            «ENDFOR»
        «ENDIF»
        
    '''

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
                return «field.fieldName»;
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
        if (txt.contains("*/")) {
            txt = txt.replace("*/", "&#42;&#47;")
        }
        txt = comment.trim
        txt = formatToParagraph(txt)

        return '''
            «wrapToDucumentation(txt)»
        '''
    }
    
    def String wrapToDucumentation(String text) {
        val StringTokenizer tokenizer = new StringTokenizer(text, NEW_LINE, false)
        val StringBuilder sb = new StringBuilder()
        
        if(text.empty)
            return ""
        
        sb.append("/**")
        sb.append(NEW_LINE)
        
        while(tokenizer.hasMoreTokens) {
            sb.append(" * ")
            sb.append(tokenizer.nextToken)
            sb.append(NEW_LINE)
        }
        sb.append(" */")
        
        return sb.toString
    }
    
    def protected String formatDataForJavaDoc(GeneratedType type) {
        val typeDescription = type.description
        val typeReference = type.reference
        val typeModuleName = type.moduleName
        val typeSchemaPath = type.schemaPath
        
        return '''
            «IF !type.isDocumentationParametersNullOrEmtpy»
               «IF typeDescription != null && !typeDescription.empty»
                «formatToParagraph(typeDescription)»
               «ENDIF»
               «IF typeReference != null && !typeReference.empty»
                Reference:
                    «formatReference(typeReference)»
               «ENDIF»
               «IF typeModuleName != null && !typeModuleName.empty»
                Module name:
                    «typeModuleName»
               «ENDIF»
               «IF typeSchemaPath != null && !typeSchemaPath.empty»
                Schema path:
                    «formatPath(typeSchemaPath)»
               «ENDIF»
            «ENDIF»
        '''.toString
    }
    
    def formatPath(Iterable<QName> schemaPath) {
        var currentElement = schemaPath.head
        val StringBuilder sb = new StringBuilder()
        sb.append("[")
        
        sb.append(currentElement)
        
        for(pathElement : schemaPath) {
            if(!currentElement.namespace.equals(pathElement.namespace)) {
                currentElement = pathElement
                sb.append("/")
                sb.append(pathElement)
            }
            else {
                sb.append("/")
                sb.append(pathElement.localName)   
            } 
        }
        sb.append("]")
        
        return sb.toString
    }
    
    def formatReference(String reference) {
        if(reference == null || reference.isEmpty)
            return reference
            
        val StringTokenizer tokenizer = new StringTokenizer(reference, " ", true)
        val StringBuilder sb = new StringBuilder();
        
        while(tokenizer.hasMoreTokens) {
            var String oneElement = tokenizer.nextToken
            if (oneElement.contains("http://")) {
                oneElement = asLink(oneElement)
            }
            sb.append(oneElement)
        }
        return sb.toString
    }
    
    def asLink(String text) {
        val StringBuilder sb = new StringBuilder()
        var tempText = text
        var char lastChar = ' '
        var boolean badEnding = false
        
        if(text.endsWith(".") || text.endsWith(":") || text.endsWith(",")) {
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
        
        formattedText = formattedText.replace("*/", "&#42;&#47;")
        formattedText = formattedText.replace(NEW_LINE, "")
        formattedText = formattedText.replace("\t", "")
        formattedText = formattedText.replaceAll(" +", " ");
            
        val StringTokenizer tokenizer = new StringTokenizer(formattedText, " ", true);
        
        while(tokenizer.hasMoreElements) {
            val nextElement = tokenizer.nextElement.toString
                
            if(lineBuilder.length + nextElement.length > 80) {
                if (lineBuilder.charAt(lineBuilder.length - 1) == ' ') {
                    lineBuilder.setLength(0)
                    lineBuilder.append(lineBuilder.toString.substring(0, lineBuilder.length - 1))
                }
                if (lineBuilder.toString.charAt(0) == ' ') {
                    lineBuilder.setLength(0)
                    lineBuilder.append(lineBuilder.toString.substring(1))
                }
                    
                sb.append(lineBuilder);
                lineBuilder.setLength(0)
                sb.append(NEW_LINE)
                    
                if(nextElement.toString == ' ')
                    isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar;
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
    
    def isDocumentationParametersNullOrEmtpy(GeneratedType type) {
        var boolean isNull = true
        val String typeDescription = type.description
        val String typeReference = type.reference
        val String typeModuleName = type.moduleName
        val Iterable<QName> typeSchemaPath = type.schemaPath
        
        if(typeDescription != null && !typeDescription.empty) {
            isNull = false
            return isNull    
        }
        if(typeReference != null && !typeReference.empty) {
            isNull = false
            return isNull            
        }
        if(typeModuleName != null && !typeModuleName.empty) {
            isNull = false
            return isNull    
        }
        if(typeSchemaPath != null && !typeSchemaPath.empty) {
            isNull = false
            return isNull
        }    
        return isNull
    }

    def generateRestrictions(Type type, String paramName, Type returnType) '''
        «val boolean isArray = returnType.name.contains("[")»
        «processRestrictions(type, paramName, returnType, isArray)»
    '''

    def generateRestrictions(GeneratedProperty field, String paramName) '''
        «val Type type = field.returnType»
        «IF type instanceof ConcreteType»
            «processRestrictions(type, paramName, field.returnType, type.name.contains("["))»
        «ELSEIF type instanceof GeneratedTransferObject»
            «processRestrictions(type, paramName, field.returnType, isArrayType(type as GeneratedTransferObject))»
        «ENDIF»
    '''


    private def processRestrictions(Type type, String paramName, Type returnType, boolean isArray) '''
        «val restrictions = type.getRestrictions»
        «IF restrictions !== null»
            «IF !restrictions.lengthConstraints.empty»
                «generateLengthRestriction(type, restrictions, paramName, isArray,
            !(returnType instanceof ConcreteType))»
            «ENDIF»
            «IF !restrictions.rangeConstraints.empty &&
            ("java.lang".equals(returnType.packageName) || "java.math".equals(returnType.packageName))»
                «generateRangeRestriction(type, returnType, restrictions, paramName,
            !(returnType instanceof ConcreteType))»
            «ENDIF»
        «ENDIF»
    '''

    def generateLengthRestriction(Type type, Restrictions restrictions, String paramName, boolean isArray,
        boolean isNestedType) '''
        if («paramName» != null) {
            boolean isValidLength = false;
            «List.importedName»<«Range.importedName»<«Integer.importedName»>> lengthConstraints = new «ArrayList.
            importedName»<>(); 
            «FOR r : restrictions.lengthConstraints»
                lengthConstraints.add(«Range.importedName».closed(«r.min», «r.max»));
            «ENDFOR»
            for («Range.importedName»<«Integer.importedName»> r : lengthConstraints) {
                «IF isArray»
                    «IF isNestedType»
                        if (r.contains(«paramName».getValue().length)) {
                    «ELSE»
                        if (r.contains(«paramName».length)) {
                    «ENDIF»
                «ELSE»
                    «IF isNestedType»
                        if (r.contains(«paramName».getValue().length())) {
                    «ELSE»
                        if (r.contains(«paramName».length())) {
                    «ENDIF»
                «ENDIF»
                isValidLength = true;
                }
            }
            if (!isValidLength) {
                throw new IllegalArgumentException(String.format("Invalid length: %s, expected: %s.", «paramName», lengthConstraints));
            }
        }
    '''

    def generateRangeRestriction(Type type, Type returnType, Restrictions restrictions, String paramName,
        boolean isNestedType) '''
        «val javaType = Class.forName(returnType.fullyQualifiedName)»
        if («paramName» != null) {
            boolean isValidRange = false;
            «List.importedName»<«Range.importedName»<«javaType.importedName»>> rangeConstraints = new «ArrayList.
            importedName»<>(); 
            «FOR r : restrictions.rangeConstraints»
                rangeConstraints.add(«Range.importedName».closed(new «javaType.importedName»(«r.min.toQuote»), new «javaType.
            importedName»(«r.max.toQuote»)));
            «ENDFOR»
            for («Range.importedName»<«javaType.importedName»> r : rangeConstraints) {
                «IF isNestedType»
                    if (r.contains(«paramName».getValue())) {
                «ELSE»
                    if (r.contains(«paramName»)) {
                «ENDIF»
                isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", «paramName», rangeConstraints));
            }
        }
    '''

    def protected generateToString(Collection<GeneratedProperty> properties) '''
        «IF !properties.empty»
            @Override
            public «String.importedName» toString() {
                «StringBuilder.importedName» builder = new «StringBuilder.importedName»("«type.name» [");
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

    def GeneratedProperty getPropByName(GeneratedType gt, String name) {
        for (GeneratedProperty prop : gt.properties) {
            if (prop.name.equals(name)) {
                return prop;
            }
        }
        return null;
    }

    def getRestrictions(Type type) {
        var Restrictions restrictions = null
        if (type instanceof ConcreteType) {
            restrictions = (type as ConcreteType).restrictions
        } else if (type instanceof GeneratedTransferObject) {
            restrictions = (type as GeneratedTransferObject).restrictions
        }
        return restrictions
    }

    def boolean isArrayType(GeneratedTransferObject type) {
        var isArray = false
        val GeneratedTransferObject superType = type.findSuperType
        val GeneratedProperty value = superType.getPropByName("value")
        if (value != null && value.returnType.name.contains("[")) {
            isArray = true
        }
        return isArray
    }

    def GeneratedTransferObject findSuperType(GeneratedTransferObject gto) {
        var GeneratedTransferObject base = gto
        var GeneratedTransferObject superType = base.superType
        while (superType !== null) {
            base = superType
            superType = base.superType
        }
        return base;
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

}
