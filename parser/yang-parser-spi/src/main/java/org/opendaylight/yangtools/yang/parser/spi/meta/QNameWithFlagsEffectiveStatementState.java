/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

/**
 * Simple {@link EffectiveStatementState} for use with typical {@link SchemaTreeEffectiveStatement}s. It differentiates
 * on QName and an {@code int} flags field.
 */
@Beta
public final class QNameWithFlagsEffectiveStatementState extends EffectiveStatementState {
    private final QName qname;
    private final int flags;

    public QNameWithFlagsEffectiveStatementState(final QName qname, final int flags) {
        this.qname = requireNonNull(qname);
        this.flags = flags;
    }

    @Override
    public int hashCode() {
        return qname.hashCode() * 31 + Integer.hashCode(flags);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || obj instanceof QNameWithFlagsEffectiveStatementState other
            && flags == other.flags && qname.equals(other.qname);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("qname", qname).add("flags", flags);
    }
}
