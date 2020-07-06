/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesEffectiveStatement;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesSchemaNode;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class Effective
        extends UnknownEffectiveStatementBase<Void, GetFilterElementAttributesStatement>
        implements GetFilterElementAttributesEffectiveStatement, GetFilterElementAttributesSchemaNode {
    private final @NonNull SchemaPath path;

    Effective(final StmtContext<Void, GetFilterElementAttributesStatement, ?> ctx) {
        super(ctx);
        path = ctx.coerceParentContext().getSchemaPath().get().createChild(
            ctx.getPublicDefinition().getStatementName());
    }

    @Override
    public QName getQName() {
        return path.getLastComponent();
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return path;
    }
}