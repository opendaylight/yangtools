/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.util.type.TypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.TypeDefinitionBinder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;

final class TypeEffectiveStatementImpl<T extends TypeDefinition<T>, D extends TypeStatement>
        extends WithSubstatements<String, D> implements TypeEffectiveStatement<D> {
    private final @NonNull T typeDefinition;

    TypeEffectiveStatementImpl(final D declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final TypeBuilder<T> builder) {
        super(declared, substatements);

        for (EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof UnknownSchemaNode) {
                builder.addUnknownSchemaNode((UnknownSchemaNode)stmt);
            }
        }
        typeDefinition = builder.build();
    }

    private TypeEffectiveStatementImpl(final TypeEffectiveStatementImpl<T, D> original, final T typeDefinition) {
        super(original);
        this.typeDefinition = requireNonNull(typeDefinition);
    }

    @Override
    public T getTypeDefinition() {
        return typeDefinition;
    }

    @NonNull TypeEffectiveStatementImpl<T, D> bindTo(final @NonNull QName qname) {
        return new TypeEffectiveStatementImpl<>(this, ((TypeDefinitionBinder<T>) typeDefinition).bindTo(qname));
    }
}
