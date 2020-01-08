/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf_list;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;

final class EmptyLeafListEffectiveStatement extends AbstractLeafListEffectiveStatement {
    EmptyLeafListEffectiveStatement(final LeafListStatement declared, final SchemaPath path, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, path, flags, substatements);
    }

    @Override
    public Optional<LeafSchemaNode> getOriginal() {
        return Optional.empty();
    }

    @Override
    public @NonNull Collection<? extends Object> getDefaults() {
        return ImmutableSet.of();
    }

    @Override
    public Optional<ElementCountConstraint> getElementCountConstraint() {
        return Optional.empty();
    }
}
