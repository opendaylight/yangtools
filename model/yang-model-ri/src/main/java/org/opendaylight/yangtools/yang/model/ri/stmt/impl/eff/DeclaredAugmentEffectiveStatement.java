/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;

public final class DeclaredAugmentEffectiveStatement extends AbstractAugmentEffectiveStatement {
    private final @NonNull TargetAugmentEffectiveStatement targetView;
    private final @Nullable AugmentationSchemaNode original;
    private final @NonNull SchemaNodeIdentifier argument;
    private final @NonNull QNameModule rootModuleQName;
    private final int flags;

    public DeclaredAugmentEffectiveStatement(final AugmentStatement declared, final SchemaNodeIdentifier argument,
            final int flags, final QNameModule rootModuleQName, final SchemaTreeAwareEffectiveStatement<?, ?> target,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final @Nullable AugmentationSchemaNode original) {
        super(declared, substatements);
        this.argument = requireNonNull(argument);
        this.rootModuleQName = requireNonNull(rootModuleQName);
        this.flags = flags;
        this.original = original;
        targetView = TargetAugmentEffectiveStatement.of(target, this);
    }

    @Override
    public SchemaNodeIdentifier argument() {
        return argument;
    }

    @Override
    @Deprecated(since = "7.0.9", forRemoval = true)
    public Optional<AugmentationSchemaNode> getOriginalDefinition() {
        return Optional.ofNullable(original);
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public QNameModule getQNameModule() {
        return rootModuleQName;
    }

    @Override
    public AugmentationSchemaNode declaredView() {
        return this;
    }

    @Override
    public AugmentationSchemaNode targetView() {
        return targetView;
    }

    @Override
    public AugmentEffectiveStatement withDeclaredSchemaTree() {
        return this;
    }

    @Override
    public AugmentEffectiveStatement withTargetSchemaTree() {
        return targetView;
    }
}
