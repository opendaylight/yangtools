/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.feature;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.Default;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.SchemaNodeMixin;

class EmptyFeatureEffectiveStatement extends Default<QName, FeatureStatement>
        implements FeatureDefinition, FeatureEffectiveStatement, SchemaNodeMixin<QName, FeatureStatement> {
    private final @NonNull SchemaPath path;
    private final int flags;

    EmptyFeatureEffectiveStatement(final FeatureStatement declared, final SchemaPath path, final int flags) {
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
    public final SchemaPath getPath() {
        return path;
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
        if (!(obj instanceof EmptyFeatureEffectiveStatement)) {
            return false;
        }
        final EmptyFeatureEffectiveStatement other = (EmptyFeatureEffectiveStatement) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "[name=" + getQName() + "]";
    }
}
