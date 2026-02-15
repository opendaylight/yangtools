/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen

import static org.opendaylight.yangtools.binding.model.ri.Types.STRING;
import static org.opendaylight.yangtools.binding.model.ri.Types.objectType;

import java.util.Locale
import org.opendaylight.yangtools.binding.model.api.ConcreteType
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.binding.model.api.GeneratedType
import org.opendaylight.yangtools.binding.model.api.JavaTypeName
import org.opendaylight.yangtools.binding.model.api.Restrictions
import org.opendaylight.yangtools.binding.model.api.Type
import org.opendaylight.yangtools.binding.model.ri.TypeConstants
import org.opendaylight.yangtools.binding.contract.Naming

// FIXME: YANGTOOLS-1618: convert to Java
abstract class BaseTemplate extends AbstractBaseTemplate {
    new(GeneratedType type) {
        super(type)
    }

    new(AbstractJavaGeneratedType javaType, GeneratedType type) {
        super(javaType, type)
    }

    // Helper patterns

    override package final emitNameConstant(String name, Type type, JavaTypeName yangModuleInfo,
            String yangDataName) '''
        /**
         * Yang Data template name of the statement represented by this class.
         */
        public static final «type.importedNonNull» «name» = «yangModuleInfo.importedName».«Naming.MODULE_INFO_YANGDATANAMEOF_METHOD_NAME»("«yangDataName»");
    '''

    override package final emitQNameConstant(String name, Type type, JavaTypeName yangModuleInfo, String localName) '''
        /**
         * YANG identifier of the statement represented by this class.
         */
        public static final «type.importedNonNull» «name» = «yangModuleInfo.importedName».«Naming.MODULE_INFO_QNAMEOF_METHOD_NAME»("«localName»");
    '''

    override package CharSequence emitValueConstant(String name, Type type) {
        val typeName = type.importedName
        val override = OVERRIDE.importedName
        return '''
            /**
             * Singleton value representing the {@link «typeName»} identity.
             */
            public static final «type.importedNonNull» «name» = new «typeName»() {
                @java.io.Serial
                private static final long serialVersionUID = 1L;

                @«override»
                public «CLASS.importedName»<«typeName»> «Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME»() {
                    return «typeName».class;
                }

                @«override»
                public int hashCode() {
                    return «typeName».class.hashCode();
                }

                @«override»
                public boolean equals(final «objectType.importedName» obj) {
                    return obj == this || obj instanceof «typeName» other
                        && «typeName».class.equals(other.«Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME»());
                }

                @«override»
                public «STRING.importedName» toString() {
                    return «MOREOBJECTS.importedName».toStringHelper("«typeName»").add("qname", QNAME).toString();
                }

                @java.io.Serial
                private Object readResolve() throws java.io.ObjectStreamException {
                    return «name»;
                }
            };
        '''
    }

    def protected checkArgument(GeneratedProperty property, Restrictions restrictions, Type actualType, String value) '''
       «IF restrictions.getRangeConstraint.isPresent»
           «IF actualType instanceof ConcreteType»
               «AbstractRangeGenerator.forType(actualType).generateRangeCheckerCall(property.getName.toFirstUpper, value)»
           «ELSE»
               «AbstractRangeGenerator.forType(actualType).generateRangeCheckerCall(property.getName.toFirstUpper, value + ".getValue()")»
           «ENDIF»
       «ENDIF»
       «val fieldName = property.fieldName»
       «IF restrictions.getLengthConstraint.isPresent»
           «IF actualType instanceof ConcreteType»
               «LengthGenerator.generateLengthCheckerCall(fieldName, value)»
           «ELSE»
               «LengthGenerator.generateLengthCheckerCall(fieldName, value + ".getValue()")»
           «ENDIF»
       «ENDIF»

       «val fieldUpperCase = fieldName.toUpperCase(Locale.ROOT)»
       «FOR currentConstant : type.getConstantDefinitions»
           «IF currentConstant.getName.startsWith(TypeConstants.PATTERN_CONSTANT_NAME)
               && fieldUpperCase.equals(currentConstant.getName.substring(TypeConstants.PATTERN_CONSTANT_NAME.length))»
           «CODEHELPERS.importedName».checkPattern(value, «Constants.MEMBER_PATTERN_LIST»«fieldName», «Constants.MEMBER_REGEX_LIST»«fieldName»);
           «ENDIF»
       «ENDFOR»
    '''
}
