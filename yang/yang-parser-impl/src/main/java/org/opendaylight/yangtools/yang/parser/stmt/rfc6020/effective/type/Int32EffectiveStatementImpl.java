/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.Int32;

public final class Int32EffectiveStatementImpl extends AbstractIntegerBuiltInTypeEffectiveStatement {
    private static final Int32EffectiveStatementImpl INSTANCE = new Int32EffectiveStatementImpl();

    private Int32EffectiveStatementImpl() {

    }

    public static Int32EffectiveStatementImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public IntegerTypeDefinitionBuilder newTypeDefinitionBuilder() {
        return new IntegerTypeDefinitionBuilder() {
            @Override
            public IntegerTypeDefinition build() {
                // FIXME: implement this using the types from DerivedType or something
                throw new UnsupportedOperationException("Implement this");
            }
        };
    }

    @Override
    public TypeDefinition<?> getTypeDefinition() {
        return Int32.getInstance();
    }
}
