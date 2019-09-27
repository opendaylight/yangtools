/*
 * Copyright (c) 2019 PANTHEoN.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.annotations.Beta;

/**
 * A specialization of {@link Serializer}, which is guaranteed to throws unchecked exceptions.
 *
 * @param <P> Product type
 * @param <I> Input type
 * @param <X> Error exception type
 */
@Beta
public interface UncheckedDeserializer<P, I, X extends RuntimeException> extends Serializer<P, I, X> {
    @Override
    P serialize(I input) throws X;
}
