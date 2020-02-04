/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anyxml;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.Default;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.OpaqueDataSchemaNodeMixin;

class EmptyAnyxmlEffectiveStatement extends Default<QName, AnyxmlStatement>
        implements AnyxmlEffectiveStatement, AnyxmlSchemaNode, OpaqueDataSchemaNodeMixin<AnyxmlStatement> {
    private final @NonNull SchemaPath path;
    private final AnyxmlSchemaNode original;
    private final int flags;

    EmptyAnyxmlEffectiveStatement(final AnyxmlStatement declared, final SchemaPath path, final int flags,
            final @Nullable AnyxmlSchemaNode original) {
        super(declared);
        this.path = requireNonNull(path);
        this.flags = flags;
        this.original = original;
    }

    @Override
    public final @NonNull SchemaPath getPath() {
        return path;
    }

    @Override
    public final int flags() {
        return flags;
    }

    @Override
    public final Optional<AnyxmlSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getQName());
        result = prime * result + Objects.hashCode(getPath());
        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EmptyAnyxmlEffectiveStatement)) {
            return false;
        }

        EmptyAnyxmlEffectiveStatement other = (EmptyAnyxmlEffectiveStatement) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("qname", getQName()).add("path", getPath()).toString();
    }
}
