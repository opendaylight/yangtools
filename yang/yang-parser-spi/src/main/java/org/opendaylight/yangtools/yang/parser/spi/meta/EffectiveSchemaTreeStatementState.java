/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

@Beta
public final class EffectiveSchemaTreeStatementState extends EffectiveStatementState {
    private final int flags;

    public EffectiveSchemaTreeStatementState(final @NonNull Immutable identity, final int flags) {
        super(identity);
        this.flags = flags;
    }

    @Override
    public int hashCode() {
        return identity().hashCode() * 31 + Integer.hashCode(flags);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EffectiveSchemaTreeStatementState)) {
            return false;
        }
        final EffectiveSchemaTreeStatementState other = (EffectiveSchemaTreeStatementState) obj;
        return flags == other.flags && identity().equals(other.identity());
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("flags", flags);
    }
}