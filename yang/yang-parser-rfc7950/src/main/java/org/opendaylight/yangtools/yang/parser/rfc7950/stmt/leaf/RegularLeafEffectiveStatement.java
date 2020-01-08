/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;

final class RegularLeafEffectiveStatement extends AbstractLeafEffectiveStatement {
    private final @Nullable LeafSchemaNode original;
    private final @NonNull ImmutableList<MustDefinition> mustConstraints;

    RegularLeafEffectiveStatement(final LeafStatement declared, final SchemaPath path, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final ImmutableList<MustDefinition> mustConstraints, final LeafSchemaNode original) {
        super(declared, path, flags, substatements);
        this.mustConstraints = requireNonNull(mustConstraints);
        this.original = original;
    }

    @Override
    public Optional<LeafSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public ImmutableList<MustDefinition> getMustConstraints() {
        return mustConstraints;
    }
}
