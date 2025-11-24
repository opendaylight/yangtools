/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.ri.type.TypeBuilder;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;

final class TypeEffectiveStatementImpl<T extends TypeDefinition<T>, D extends TypeStatement>
        extends WithSubstatements<QName, D> implements TypeEffectiveStatement<D> {
    private final @NonNull T typeDefinition;

    TypeEffectiveStatementImpl(final D declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final TypeBuilder<T> builder) {
        super(declared, substatements);

        for (var stmt : substatements) {
            if (stmt instanceof UnknownSchemaNode unknown) {
                builder.addUnknownSchemaNode(unknown);
            }
        }
        typeDefinition = builder.build();
    }

    @Override
    public T getTypeDefinition() {
        return typeDefinition;
    }
}
