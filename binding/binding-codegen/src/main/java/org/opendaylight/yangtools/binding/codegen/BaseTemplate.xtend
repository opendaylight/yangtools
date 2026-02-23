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

import org.opendaylight.yangtools.binding.model.api.GeneratedType
import org.opendaylight.yangtools.binding.model.api.Type
import org.opendaylight.yangtools.binding.contract.Naming

// FIXME: YANGTOOLS-1618: convert to Java
abstract class BaseTemplate extends AbstractBaseTemplate {
    new(GeneratedType type) {
        super(type)
    }

    new(AbstractJavaGeneratedType javaType, GeneratedType type) {
        super(javaType, type)
    }

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
}
