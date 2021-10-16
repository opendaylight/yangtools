/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.list;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;

final class EmptyListEffectiveStatement extends AbstractListEffectiveStatement {
    EmptyListEffectiveStatement(final ListStatement declared, final Immutable path, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final ImmutableList<QName> keyDefinition) {
        super(declared, path, flags, substatements, keyDefinition);
    }

    EmptyListEffectiveStatement(final EmptyListEffectiveStatement original, final Immutable path, final int flags) {
        super(original, path, flags);
    }

    @Override
    @Deprecated(since = "7.0.9", forRemoval = true)
    public Optional<? extends SchemaNode> getOriginal() {
        return Optional.empty();
    }

    @Override
    public Optional<ElementCountConstraint> getElementCountConstraint() {
        return Optional.empty();
    }
}
