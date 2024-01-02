/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Reference of statement source. Statement source reference serves to provide information, why a statement was defined
 * and introduced in model.
 *
 * <p>
 * Reasons for introduction of statement could be various, but most obvious one is explicit declaration in model source
 * text.
 */
public abstract class StatementSourceReference implements Immutable {
    /**
     * Returns the {@link StatementOrigin} associated with this reference.
     *
     * @return {@link StatementOrigin#DECLARATION} if statement was explicitly declared in YANG model source,
     *         {@link StatementOrigin#CONTEXT} if statement was inferred.
     */
    public abstract @NonNull StatementOrigin statementOrigin();

    /**
     * Returns the {@link DeclarationReference} associated with this reference, if available.
     *
     * @return A {@link DeclarationReference} or null.
     */
    public abstract @Nullable DeclarationReference declarationReference();

    /**
     * Returns human readable representation of statement source.
     *
     * <p>
     * Implementations of this interface should override {@link #toString()}, since it may be used in error reporting
     * to provide context information for model designer to debug errors in its mode.
     *
     * @return human readable representation of statement source.
     */
    @Override
    public abstract @NonNull String toString();
}
