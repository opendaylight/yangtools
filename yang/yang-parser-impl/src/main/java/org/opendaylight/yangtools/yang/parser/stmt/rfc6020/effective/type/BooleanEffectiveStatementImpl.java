/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Preconditions;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BooleanType;

public final class BooleanEffectiveStatementImpl extends AbstractBuiltInEffectiveStatement<BooleanTypeDefinition> {
    private static final BooleanEffectiveStatementImpl INSTANCE = new BooleanEffectiveStatementImpl();

    private BooleanEffectiveStatementImpl() {

    }

    public static BooleanEffectiveStatementImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanTypeDefinition getTypeDefinition() {
        return BooleanType.getInstance();
    }

    @Override
    public TypeDefinitionBuilder<BooleanTypeDefinition> newTypeDefinitionBuilder() {
        return new AbstractTypeDefinitionBuilder<BooleanTypeDefinition>() {
            @Override
            public BooleanTypeDefinition build() {
                final String value = getDefaultValue();

                Preconditions.checkArgument("true".equals(value) || "false".equals(value),
                    "Boolean type allows default value to be either \"true\" or \"false\", not \"\"%s\"", value);

                // FIXME: this is wrong, create a proper instance
                return BooleanType.getInstance();
            }
        };
    }
}
