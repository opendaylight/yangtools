/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

/**
 * An extension of the {@link Builder} concept which allows an implementation
 * of this interface to be used in collections instead of the product. Given
 * the mutable nature of Builders, this has to be done very carefully.
 *
 * @param <P> Product type
 */
public interface ProductAwareBuilder<P> extends Builder<P> {
    /**
     * Return the hash code of the product. This has to be equivalent
     * of calling {@link #build()}.{@link Object#hashCode()}.
     *
     * @return the hash code of the product.
     */
    int productHashCode();

    /**
     * Check whether an instance of the product that would be created
     * by the builder is equal to an existing instance. This has to
     * be equivalent of calling {@link #build()}.{@link Object#equals(Object)}.
     *
     * @param product Product instance
     * @return Return true if the product is equal to the would-be
     *         product of the builder.
     */
    boolean productEquals(Object product);
}
