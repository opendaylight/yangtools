/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;

public final class RegularLeafEffectiveStatement extends AbstractLeafEffectiveStatement {
    private final @Nullable LeafSchemaNode original;

    public RegularLeafEffectiveStatement(final LeafStatement declared, final Immutable path, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final LeafSchemaNode original) {
        super(declared, path, flags, substatements);
        this.original = original;
    }

    public RegularLeafEffectiveStatement(final AbstractLeafEffectiveStatement originalEffective, final Immutable path,
            final int flags, final LeafSchemaNode original) {
        super(originalEffective, path, flags);
        this.original = original;
    }

    @Override
    public Optional<LeafSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }
}
