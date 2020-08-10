/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@VisibleForTesting
public final class AnyxmlSchemaLocationEffectiveStatementImpl
        extends UnknownEffectiveStatementBase<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement>
        implements AnyxmlSchemaLocationEffectiveStatement {

    private final @NonNull SchemaPath path;

    AnyxmlSchemaLocationEffectiveStatementImpl(
            final StmtContext<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement, ?> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(ctx, substatements);
        path = ctx.coerceParentContext().getSchemaPath().get().createChild(getNodeType());
    }

    @Override
    public @NonNull QName getQName() {
        return getNodeType();
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return path;
    }
}
