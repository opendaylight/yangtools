/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.annotations.Beta;

/**
 * The concept of a combined {@link Serializer} and {@link Deserializer}, which produces an object from some input.
 *
 * @param <P> Product type
 * @param <I> Input type
 * @param <X> Error exception type
 */
@Beta
public interface UncheckedCodec<P, I, X extends RuntimeException>
    extends Codec<P, I, X>, UncheckedSerializer<I, P, X>, UncheckedDeserializer<P, I, X> {

}
