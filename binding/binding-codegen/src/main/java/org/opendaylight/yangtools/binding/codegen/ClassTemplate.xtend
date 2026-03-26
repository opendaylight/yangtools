/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen

import static org.opendaylight.yangtools.binding.model.ri.BindingTypes.BITS_TYPE_OBJECT
import static org.opendaylight.yangtools.binding.model.ri.Types.STRING;

import java.util.Collection
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject
import org.opendaylight.yangtools.binding.model.ri.TypeConstants
import org.opendaylight.yangtools.binding.model.ri.Types
import org.opendaylight.yangtools.binding.contract.Naming
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition

/**
 * Template for generating JAVA class.
 */
// FIXME: YANGTOOLS-1806: convert to Java
class ClassTemplate extends AbstractClassTemplate {
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
    }

    override package generateBody(boolean isInnerClass) '''
        «type.formatDataForJavaDoc.wrapToDocumentation»
        «annotationDeclaration»
        «IF !isInnerClass»
            «generatedAnnotation»
        «ENDIF»
        «generateClassDeclaration(isInnerClass)» {
            «suidDeclaration»
            «generateInnerClasses(type.enclosedTypes)»
            «generateInnerEnumTypeObjects(enums)»
            «constantsDeclarations»
            «generateFields»

            «IF restrictions !== null»
                «IF restrictions.lengthConstraint.present»
                    «LengthGenerator.generateLengthChecker("_value", TypeUtils.encapsulatedValueType(genTO),
                        restrictions.lengthConstraint.orElseThrow, this)»
                «ENDIF»
                «IF restrictions.rangeConstraint.present»
                    «rangeGenerator.generateRangeChecker("_value", restrictions.rangeConstraint.orElseThrow, this)»
                «ENDIF»
            «ENDIF»

            «constructors»

            «defaultInstance»

            «propertyMethods»

            «IF isBitsTypeObject»
                «validNamesAndValues»
            «ENDIF»

            «generateHashCode»

            «generateEquals»

            «generateToString(genTO.toStringIdentifiers)»
        }

    '''

    def private isBitsTypeObject() {
        var wlk = genTO
        while (wlk !== null) {
            for (impl : wlk.implements) {
                if (BITS_TYPE_OBJECT.name.equals(impl.name)) {
                    return true
                }
            }
            wlk = wlk.superType
        }
        return false
    }

    def private validNamesAndValues() {
        for (c : consts) {
            if (TypeConstants.VALID_NAMES_NAME.equals(c.name)) {
                return validNamesAndValues(c.value as BitsTypeDefinition)
            }
        }
        return ""
    }

    def private validNamesAndValues(BitsTypeDefinition typedef) '''

        @«OVERRIDE.importedName»
        public «IMMUTABLE_SET.importedName»<«STRING.importedName»> validNames() {
            return «TypeConstants.VALID_NAMES_NAME»;
        }

        @«OVERRIDE.importedName»
        public boolean[] values() {
            return new boolean[] {
                    «FOR bit : typedef.bits SEPARATOR ','»
                        «Naming.getPropertyName(bit.name).getterMethodName»()
                    «ENDFOR»
                };
        }
    '''

    /**
     * Template method which generates the method <code>equals()</code>.
     *
     * @return string with the <code>equals()</code> method definition in JAVA format
     */
    def private generateEquals() '''
        «IF !genTO.equalsIdentifiers.empty»
            @«OVERRIDE.importedName»
            public final boolean equals(«OBJECT.importedName» obj) {
                return this == obj || obj instanceof «type.simpleName» other
                    «FOR property : genTO.equalsIdentifiers»
                        «val fieldName = property.fieldName»
                        «val type = property.returnType»
                        «IF type.equals(Types.primitiveBooleanType)»
                            && «fieldName» == other.«fieldName»«
                        »«ELSE»
                            && «type.importedUtilClass».equals(«fieldName», other.«fieldName»)«
                        »«ENDIF»«
                    »«ENDFOR»;
            }
        «ENDIF»
    '''

    def private generateToString(Collection<GeneratedProperty> properties) '''
        «IF !properties.empty»
            @«OVERRIDE.importedName»
            public «STRING.importedName» toString() {
                final var helper = «MOREOBJECTS.importedName».toStringHelper(«type.importedName».class);
                «FOR property : properties»
                    «CODEHELPERS.importedName».«property.valueAppender»(helper, "«property.name»", «property.fieldName»);
                «ENDFOR»
                return helper.toString();
            }
        «ENDIF»
    '''

    def private valueAppender(GeneratedProperty prop) {
        if (prop.returnType.equals(Types.primitiveBooleanType())) {
            return "appendBit"
        }
        return "appendValue"
    }
}
