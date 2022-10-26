/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Marker interface for objects which have an equivalent EffectiveStatement. This interface generally serves as a bridge
 * from legacy {@link DocumentedNode}s view of the schema to the more modern {@link EffectiveStatement}s view.
 *
 * <p>
 * This is a transitional interface tied to {@link DocumentedNode} and is expected to follow its lifecycle, i.e. it will
 * be deprecated and removed when last of its users in this package go.
 *
 * <p>
 * Implementations of this interface are recommeded to return a constant object, preferably {@code this}.
 *
 * @param <E> Type of equivalent {@link EffectiveStatement}.
 */
public interface EffectiveStatementEquivalent<E extends EffectiveStatement<?, ?>> {
    /**
     * Return {@link EffectiveStatement} representation of this object.
     *
     * @return {@link EffectiveStatement} representation of this object.
     */
    @NonNull E asEffectiveStatement();
}
