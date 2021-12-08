/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.SchemaNodeMixin;

abstract class AbstractIdentityEffectiveStatement extends DefaultArgument<QName, IdentityStatement>
        implements IdentityEffectiveStatement, IdentitySchemaNode, SchemaNodeMixin<QName, IdentityStatement> {
    private final @NonNull Immutable path;

    AbstractIdentityEffectiveStatement(final IdentityStatement declared, final Immutable path) {
        super(declared);
        this.path = requireNonNull(path);
    }

    @Override
    public final Immutable pathObject() {
        return path;
    }

    @Override
    public final IdentityEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    protected final ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("qname", getQName()).add("path", path);
    }
}
