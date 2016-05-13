/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import com.google.common.io.BaseEncoding
import java.beans.ConstructorProperties
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.List
import java.util.Objects
import java.util.regex.Pattern
import org.opendaylight.yangtools.binding.generator.util.TypeConstants
import org.opendaylight.yangtools.sal.binding.model.api.ConcreteType
import org.opendaylight.yangtools.sal.binding.model.api.Constant
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions
import org.opendaylight.yangtools.sal.binding.model.api.Type
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition

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

    private val AbstractRangeGenerator<?> rangeGenerator

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

        if (restrictions != null && !restrictions.rangeConstraints.nullOrEmpty) {
            rangeGenerator = AbstractRangeGenerator.forType(findProperty(genType, "value").returnType)
            Preconditions.checkNotNull(rangeGenerator)
        } else {
            rangeGenerator = null
        }
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
        «annotationDeclaration»
        «generateClassDeclaration(isInnerClass)» {
            «suidDeclaration»
            «innerClassesDeclarations»
            «enumDeclarations»
            «constantsDeclarations»
            «generateFields»

            «IF restrictions != null»
                «IF !restrictions.lengthConstraints.nullOrEmpty»
                    «LengthGenerator.generateLengthChecker("_value", findProperty(genTO, "value").returnType, restrictions.lengthConstraints)»
                «ENDIF»
                «IF !restrictions.rangeConstraints.nullOrEmpty»
                    «rangeGenerator.generateRangeChecker("_value", restrictions.rangeConstraints)»
                «ENDIF»
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
                for (Pattern p : patterns) {
                    «Preconditions.importedName».checkArgument(p.matcher(_value).matches(), "Supplied value \"%s\" does not match required pattern \"%s\"", _value, p);
                }
                «ENDIF»
            «ENDFOR»
        «ENDIF»

        «FOR p : properties»
            «IF p.returnType.importedName.contains("[]")»
                «IF genTO.typedef && !allProperties.empty && allProperties.size == 1 && allProperties.get(0).name
                .equals("value")»
                this.«p.fieldName» = «p.fieldName».clone();
                «ELSE»
                this.«p.fieldName» = «p.fieldName» == null ? null : «p.fieldName».clone();
                «ENDIF»
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

    def private static paramValue(Type returnType, String paramName) {
        if (returnType instanceof ConcreteType) {
            return paramName
        } else {
            return paramName + ".getValue()"
        }
    }

    def private generateRestrictions(Type type, String paramName, Type returnType) '''
        «val restrictions = type.getRestrictions»
        «IF restrictions !== null»
            «IF !restrictions.lengthConstraints.empty || !restrictions.rangeConstraints.empty»
            if («paramName» != null) {
                «IF !restrictions.lengthConstraints.empty»
                    «LengthGenerator.generateLengthCheckerCall(paramName, paramValue(returnType, paramName))»
                «ENDIF»
                «IF !restrictions.rangeConstraints.empty»
                    «rangeGenerator.generateRangeCheckerCall(paramName, paramValue(returnType, paramName))»
                «ENDIF»
                }
            «ENDIF»
        «ENDIF»
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

    def protected annotationDeclaration() '''
        «IF genTO.getAnnotations != null»
            «FOR e : genTO.getAnnotations»
                @«e.getName»
            «ENDFOR»
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
                    «emitConstant(c)»
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
                    result = prime * result + «Arrays.importedName».hashCode(«property.fieldName»);
                    «ELSE»
                    result = prime * result + «Objects.importedName».hashCode(«property.fieldName»);
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
                    «IF property.returnType.name.contains("[")»
                    if (!«Arrays.importedName».equals(«fieldName», other.«fieldName»)) {
                    «ELSE»
                    if (!«Objects.importedName».equals(«fieldName», other.«fieldName»)) {
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
