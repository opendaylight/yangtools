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

/**
 * Model statement. There are two base types of model statements:
 * <ul>
 * <li>{@link DeclaredStatement} - Statement representation as was defined in original
 * source. This representation could be used during computation of effective model
 * or during transforming YANG model from one serialization format to other.
 * </li>
 * <li>
 * {@link EffectiveStatement} - Representation of effective statement - this
 * statement may be different from declared, in such way, that it contains additional
 * substatements, provides access to model namespaces. Some effective statements may be not
 * directly declared in YANG source, but could be inferred by semantic processing of
 * other statements (eg. uses, augment).
 * </li>
 * </ul>
 *
 * @param <A> Argument type ({@link Void} if statement does not have argument.)
 */
public interface ModelStatement<A> {
    /**
     * Statement Definition of this statement.
     *
     * @return definition of this statement.
     */
    @NonNull StatementDefinition statementDefinition();

    /**
     * Returns statement argument.
     *
     * @return statement argument or null if statement does not have argument.
     */
    // FIXME: figure out @NonNull argument override in subclasses and switch to JDT @Nullable
    @Nullable A argument();

    /**
     * Returns statement source, which denotes if statement was
     * explicitly declared in original model or inferred during
     * semantic processing of model.
     *
     * @return statement source.
     */
    @NonNull StatementSource getStatementSource();
}
