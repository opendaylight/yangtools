/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;

/**
 * Reference of statement source. Statement source reference serves to provide information, why a statement was defined
 * and introduced in model.
 *
 * <p>
 * Reasons for introduction of statement could be various, but most obvious one is explicit declaration in model source
 * text such as {@link DeclarationInTextSource}.
 */
public interface StatementSourceReference {
    /**
     * Returns source type.
     *
     * @return {@link StatementSource#DECLARATION} if statement was explicitly
     *         declared in YANG model source, {@link StatementSource#CONTEXT} if statement
     *         was inferred.
     */
    StatementSource getStatementSource();

    /**
     * Returns human readable representation of statement source.
     *
     * <p>
     * Implementations of this interface should override {@link #toString()},
     * since it may be used in error reporting to provide context
     * information for model designer to debug errors in its mode.
     *
     * @return human readable representation of statement source.
     */
    @Override
    String toString();
}
