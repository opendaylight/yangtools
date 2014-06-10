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
import java.util.List
import org.opendaylight.yangtools.sal.binding.model.api.ConcreteType
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import java.util.Collection
import java.util.Arrays
import java.util.HashMap
import com.google.common.collect.ImmutableList
import java.math.BigInteger
import java.math.BigDecimal

abstract class BaseTemplate {
    protected val GeneratedType type;
    protected val Map<String, String> importMap;
    static val paragraphSplitter = Splitter.on("\n\n").omitEmptyStrings();

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
        «val restrictions = type.getRestrictions»
        «IF restrictions !== null»
            «val boolean isNestedType = !(returnType instanceof ConcreteType)»
            «IF !restrictions.lengthConstraints.empty»
                «generateLengthRestriction(returnType, restrictions, paramName, isNestedType)»
            «ENDIF»
            «IF !restrictions.rangeConstraints.empty»
                «generateRangeRestriction(returnType, restrictions, paramName, isNestedType)»
            «ENDIF»
        «ENDIF»
    '''

    def private generateLengthRestriction(Type returnType, Restrictions restrictions, String paramName, boolean isNestedType) '''
        «val clazz = restrictions.lengthConstraints.iterator.next.min.class»
        if («paramName» != null) {
            «printLengthConstraint(returnType, clazz, paramName, isNestedType, returnType.name.contains("["))»
            boolean isValidLength = false;
            for («Range.importedName»<«clazz.importedNumber»> r : «IF isNestedType»«returnType.importedName».«ENDIF»length()) {
                if (r.contains(_constraint)) {
                    isValidLength = true;
                }
            }
            if (!isValidLength) {
                throw new IllegalArgumentException(String.format("Invalid length: %s, expected: %s.", «paramName», «IF isNestedType»«returnType.importedName».«ENDIF»length()));
            }
        }
    '''

    def private generateRangeRestriction(Type returnType, Restrictions restrictions, String paramName, boolean isNestedType) '''
        «val clazz = restrictions.rangeConstraints.iterator.next.min.class»
        if («paramName» != null) {
            «printRangeConstraint(returnType, clazz, paramName, isNestedType)»
            boolean isValidRange = false;
            for («Range.importedName»<«clazz.importedNumber»> r : «IF isNestedType»«returnType.importedName».«ENDIF»range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", «paramName», «IF isNestedType»«returnType.importedName».«ENDIF»range()));
            }
        }
    '''

    /**
     * Print length constraint.
     * This should always be a BigInteger (only string and binary can have length restriction)
     */
    def printLengthConstraint(Type returnType, Class<? extends Number> clazz, String paramName, boolean isNestedType, boolean isArray) '''
        «clazz.importedNumber» _constraint = «clazz.importedNumber».valueOf(«paramName»«IF isNestedType».getValue()«ENDIF».length«IF !isArray»()«ENDIF»);
    '''

    def printRangeConstraint(Type returnType, Class<? extends Number> clazz, String paramName, boolean isNestedType) '''
        «IF clazz.canonicalName.equals(BigDecimal.canonicalName)»
            «clazz.importedNumber» _constraint = new «clazz.importedNumber»(«paramName»«IF isNestedType».getValue()«ENDIF».toString());
        «ELSE»
            «IF isNestedType»
                «val propReturnType = findProperty(returnType as GeneratedTransferObject, "value").returnType»
                «IF propReturnType.fullyQualifiedName.equals(BigInteger.canonicalName)»
                    «clazz.importedNumber» _constraint = «paramName».getValue();
                «ELSE»
                    «clazz.importedNumber» _constraint = «clazz.importedNumber».valueOf(«paramName».getValue());
                «ENDIF»
            «ELSE»
                «IF returnType.fullyQualifiedName.equals(BigInteger.canonicalName)»
                    «clazz.importedNumber» _constraint = «paramName»;
                «ELSE»
                    «clazz.importedNumber» _constraint = «clazz.importedNumber».valueOf(«paramName»);
                «ENDIF»
            «ENDIF»
        «ENDIF»
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
                    «lengthMethodBody(restrictions, numberClass, className, varName)»
                «ELSE»
                    «lengthMethodBody(restrictions, typeof(BigInteger), className, varName)»
                «ENDIF»
            }
        «ENDIF»
    '''

    def private lengthMethodBody(Restrictions restrictions, Class<? extends Number> numberClass, String className, String varName) '''
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

    def protected generateRangeMethod(String methodName, Type type, String className, String varName) '''
        «val Restrictions restrictions = type.restrictions»
        «IF restrictions != null && !(restrictions.rangeConstraints.empty)»
            «val numberClass = restrictions.rangeConstraints.iterator.next.min.class»
            public static «List.importedName»<«Range.importedName»<«numberClass.importedNumber»>> «methodName»() {
                «IF numberClass.equals(typeof(BigDecimal))»
                    «rangeMethodBody(restrictions, numberClass, className, varName)»
                «ELSE»
                    «rangeMethodBody(restrictions, typeof(BigInteger), className, varName)»
                «ENDIF»
            }
        «ENDIF»
    '''

    def private rangeMethodBody(Restrictions restrictions, Class<? extends Number> numberClass, String className, String varName) '''
        if («varName» == null) {
            synchronized («className».class) {
                if («varName» == null) {
                    «ImmutableList.importedName».Builder<«Range.importedName»<«numberClass.importedName»>> builder = «ImmutableList.importedName».builder();
                    «FOR r : restrictions.rangeConstraints»
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

    def private String numericValue(Class<? extends Number> clazz, Object numberValue) {
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
