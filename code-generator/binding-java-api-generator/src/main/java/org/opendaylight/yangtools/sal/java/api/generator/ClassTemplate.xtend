/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import com.google.common.collect.Range
import com.google.common.io.BaseEncoding
import java.beans.ConstructorProperties
import java.math.BigDecimal
import java.math.BigInteger
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.List
import java.util.regex.Pattern
import org.opendaylight.yangtools.binding.generator.util.TypeConstants
import org.opendaylight.yangtools.sal.binding.model.api.Constant
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition
import com.google.common.base.Preconditions
import org.opendaylight.yangtools.sal.binding.model.api.Type
import org.opendaylight.yangtools.sal.binding.model.api.ConcreteType

/**
 * Template for generating JAVA class.
 */
class ClassTemplate extends BaseTemplate {

    protected val List<GeneratedProperty> properties
    protected val List<GeneratedProperty> finalProperties
    protected val List<GeneratedProperty> parentProperties
    protected val Iterable<GeneratedProperty> allProperties;
    protected val Restrictions restrictions

    /**
     * List of enumeration which are generated as JAVA enum type.
     */
    protected val List<Enumeration> enums

    /**
     * List of constant instances which are generated as JAVA public static final attributes.
     */
    protected val List<Constant> consts

    /**
     * List of generated types which are enclosed inside <code>genType</code>
     */
    protected val List<GeneratedType> enclosedGeneratedTypes;

    protected val GeneratedTransferObject genTO;

    /**
     * Creates instance of this class with concrete <code>genType</code>.
     *
     * @param genType generated transfer object which will be transformed to JAVA class source code
     */
    new(GeneratedTransferObject genType) {
        super(genType)
        this.genTO = genType
        this.properties = genType.properties
        this.finalProperties = GeneratorUtil.resolveReadOnlyPropertiesFromTO(genTO.properties)
        this.parentProperties = GeneratorUtil.getPropertiesOfAllParents(genTO)
        this.restrictions = genType.restrictions

        var List<GeneratedProperty> sorted = new ArrayList<GeneratedProperty>();
        sorted.addAll(properties);
        sorted.addAll(parentProperties);
        Collections.sort(sorted, [p1, p2|
            p1.name.compareTo(p2.name)
        ]);

        this.allProperties = sorted
        this.enums = genType.enumerations
        this.consts = genType.constantDefinitions
        this.enclosedGeneratedTypes = genType.enclosedTypes
    }

    /**
     * Generates JAVA class source code (class body only).
     *
     * @return string with JAVA class body source code
     */
    def CharSequence generateAsInnerClass() {
        return generateBody(true)
    }

    override protected body() {
        generateBody(false);
    }

    /**
     * Template method which generates class body.
     *
     * @param isInnerClass boolean value which specify if generated class is|isn't inner
     * @return string with class source code in JAVA format
     */
    def protected generateBody(boolean isInnerClass) '''
        «wrapToDocumentation(formatDataForJavaDoc(type))»
        «generateClassDeclaration(isInnerClass)» {
            «suidDeclaration»
            «innerClassesDeclarations»
            «enumDeclarations»
            «constantsDeclarations»
            «generateFields»

            «IF restrictions != null && (!restrictions.rangeConstraints.nullOrEmpty ||
                !restrictions.lengthConstraints.nullOrEmpty)»
            «generateConstraints»

            «ENDIF»
            «constructors»

            «defaultInstance»

            «FOR field : properties SEPARATOR "\n"»
                «field.getterMethod»
                «IF !field.readOnly»
                    «field.setterMethod»
                «ENDIF»
            «ENDFOR»

            «IF (genTO.isTypedef() && genTO.getBaseType instanceof BitsTypeDefinition)»
                «generateGetValueForBitsTypeDef»
            «ENDIF»

            «generateHashCode»

            «generateEquals»

            «generateToString(genTO.toStringIdentifiers)»

            «generateLengthMethod("length", "_length")»

            «generateRangeMethod("range", "_range")»

        }

    '''

    /**
     * Template method which generates the method <code>getValue()</code> for typedef,
     * which base type is BitsDefinition.
     *
     * @return string with the <code>getValue()</code> method definition in JAVA format
     */
    def protected generateGetValueForBitsTypeDef() '''

        public boolean[] getValue() {
            return new boolean[]{
            «FOR property: genTO.properties SEPARATOR ','»
                 «property.fieldName»
            «ENDFOR»
            };
        }
    '''

    def private generateLengthMethod(String methodName, String varName) '''
        «IF restrictions != null && !(restrictions.lengthConstraints.empty)»
            «val numberClass = restrictions.lengthConstraints.iterator.next.min.class»
            /**
             * @deprecated This method is slated for removal in a future release. See BUG-1485 for details.
             */
            @Deprecated
            public static «List.importedName»<«Range.importedName»<«numberClass.importedNumber»>> «methodName»() {
                return «varName»;
            }
        «ENDIF»
    '''

    def private generateRangeMethod(String methodName, String varName) '''
        «IF restrictions != null && !(restrictions.rangeConstraints.empty)»
            «val returnType = allProperties.iterator.next.returnType»
            /**
             * @deprecated This method is slated for removal in a future release. See BUG-1485 for details.
             */
            @Deprecated
            public static «List.importedName»<«Range.importedName»<«returnType.importedNumber»>> «methodName»() {
                return «varName»;
            }
        «ENDIF»
    '''

    /**
     * Template method which generates inner classes inside this interface.
     *
     * @return string with the source code for inner classes in JAVA format
     */
    def protected innerClassesDeclarations() '''
        «IF !enclosedGeneratedTypes.empty»
            «FOR innerClass : enclosedGeneratedTypes SEPARATOR "\n"»
                «IF (innerClass instanceof GeneratedTransferObject)»
                    «val classTemplate = new ClassTemplate(innerClass)»
                    «classTemplate.generateAsInnerClass»

                «ENDIF»
            «ENDFOR»
        «ENDIF»
    '''

    def protected constructors() '''
        «IF genTO.unionType»
            «genUnionConstructor»
        «ELSE»
            «allValuesConstructor»
        «ENDIF»
        «IF !allProperties.empty»
            «copyConstructor»
        «ENDIF»
        «IF properties.empty && !parentProperties.empty »
            «parentConstructor»
        «ENDIF»
    '''

    def private generateConstraints() '''
        static {
            «IF !restrictions.rangeConstraints.nullOrEmpty»
            «generateRangeConstraints»
            «ENDIF»
            «IF !restrictions.lengthConstraints.nullOrEmpty»
            «generateLengthConstraints»
            «ENDIF»
        }
    '''

    private def generateRangeConstraints() '''
        «IF !allProperties.nullOrEmpty»
            «val returnType = allProperties.iterator.next.returnType»
            «IF returnType.fullyQualifiedName.equals(BigDecimal.canonicalName)»
                «rangeBody(restrictions, BigDecimal, genTO.importedName, "_range")»
            «ELSE»
                «rangeBody(restrictions, BigInteger, genTO.importedName, "_range")»
            «ENDIF»
        «ENDIF»
    '''

    private def rangeBody(Restrictions restrictions, Class<? extends Number> numberClass, String className, String varName) '''
        «ImmutableList.importedName».Builder<«Range.importedName»<«numberClass.importedName»>> builder = «ImmutableList.importedName».builder();
        «FOR r : restrictions.rangeConstraints»
            builder.add(«Range.importedName».closed(«numericValue(numberClass, r.min)», «numericValue(numberClass, r.max)»));
        «ENDFOR»
        «varName» = builder.build();
    '''

    private def lengthBody(Restrictions restrictions, Class<? extends Number> numberClass, String className, String varName) '''
        «ImmutableList.importedName».Builder<«Range.importedName»<«numberClass.importedName»>> builder = «ImmutableList.importedName».builder();
        «FOR r : restrictions.lengthConstraints»
            builder.add(«Range.importedName».closed(«numericValue(numberClass, r.min)», «numericValue(numberClass, r.max)»));
        «ENDFOR»
        «varName» = builder.build();
    '''

    private def generateLengthConstraints() '''
        «IF restrictions != null && !(restrictions.lengthConstraints.empty)»
            «val numberClass = restrictions.lengthConstraints.iterator.next.min.class»
            «IF numberClass.equals(typeof(BigDecimal))»
                «lengthBody(restrictions, numberClass, genTO.importedName, "_length")»
            «ELSE»
                «lengthBody(restrictions, typeof(BigInteger), genTO.importedName, "_length")»
            «ENDIF»
        «ENDIF»
    '''

    def protected allValuesConstructor() '''
    «IF genTO.typedef && !allProperties.empty && allProperties.size == 1 && allProperties.get(0).name.equals("value")»
        @«ConstructorProperties.importedName»("value")
    «ENDIF»
    public «type.name»(«allProperties.asArgumentsDeclaration») {
        «IF false == parentProperties.empty»
            super(«parentProperties.asArguments»);
        «ENDIF»
        «FOR p : allProperties»
            «generateRestrictions(type, p.fieldName.toString, p.returnType)»
        «ENDFOR»

        «/*
         * If we have patterns, we need to apply them to the value field. This is a sad
         * consequence of how this code is structured.
         */
        IF genTO.typedef && !allProperties.empty && allProperties.size == 1 && allProperties.get(0).name.equals("value")»

        «Preconditions.importedName».checkNotNull(_value, "Supplied value may not be null");

            «FOR c : consts»
                «IF c.name == TypeConstants.PATTERN_CONSTANT_NAME && c.value instanceof List<?>»
            boolean valid = false;
            for (Pattern p : patterns) {
                if (p.matcher(_value).matches()) {
                    valid = true;
                    break;
                }
            }

            «Preconditions.importedName».checkArgument(valid, "Supplied value \"%s\" does not match any of the permitted patterns %s", _value, «TypeConstants.PATTERN_CONSTANT_NAME»);
                «ENDIF»
            «ENDFOR»
        «ENDIF»

        «FOR p : properties»
            «IF p.returnType.importedName.contains("[]")»
            this.«p.fieldName» = «p.fieldName» == null ? null : «p.fieldName».clone();
            «ELSE»
            this.«p.fieldName» = «p.fieldName»;
            «ENDIF»
        «ENDFOR»
    }

    '''

    def protected genUnionConstructor() '''
    «FOR p : allProperties»
        «val List<GeneratedProperty> other = new ArrayList(properties)»
        «IF other.remove(p)»
            «genConstructor(p, other)»
        «ENDIF»
    «ENDFOR»

    '''

    def protected genConstructor(GeneratedProperty property, GeneratedProperty... other) '''
    public «type.name»(«property.returnType.importedName + " " + property.name») {
        «IF false == parentProperties.empty»
            super(«parentProperties.asArguments»);
        «ENDIF»

        «generateRestrictions(type, property.fieldName.toString, property.returnType)»

        this.«property.fieldName» = «property.name»;
        «FOR p : other»
            this.«p.fieldName» = null;
        «ENDFOR»
    }
    '''

    def private generateRestrictions(Type type, String paramName, Type returnType) '''
        «val restrictions = type.getRestrictions»
        «IF restrictions !== null»
            «val boolean isNestedType = !(returnType instanceof ConcreteType)»
            «IF !restrictions.lengthConstraints.empty»
                «generateLengthRestriction(returnType, restrictions, paramName, isNestedType)»
            «ENDIF»
            «IF !restrictions.rangeConstraints.empty»
                «generateRangeRestriction(returnType, paramName, isNestedType)»
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

    def private generateRangeRestriction(Type returnType, String paramName, boolean isNestedType) '''
        if («paramName» != null) {
            «printRangeConstraint(returnType, paramName, isNestedType)»
            boolean isValidRange = false;
            for («Range.importedName»<«returnType.importedNumber»> r : «IF isNestedType»«returnType.importedName».«ENDIF»range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", «paramName», «IF isNestedType»«returnType.importedName».«ENDIF»range()));
            }
        }
    '''

    def protected copyConstructor() '''
    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */
    public «type.name»(«type.name» source) {
        «IF false == parentProperties.empty»
            super(source);
        «ENDIF»
        «FOR p : properties»
            this.«p.fieldName» = source.«p.fieldName»;
        «ENDFOR»
    }
    '''

    def protected parentConstructor() '''
    /**
     * Creates a new instance from «genTO.superType.importedName»
     *
     * @param source Source object
     */
    public «type.name»(«genTO.superType.importedName» source) {
            super(source);
    }
    '''

    def protected defaultInstance() '''
        «IF genTO.typedef && !allProperties.empty && !genTO.unionType»
            «val prop = allProperties.get(0)»
            «IF !("org.opendaylight.yangtools.yang.binding.InstanceIdentifier".equals(prop.returnType.fullyQualifiedName))»
            public static «genTO.name» getDefaultInstance(String defaultValue) {
                «IF "byte[]".equals(prop.returnType.name)»
                    «BaseEncoding.importedName» baseEncoding = «BaseEncoding.importedName».base64();
                    return new «genTO.name»(baseEncoding.decode(defaultValue));
                «ELSEIF "java.lang.String".equals(prop.returnType.fullyQualifiedName)»
                    return new «genTO.name»(defaultValue);
                «ELSEIF allProperties.size > 1»
                    «bitsArgs»
                «ELSEIF "java.lang.Boolean".equals(prop.returnType.fullyQualifiedName)»
                    return new «genTO.name»(Boolean.valueOf(defaultValue));
                «ELSEIF "java.lang.Byte".equals(prop.returnType.fullyQualifiedName)»
                    return new «genTO.name»(Byte.valueOf(defaultValue));
                «ELSEIF "java.lang.Short".equals(prop.returnType.fullyQualifiedName)»
                    return new «genTO.name»(Short.valueOf(defaultValue));
                «ELSEIF "java.lang.Integer".equals(prop.returnType.fullyQualifiedName)»
                    return new «genTO.name»(Integer.valueOf(defaultValue));
                «ELSEIF "java.lang.Long".equals(prop.returnType.fullyQualifiedName)»
                    return new «genTO.name»(Long.valueOf(defaultValue));
                «ELSE»
                    return new «genTO.name»(new «prop.returnType.importedName»(defaultValue));
                «ENDIF»
            }
            «ENDIF»
        «ENDIF»
    '''

    def protected bitsArgs() '''
        «List.importedName»<«String.importedName»> properties = «Lists.importedName».newArrayList(«allProperties.propsAsArgs»);
        if (!properties.contains(defaultValue)) {
            throw new «IllegalArgumentException.importedName»("invalid default parameter");
        }
        int i = 0;
        return new «genTO.name»(
        «FOR prop : allProperties SEPARATOR ","»
            properties.get(i++).equals(defaultValue) ? «Boolean.importedName».TRUE : null
        «ENDFOR»
        );
    '''

    def protected propsAsArgs(Iterable<GeneratedProperty> properties) '''
        «FOR prop : properties SEPARATOR ","»
            "«prop.name»"
        «ENDFOR»
    '''

    /**
     * Template method which generates JAVA class declaration.
     *
     * @param isInnerClass boolean value which specify if generated class is|isn't inner
     * @return string with class declaration in JAVA format
     */
    def protected generateClassDeclaration(boolean isInnerClass) '''
        public«
        IF (isInnerClass)»«
            " static final "»«
        ELSEIF (type.abstract)»«
            " abstract "»«
        ELSE»«
            " "»«
        ENDIF»class «type.name»«
        IF (genTO.superType != null)»«
            " extends "»«genTO.superType.importedName»«
        ENDIF»
        «IF (!type.implements.empty)»«
            " implements "»«
            FOR type : type.implements SEPARATOR ", "»«
                type.importedName»«
            ENDFOR»«
        ENDIF
    »'''

    /**
     * Template method which generates JAVA enum type.
     *
     * @return string with inner enum source code in JAVA format
     */
    def protected enumDeclarations() '''
        «IF !enums.empty»
            «FOR e : enums SEPARATOR "\n"»
                «val enumTemplate = new EnumTemplate(e)»
                «enumTemplate.generateAsInnerClass»
            «ENDFOR»
        «ENDIF»
    '''

    def protected suidDeclaration() '''
        «IF genTO.SUID != null»
            private static final long serialVersionUID = «genTO.SUID.value»L;
        «ENDIF»
    '''

    /**
     * Template method which generates JAVA constants.
     *
     * @return string with constants in JAVA format
     */
    def protected constantsDeclarations() '''
        «IF !consts.empty»
            «FOR c : consts»
                «IF c.name == TypeConstants.PATTERN_CONSTANT_NAME»
                    «val cValue = c.value»
                    «IF cValue instanceof List<?>»
                        private static final «Pattern.importedName»[] «Constants.MEMBER_PATTERN_LIST»;
                        public static final «List.importedName»<String> «TypeConstants.PATTERN_CONSTANT_NAME» = «ImmutableList.importedName».of(«
                        FOR v : cValue SEPARATOR ", "»«
                            IF v instanceof String»"«
                                v»"«
                            ENDIF»«
                        ENDFOR»);

                        «generateStaticInicializationBlock»
                    «ENDIF»
                «ELSE»
                    public static final «c.type.importedName» «c.name» = «c.value»;
                «ENDIF»
            «ENDFOR»
        «ENDIF»
    '''

    /**
     * Template method which generates JAVA static initialization block.
     *
     * @return string with static initialization block in JAVA format
     */
    def protected generateStaticInicializationBlock() '''
        static {
            final «Pattern.importedName» a[] = new «Pattern.importedName»[«TypeConstants.PATTERN_CONSTANT_NAME».size()];
            int i = 0;
            for (String regEx : «TypeConstants.PATTERN_CONSTANT_NAME») {
                a[i++] = Pattern.compile(regEx);
            }

            «Constants.MEMBER_PATTERN_LIST» = a;
        }
    '''

    /**
     * Template method which generates JAVA class attributes.
     *
     * @return string with the class attributes in JAVA format
     */
    def protected generateFields() '''
        «IF restrictions != null»
            «val prop = getPropByName("value")»
            «IF prop != null»
                «IF !(restrictions.lengthConstraints.empty)»
                    private static final «List.importedName»<«Range.importedName»<«prop.returnType.importedNumber»>> _length;
                «ENDIF»
                «IF !(restrictions.rangeConstraints.empty)»
                    private static final «List.importedName»<«Range.importedName»<«prop.returnType.importedNumber»>> _range;
                «ENDIF»
            «ENDIF»
        «ENDIF»
        «IF !properties.empty»
            «FOR f : properties»
                private«IF f.readOnly» final«ENDIF» «f.returnType.importedName» «f.fieldName»;
            «ENDFOR»
        «ENDIF»
    '''

    /**
     * Template method which generates the method <code>hashCode()</code>.
     *
     * @return string with the <code>hashCode()</code> method definition in JAVA format
     */
    def protected generateHashCode() '''
        «IF !genTO.hashCodeIdentifiers.empty»
            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                «FOR property : genTO.hashCodeIdentifiers»
                    «IF property.returnType.name.contains("[")»
                    result = prime * result + ((«property.fieldName» == null) ? 0 : «Arrays.importedName».hashCode(«property.fieldName»));
                    «ELSE»
                    result = prime * result + ((«property.fieldName» == null) ? 0 : «property.fieldName».hashCode());
                    «ENDIF»
                «ENDFOR»
                return result;
            }
        «ENDIF»
    '''

    /**
     * Template method which generates the method <code>equals()</code>.
     *
     * @return string with the <code>equals()</code> method definition in JAVA format
     */
    def protected generateEquals() '''
        «IF !genTO.equalsIdentifiers.empty»
            @Override
            public boolean equals(java.lang.Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                «type.name» other = («type.name») obj;
                «FOR property : genTO.equalsIdentifiers»
                    «val fieldName = property.fieldName»
                    if («fieldName» == null) {
                        if (other.«fieldName» != null) {
                            return false;
                        }
                    «IF property.returnType.name.contains("[")»
                    } else if(!«Arrays.importedName».equals(«fieldName», other.«fieldName»)) {
                    «ELSE»
                    } else if(!«fieldName».equals(other.«fieldName»)) {
                    «ENDIF»
                        return false;
                    }
                «ENDFOR»
                return true;
            }
        «ENDIF»
    '''

    def GeneratedProperty getPropByName(String name) {
        for (GeneratedProperty prop : allProperties) {
            if (prop.name.equals(name)) {
                return prop;
            }
        }
        return null;
    }

}
