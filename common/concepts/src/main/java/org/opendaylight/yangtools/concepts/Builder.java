/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

/**
 * Builder object which produces a product.
 *
 * @param <P> Product of builder
 * @author Tony Tkacik &lt;ttkacik@cisco.com&gt;
 * @deprecated This is an over-arching concept whose {@link #build()} method hides caller hierarchy. Users are advised
 *             to migrate away, either without a replacement interface, or with a proper domain-specific interface.
 */
@Deprecated(since = "8.0.0")
public interface Builder<P> extends CheckedBuilder<P, IllegalArgumentException> {
    @Override
    P build();
}
