/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

final class TargetAugmentEffectiveStatement extends AbstractAugmentEffectiveStatement {
    private final @NonNull DeclaredAugmentEffectiveStatement declared;

    private TargetAugmentEffectiveStatement(final DeclaredAugmentEffectiveStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared.getDeclared(), substatements);
        this.declared = requireNonNull(declared);
    }

    static @NonNull TargetAugmentEffectiveStatement of(final SchemaTreeAwareEffectiveStatement<?, ?> target,
            final DeclaredAugmentEffectiveStatement declared) {
        final var substatements = declared.effectiveSubstatements();
        final var builder = ImmutableList.<EffectiveStatement<?, ?>>builderWithExpectedSize(substatements.size());

        for (var stmt : substatements) {
            if (stmt instanceof SchemaTreeEffectiveStatement) {
                final var qname = ((SchemaTreeEffectiveStatement<?>) stmt).getIdentifier();
                target.get(Namespace.class, qname).ifPresent(builder::add);
            } else {
                builder.add(stmt);
            }
        }

        return new TargetAugmentEffectiveStatement(declared, builder.build());
    }

    @Override
    public SchemaNodeIdentifier argument() {
        return declared.argument();
    }

    @Override
    @Deprecated(since = "7.0.9", forRemoval = true)
    public Optional<AugmentationSchemaNode> getOriginalDefinition() {
        return declared.getOriginalDefinition();
    }

    @Override
    public int flags() {
        return declared.flags();
    }

    @Override
    public QNameModule getQNameModule() {
        return declared.getQNameModule();
    }

    @Override
    public AugmentationSchemaNode declaredView() {
        return declared;
    }

    @Override
    public AugmentationSchemaNode targetView() {
        return this;
    }

    @Override
    public AugmentEffectiveStatement withDeclaredSchemaTree() {
        return declared;
    }

    @Override
    public AugmentEffectiveStatement withTargetSchemaTree() {
        return this;
    }
}
