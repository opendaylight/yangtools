/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static java.util.Objects.requireNonNull
import static org.opendaylight.mdsal.binding.model.util.BaseYangTypes.BINARY_TYPE
import static org.opendaylight.mdsal.binding.model.util.BaseYangTypes.BOOLEAN_TYPE
import static org.opendaylight.mdsal.binding.model.util.BaseYangTypes.EMPTY_TYPE
import static org.opendaylight.mdsal.binding.model.util.BaseYangTypes.INSTANCE_IDENTIFIER
import static org.opendaylight.mdsal.binding.model.util.BaseYangTypes.INT16_TYPE
import static org.opendaylight.mdsal.binding.model.util.BaseYangTypes.INT32_TYPE
import static org.opendaylight.mdsal.binding.model.util.BaseYangTypes.INT64_TYPE
import static org.opendaylight.mdsal.binding.model.util.BaseYangTypes.INT8_TYPE
import static org.opendaylight.mdsal.binding.model.util.BaseYangTypes.STRING_TYPE
import static org.opendaylight.mdsal.binding.model.util.BaseYangTypes.UINT16_TYPE
import static org.opendaylight.mdsal.binding.model.util.BaseYangTypes.UINT32_TYPE
import static org.opendaylight.mdsal.binding.model.util.BaseYangTypes.UINT64_TYPE
import static org.opendaylight.mdsal.binding.model.util.BaseYangTypes.UINT8_TYPE
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.SCALAR_TYPE_OBJECT
import static org.opendaylight.mdsal.binding.model.util.Types.STRING;
import static extension org.apache.commons.text.StringEscapeUtils.escapeJava

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import java.beans.ConstructorProperties
import java.util.ArrayList
import java.util.Base64;
import java.util.Comparator
import java.util.List
import java.util.Map
import java.util.Set
import javax.management.ConstructorParameters
import org.gaul.modernizer_maven_annotations.SuppressModernizer
import org.opendaylight.mdsal.binding.model.api.ConcreteType
import org.opendaylight.mdsal.binding.model.api.Constant
import org.opendaylight.mdsal.binding.model.api.Enumeration
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject
import org.opendaylight.mdsal.binding.model.api.Restrictions
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.mdsal.binding.model.util.TypeConstants
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping
import org.opendaylight.yangtools.yang.common.Empty
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition

/**
 * Template for generating JAVA class.
 */
@SuppressModernizer
class ClassTemplate extends BaseTemplate {
    static val Comparator<GeneratedProperty> PROP_COMPARATOR = Comparator.comparing([prop | prop.name])
    static val VALUEOF_TYPES = Set.of(
        BOOLEAN_TYPE,
        INT8_TYPE,
        INT16_TYPE,
        INT32_TYPE,
        INT64_TYPE,
        UINT8_TYPE,
        UINT16_TYPE,
        UINT32_TYPE,
        UINT64_TYPE)

    protected val List<GeneratedProperty> properties
    protected val List<GeneratedProperty> finalProperties
    protected val List<GeneratedProperty> parentProperties
    protected val List<GeneratedProperty> allProperties
    protected val Restrictions restrictions

    /**
     * List of enumeration which are generated as JAVA enum type.
     */
    protected val List<Enumeration> enums

    /**
     * List of constant instances which are generated as JAVA public static final attributes.
     */
    protected val List<Constant> consts

    protected val GeneratedTransferObject genTO

    val AbstractRangeGenerator<?> rangeGenerator

    /**
     * Creates instance of this class with concrete <code>genType</code>.
     *
     * @param genType generated transfer object which will be transformed to JAVA class source code
     */
    new(GeneratedTransferObject genType) {
        this(new TopLevelJavaGeneratedType(genType), genType)
    }

    /**
     * Creates instance of this class with concrete <code>genType</code>.
     *
     * @param genType generated transfer object which will be transformed to JAVA class source code
     */
    new(AbstractJavaGeneratedType javaType, GeneratedTransferObject genType) {
        super(javaType, genType)
        this.genTO = genType
        this.properties = genType.properties
        this.finalProperties = GeneratorUtil.resolveReadOnlyPropertiesFromTO(genTO.properties)
        this.parentProperties = GeneratorUtil.getPropertiesOfAllParents(genTO)
        this.restrictions = genType.restrictions

        val sorted = new ArrayList();
        sorted.addAll(properties);
        sorted.addAll(parentProperties);
        sorted.sort(PROP_COMPARATOR);

        this.allProperties = sorted
        this.enums = genType.enumerations
        this.consts = genType.constantDefinitions

        if (restrictions !== null && restrictions.rangeConstraint.present) {
            rangeGenerator = requireNonNull(AbstractRangeGenerator.forType(TypeUtils.encapsulatedValueType(genType)))
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

            «IF restrictions !== null»
                «IF restrictions.lengthConstraint.present»
                    «LengthGenerator.generateLengthChecker("_value", TypeUtils.encapsulatedValueType(genTO),
                        restrictions.lengthConstraint.get, this)»
                «ENDIF»
                «IF restrictions.rangeConstraint.present»
                    «rangeGenerator.generateRangeChecker("_value", restrictions.rangeConstraint.get, this)»
                «ENDIF»
            «ENDIF»

            «constructors»

            «defaultInstance»

            «propertyMethods»

            «IF (genTO.isTypedef() && genTO.getBaseType instanceof BitsTypeDefinition)»
                «generateGetValueForBitsTypeDef»
            «ENDIF»

            «generateHashCode»

            «generateEquals»

            «generateToString(genTO.toStringIdentifiers)»
        }

    '''

    def private propertyMethods() {
        if (properties.empty) {
            return ""
        }
        isScalarTypeObject ? scalarTypeObjectValue(properties.get(0)) : defaultProperties
    }

    def private isScalarTypeObject() {
        for (impl : genTO.implements) {
            if (SCALAR_TYPE_OBJECT.identifier.equals(impl.identifier)) {
                return true
            }
        }
        return false
    }

    def private defaultProperties() '''
        «FOR field : properties SEPARATOR "\n"»
            «field.getterMethod»
            «IF !field.readOnly»
                «field.setterMethod»
            «ENDIF»
        «ENDFOR»
    '''

    def private scalarTypeObjectValue(GeneratedProperty field) '''
        @«OVERRIDE.importedName»
        public «field.returnType.importedName» «BindingMapping.SCALAR_TYPE_OBJECT_GET_VALUE_NAME»() {
            return «field.fieldName»«IF field.returnType.name.endsWith("[]")».clone()«ENDIF»;
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
        «IF !type.enclosedTypes.empty»
            «FOR innerClass : type.enclosedTypes SEPARATOR "\n"»
                «generateInnerClass(innerClass)»
            «ENDFOR»
        «ENDIF»
    '''

    def protected constructors() '''
        «IF genTO.unionType»
            «genUnionConstructor»
        «ELSEIF genTO.typedef && allProperties.size == 1 && allProperties.get(0).name.equals(TypeConstants.VALUE_PROP)»
            «typedefConstructor»
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

    def private allValuesConstructor() '''
    public «type.name»(«allProperties.asArgumentsDeclaration») {
        «IF !parentProperties.empty»
            super(«parentProperties.asArguments»);
        «ENDIF»
        «FOR p : allProperties»
            «generateRestrictions(type, p.fieldName, p.returnType)»
        «ENDFOR»

        «FOR p : properties»
            «val fieldName = p.fieldName»
            «IF p.returnType.name.endsWith("[]")»
                this.«fieldName» = «fieldName» == null ? null : «fieldName».clone();
            «ELSE»
                this.«fieldName» = «fieldName»;
            «ENDIF»
        «ENDFOR»
    }
    '''

    def private typedefConstructor() '''
    @«ConstructorParameters.importedName»("«TypeConstants.VALUE_PROP»")
    @«ConstructorProperties.importedName»("«TypeConstants.VALUE_PROP»")
    public «type.name»(«allProperties.asArgumentsDeclaration») {
        «IF !parentProperties.empty»
            super(«parentProperties.asArguments»);
        «ENDIF»
        «FOR p : allProperties»
            «generateRestrictions(type, p.fieldName, p.returnType)»
        «ENDFOR»
        «/*
         * If we have patterns, we need to apply them to the value field. This is a sad consequence of how this code is
         * structured.
         */»
        «CODEHELPERS.importedName».requireValue(_value);
        «genPatternEnforcer("_value")»

        «FOR p : properties»
            «val fieldName = p.fieldName»
            «IF p.returnType.name.endsWith("[]")»
                this.«fieldName» = «fieldName».clone();
            «ELSE»
                this.«fieldName» = «fieldName»;
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

    def protected genConstructor(GeneratedProperty property, Iterable<GeneratedProperty> other) '''
    public «type.name»(«property.returnType.importedName + " " + property.name») {
        «IF !parentProperties.empty»
            super(«parentProperties.asArguments»);
        «ENDIF»

        «val fieldName = property.fieldName»
        «generateRestrictions(type, fieldName, property.returnType)»

        this.«fieldName» = «property.name»;
        «FOR p : other»
            this.«p.fieldName» = null;
        «ENDFOR»
    }
    '''

    def private genPatternEnforcer(String ref) '''
        «FOR c : consts»
            «IF c.name == TypeConstants.PATTERN_CONSTANT_NAME»
            «CODEHELPERS.importedName».checkPattern(«ref», «Constants.MEMBER_PATTERN_LIST», «Constants.MEMBER_REGEX_LIST»);
            «ENDIF»
        «ENDFOR»
    '''

    def private static paramValue(Type returnType, String paramName) {
        if (returnType instanceof ConcreteType) {
            return paramName
        } else {
            return paramName + ".getValue()"
        }
    }

    def private generateRestrictions(Type type, String paramName, Type returnType) '''
        «val restrictions = type.restrictions»
        «IF restrictions !== null»
            «IF restrictions.lengthConstraint.present || restrictions.rangeConstraint.present»
            if («paramName» != null) {
                «IF restrictions.lengthConstraint.present»
                    «LengthGenerator.generateLengthCheckerCall(paramName, paramValue(returnType, paramName))»
                «ENDIF»
                «IF restrictions.rangeConstraint.present»
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
        «IF !parentProperties.empty»
            super(source);
        «ENDIF»
        «FOR p : properties»
            «val fieldName = p.fieldName»
            this.«fieldName» = source.«fieldName»;
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
        «genPatternEnforcer("getValue()")»
    }
    '''

    def protected defaultInstance() '''
        «IF genTO.typedef && !allProperties.empty && !genTO.unionType»
            «val prop = allProperties.get(0)»
            «val propType = prop.returnType»
            «IF !(INSTANCE_IDENTIFIER.identifier.equals(propType.identifier))»
            public static «genTO.name» getDefaultInstance(final String defaultValue) {
                «IF allProperties.size > 1»
                    «bitsArgs»
                «ELSEIF VALUEOF_TYPES.contains(propType)»
                    return new «genTO.name»(«propType.importedName».valueOf(defaultValue));
                «ELSEIF STRING_TYPE.equals(propType)»
                    return new «genTO.name»(defaultValue);
                «ELSEIF BINARY_TYPE.equals(propType)»
                    return new «genTO.name»(«Base64.importedName».getDecoder().decode(defaultValue));
                «ELSEIF EMPTY_TYPE.equals(propType)»
                    «Preconditions.importedName».checkArgument(defaultValue.isEmpty(), "Invalid value %s", defaultValue);
                    return new «genTO.name»(«Empty.importedName».getInstance());
                «ELSE»
                    return new «genTO.name»(new «propType.importedName»(defaultValue));
                «ENDIF»
            }
            «ENDIF»
        «ENDIF»
    '''

    @SuppressFBWarnings(value = "DLS_DEAD_LOCAL_STORE", justification = "FOR with SEPARATOR, not needing for value")
    def protected bitsArgs() '''
        «JU_LIST.importedName»<«STRING.importedName»> properties = «Lists.importedName».newArrayList(«allProperties.propsAsArgs»);
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
        IF (genTO.superType !== null)»«
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
                «new EnumTemplate(javaType.getEnclosedType(e.identifier), e).generateAsInnerClass»
            «ENDFOR»
        «ENDIF»
    '''

    def protected suidDeclaration() '''
        «IF genTO.SUID !== null»
            private static final long serialVersionUID = «genTO.SUID.value»L;
        «ENDIF»
    '''

    def protected annotationDeclaration() '''
        «IF genTO.getAnnotations !== null»
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
                    «val cValue = c.value as Map<String, String>»
                    «val jurPatternRef = JUR_PATTERN.importedName»
                    public static final «JU_LIST.importedName»<String> «TypeConstants.PATTERN_CONSTANT_NAME» = «ImmutableList.importedName».of(«
                    FOR v : cValue.keySet SEPARATOR ", "»"«v.escapeJava»"«ENDFOR»);
                    «IF cValue.size == 1»
                        private static final «jurPatternRef» «Constants.MEMBER_PATTERN_LIST» = «jurPatternRef».compile(«TypeConstants.PATTERN_CONSTANT_NAME».get(0));
                        private static final String «Constants.MEMBER_REGEX_LIST» = "«cValue.values.iterator.next.escapeJava»";
                    «ELSE»
                        private static final «jurPatternRef»[] «Constants.MEMBER_PATTERN_LIST» = «CODEHELPERS.importedName».compilePatterns(«TypeConstants.PATTERN_CONSTANT_NAME»);
                        private static final String[] «Constants.MEMBER_REGEX_LIST» = { «
                        FOR v : cValue.values SEPARATOR ", "»"«v.escapeJava»"«ENDFOR» };
                    «ENDIF»
                «ELSE»
                    «emitConstant(c)»
                «ENDIF»
            «ENDFOR»
        «ENDIF»
    '''

    /**
     * Template method which generates JAVA class attributes.
     *
     * @return string with the class attributes in JAVA format
     */
    def protected generateFields() '''
        «IF !properties.empty»
            «FOR f : properties»
                private«IF isReadOnly(f)» final«ENDIF» «f.returnType.importedName» «f.fieldName»;
            «ENDFOR»
        «ENDIF»
    '''

    protected def isReadOnly(GeneratedProperty field) {
        return field.readOnly
    }

    /**
     * Template method which generates the method <code>hashCode()</code>.
     *
     * @return string with the <code>hashCode()</code> method definition in JAVA format
     */
    def protected generateHashCode() {
        val size = genTO.hashCodeIdentifiers.size
        if (size == 0) {
            return ""
        }
        return '''
            @«OVERRIDE.importedName»
            public int hashCode() {
                «IF size != 1»
                    «hashCodeResult(genTO.hashCodeIdentifiers)»
                    return result;
                «ELSE»
                    return «CODEHELPERS.importedName».wrapperHashCode(«genTO.hashCodeIdentifiers.get(0).fieldName»);
                «ENDIF»
            }
        '''
    }

    /**
     * Template method which generates the method <code>equals()</code>.
     *
     * @return string with the <code>equals()</code> method definition in JAVA format
     */
    def private generateEquals() '''
        «IF !genTO.equalsIdentifiers.empty»
            @«OVERRIDE.importedName»
            public final boolean equals(java.lang.Object obj) {
                if (this == obj) {
                    return true;
                }
                if (!(obj instanceof «type.name»)) {
                    return false;
                }
                final «type.name» other = («type.name») obj;
                «FOR property : genTO.equalsIdentifiers»
                    «val fieldName = property.fieldName»
                    if (!«property.importedUtilClass».equals(«fieldName», other.«fieldName»)) {
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
        return null
    }
}
