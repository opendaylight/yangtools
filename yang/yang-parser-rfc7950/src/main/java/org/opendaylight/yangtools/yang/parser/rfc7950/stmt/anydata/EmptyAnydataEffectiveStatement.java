/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anydata;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.Default;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.OpaqueDataSchemaNodeMixin;

class EmptyAnydataEffectiveStatement extends Default<QName, AnydataStatement>
        implements AnydataEffectiveStatement, AnydataSchemaNode, OpaqueDataSchemaNodeMixin<AnydataStatement> {
    private final @NonNull SchemaPath path;
    private final AnydataSchemaNode original;
    private final int flags;

    EmptyAnydataEffectiveStatement(final AnydataStatement declared, final SchemaPath path, final int flags,
            final @Nullable AnydataSchemaNode original) {
        super(declared);
        this.path = requireNonNull(path);
        this.flags = flags;
        this.original = original;
    }

    @Override
    @Deprecated
    public final SchemaPath getPath() {
        return path;
    }

    @Override
    public final int flags() {
        return flags;
    }

    @Override
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
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("qname", getQName()).add("path", getPath()).toString();
    }
}
