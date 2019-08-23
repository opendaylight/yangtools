/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

/**
 * The concept of a combined {@link Serializer} and {@link Deserializer}, which produces an object from some input.
 * Implementations should consider subclassing {@link AbstractCodec}.
 *
 * @param <P> Product type
 * @param <I> Input type
 * @param <X> Error exception type
 */
public interface Codec<P, I, X extends Exception> extends Serializer<P, I, X>, Deserializer<I, P, X> {
    @Override
    I deserialize(P input) throws X;

    @Override
    P serialize(I input) throws X;
}
