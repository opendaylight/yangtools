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
 *
 * @author Tony Tkacik &lt;ttkacik@cisco.com&gt;
 */
public interface Builder<P> extends Mutable {
    /**
     * Returns instance of the product. Multiple calls to this method are not required to return same instance if
     * the state of the builder has changed.
     *
     * @return A newly-built instance
     * @throws IllegalStateException if the builder's state is not sufficiently initialized
     */
    P build();
}
