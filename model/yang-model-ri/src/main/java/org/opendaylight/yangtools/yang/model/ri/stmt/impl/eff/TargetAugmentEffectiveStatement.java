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

final class TargetAugmentEffectiveStatement extends AbstractAugmentEffectiveStatement {
    private final @NonNull DeclaredAugmentEffectiveStatement declaredView;

    TargetAugmentEffectiveStatement(final DeclaredAugmentEffectiveStatement declaredView,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declaredView.getDeclared(), substatements);
        this.declaredView = requireNonNull(declaredView);
    }

    @Override
    public SchemaNodeIdentifier argument() {
        return declaredView.argument();
    }

    @Override
    @Deprecated(since = "7.0.9", forRemoval = true)
    public Optional<AugmentationSchemaNode> getOriginalDefinition() {
        return declaredView.getOriginalDefinition().map(AugmentationSchemaNode::targetView);
    }

    @Override
    public int flags() {
        return declaredView.flags();
    }

    @Override
    public QNameModule getQNameModule() {
        return declaredView.getQNameModule();
    }

    @Override
    public AugmentationSchemaNode declaredView() {
        return declaredView;
    }

    @Override
    public AugmentationSchemaNode targetView() {
        return this;
    }

    @Override
    public AugmentEffectiveStatement withDeclaredSchemaTree() {
        return declaredView;
    }

    @Override
    public AugmentEffectiveStatement withTargetSchemaTree() {
        return this;
    }
}
