/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator

import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType
import java.util.Map
import org.opendaylight.yangtools.sal.binding.model.api.Type
import org.opendaylight.yangtools.binding.generator.util.Types
import com.google.common.base.Splitter
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature
import com.google.common.collect.Range
import java.util.ArrayList
import java.util.List
import org.opendaylight.yangtools.sal.binding.model.api.ConcreteType
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import java.util.Collection
import java.util.Arrays

abstract class BaseTemplate {

    protected val GeneratedType type;
    protected val Map<String, String> importMap;
    static val paragraphSplitter = Splitter.on("\n\n").omitEmptyStrings();

    new(GeneratedType _type) {
        if (_type == null) {
            throw new IllegalArgumentException("Generated type reference cannot be NULL!")
        }
        this.type = _type;
        this.importMap = GeneratorUtil.createImports(type)
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
        if(comment == null) return '';
        var txt = comment
        if (txt.contains("*/")) {
            txt = txt.replace("*/", "&#42;&#47;")
        }
        val paragraphs = paragraphSplitter.split(txt)

        return '''
            /**
              «FOR p : paragraphs SEPARATOR "<p>"»
                  «p»
              «ENDFOR»
            **/
        '''
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
                throw new IllegalArgumentException(String.format("Invalid length: {}, expected: {}.", «paramName», lengthConstraints));
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
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("«type.name» [«properties.get(0).fieldName»=");
                «IF properties.get(0).returnType.name.contains("[")»
                    builder.append(«Arrays.importedName».toString(«properties.get(0).fieldName»));
                «ELSE»
                    builder.append(«properties.get(0).fieldName»);
                «ENDIF»
                «FOR i : 1..<properties.size»
                    builder.append(", «properties.get(i).fieldName»=");
                    «IF properties.get(i).returnType.name.contains("[")»
                        builder.append(«Arrays.importedName».toString(«properties.get(i).fieldName»));
                    «ELSE»
                        builder.append(«properties.get(i).fieldName»);
                    «ENDIF»
                «ENDFOR»
                builder.append("]");
                return builder.toString();
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
