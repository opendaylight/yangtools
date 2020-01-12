/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.list;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

final class EmptyListEffectiveStatement extends AbstractListEffectiveStatement {
    EmptyListEffectiveStatement(final ListStatement declared, final SchemaPath path, final int flags,
            final StatementSourceReference ref, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, path, flags, ref, substatements);
    }

    @Override
    public Optional<? extends SchemaNode> getOriginal() {
        return Optional.empty();
    }

    @Override
    public List<QName> getKeyDefinition() {
        return ImmutableList.of();
    }

    @Override
    public Optional<ElementCountConstraint> getElementCountConstraint() {
        return Optional.empty();
    }
}
