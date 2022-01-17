/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Builder object which produces a product.
 *
 * @param <P> Product of builder
 * @deprecated This is an over-arching concept whose {@link #build()} method hides caller hierarchy. Users are advised
 *             to migrate away, either without a replacement interface, or with a proper domain-specific interface.
 */
@Deprecated(since = "8.0.0", forRemoval = true)
public interface CheckedBuilder<P, E extends Exception> extends Mutable {
    /**
     * Returns instance of the product. Multiple calls to this method are not required to return same instance if
     * the state of the builder has changed.
     *
     * @return A newly-built instance
     * @throws E if the builder's state is not sufficiently initialized
     */
    @NonNull P build() throws E;
}
