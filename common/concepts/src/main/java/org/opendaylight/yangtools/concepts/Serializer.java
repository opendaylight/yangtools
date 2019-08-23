/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import org.eclipse.jdt.annotation.NonNull;

/**
 * An entity which is able to convert some input into a product.
 *
 * @param <P> Product type
 * @param <I> Input type
 * @param <X> Error exception type
 */
public interface Serializer<P, I, X extends Exception> {
    /**
     * Convert an input into a product.
     *
     * @param input Input
     * @return A product
     * @throws NullPointerException if input is null
     * @throws X when input is not valid
     */
    @NonNull P serialize(@NonNull I input) throws X;
}
