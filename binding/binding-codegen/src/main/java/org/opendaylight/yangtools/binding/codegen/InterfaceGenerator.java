/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.Type;

final class InterfaceGenerator implements CodeGenerator {
    @Override
    public boolean isAcceptable(final Type type) {
        return type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)
                && !(type instanceof EnumTypeObjectArchetype);
    }

    /**
     * Generates JAVA source code for generated type <code>Type</code>. The code
     * is generated according to the template source code template which is
     * written in XTEND language.
     */
    @Override
    public String generate(final Type type) {
        return switch (type) {
            case DataRootArchetype dataRoot -> new DataRootTemplate(dataRoot).generate();
            // Note: unfortunate class hierarchy design
            case GeneratedTransferObject gto -> "";
            case GeneratedType gt -> new InterfaceTemplate(gt).generate();
            default -> "";
        };
    }

    @Override
    public String getUnitName(final Type type) {
        return type.simpleName();
    }
}
