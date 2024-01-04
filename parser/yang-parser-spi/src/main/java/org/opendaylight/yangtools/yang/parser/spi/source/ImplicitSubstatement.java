/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;

/**
 * An implicit sub-statement, which is implied to be always present in its parent, even if it does not appear in model
 * source.
 */
@Beta
public final class ImplicitSubstatement extends StatementSourceReference {
    private final StatementSourceReference parentRef;

    private ImplicitSubstatement(final StatementSourceReference parentRef) {
        this.parentRef = requireNonNull(parentRef);
    }

    /**
     * Create a new {@link ImplicitSubstatement}.
     *
     * @param parentRef Parent source reference
     * @return A new reference
     * @throws NullPointerException if parentRef is null
     */
    public static ImplicitSubstatement of(final StatementSourceReference parentRef) {
        return new ImplicitSubstatement(parentRef);
    }

    @Override
    public StatementOrigin statementOrigin() {
        return StatementOrigin.CONTEXT;
    }

    @Override
    public DeclarationReference declarationReference() {
        return null;
    }

    @Override
    public int hashCode() {
        return parentRef.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ImplicitSubstatement other && parentRef.equals(other.parentRef);
    }

    @Override
    public String toString() {
        return parentRef.toString();
    }
}
