/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.identity;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNodeDefaults;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultArgument;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.SchemaNodeMixin;

abstract class AbstractIdentityEffectiveStatement extends DefaultArgument<QName, IdentityStatement>
        implements IdentityEffectiveStatement, IdentitySchemaNode, SchemaNodeMixin<QName, IdentityStatement> {
    private final @Nullable SchemaPath path;

    AbstractIdentityEffectiveStatement(final IdentityStatement declared, final SchemaPath path) {
        super(declared);
        this.path = path;
    }

    @Override
    @Deprecated
    public final SchemaPath getPath() {
        return SchemaNodeDefaults.throwUnsupportedIfNull(this, path);
    }

    @Override
    public final IdentityEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("qname", getQName()).add("path", path).toString();
    }
}
