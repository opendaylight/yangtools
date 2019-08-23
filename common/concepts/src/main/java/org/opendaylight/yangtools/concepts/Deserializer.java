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
 * The concept of a deserializer, which produces an object from some input.
 *
 * @param <P> Product type
 * @param <I> Input type
 * @param <X> Error exception type
 */
public interface Deserializer<P, I, X extends Exception> {
    /**
     * Produce an object base on input.
     *
     * @param input Input object
     * @return Product derived from input
     * @throws NullPointerException if input is null
     * @throws X when input is not valid
     */
    @NonNull P deserialize(@NonNull I input) throws X;
}
