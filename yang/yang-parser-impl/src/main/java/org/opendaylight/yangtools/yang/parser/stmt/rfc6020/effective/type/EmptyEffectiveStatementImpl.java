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
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.EmptyType;

public final class EmptyEffectiveStatementImpl extends AbstractBuiltInEffectiveStatement<EmptyTypeDefinition> {
    private static final EmptyEffectiveStatementImpl INSTANCE = new EmptyEffectiveStatementImpl();

    private EmptyEffectiveStatementImpl() {

    }

    public static EmptyEffectiveStatementImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeDefinition<?> getTypeDefinition() {
        return EmptyType.getInstance();
    }

    @Override
    public TypeDefinitionBuilder<?> newTypeDefinitionBuilder() {
        // TODO Auto-generated method stub
        return new AbstractTypeDefinitionBuilder<EmptyTypeDefinition>() {
            @Override
            public EmptyTypeDefinition build() {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }
}
