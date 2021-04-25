/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.Default;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.SchemaNodeMixin;

public class EmptyFeatureEffectiveStatement extends Default<QName, FeatureStatement>
        implements FeatureDefinition, FeatureEffectiveStatement, SchemaNodeMixin<QName, FeatureStatement> {
    private final @NonNull Immutable path;
    private final int flags;

    public EmptyFeatureEffectiveStatement(final FeatureStatement declared, final Immutable path, final int flags) {
        super(declared);
        this.path = requireNonNull(path);
        this.flags = flags;
    }

    @Override
    public final int flags() {
        return flags;
    }

    @Override
    public final @NonNull QName argument() {
        return getQName();
    }

    @Override
    @Deprecated
    public final Immutable pathObject() {
        return path;
    }

    @Override
    public final FeatureEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "[name=" + getQName() + "]";
    }
}
