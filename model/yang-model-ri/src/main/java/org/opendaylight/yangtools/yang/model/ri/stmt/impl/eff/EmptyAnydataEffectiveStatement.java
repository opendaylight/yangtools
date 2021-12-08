/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.Default;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.OpaqueDataSchemaNodeMixin;

public class EmptyAnydataEffectiveStatement extends Default<QName, AnydataStatement>
        implements AnydataEffectiveStatement, AnydataSchemaNode, OpaqueDataSchemaNodeMixin<AnydataStatement> {
    private final @NonNull Immutable path;
    private final AnydataSchemaNode original;
    private final int flags;

    public EmptyAnydataEffectiveStatement(final AnydataStatement declared, final Immutable path, final int flags,
            final @Nullable AnydataSchemaNode original) {
        super(declared);
        this.path = requireNonNull(path);
        this.flags = flags;
        this.original = original;
    }

    public EmptyAnydataEffectiveStatement(final EmptyAnydataEffectiveStatement original, final Immutable path,
            final int flags, final @Nullable AnydataSchemaNode newOriginal) {
        super(original);
        this.path = requireNonNull(path);
        this.flags = flags;
        this.original = newOriginal;
    }

    @Override
    public final Immutable pathObject() {
        return path;
    }

    @Override
    public final int flags() {
        return flags;
    }

    @Override
    @Deprecated(since = "7.0.9", forRemoval = true)
    public final Optional<AnydataSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public final Optional<ContainerSchemaNode> getDataSchema() {
        /*
         * :TODO we need to determine a way how to set schema of AnyData
         */
        return Optional.empty();
    }

    @Override
    public final AnydataEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    protected final ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("qname", getQName());
    }
}
