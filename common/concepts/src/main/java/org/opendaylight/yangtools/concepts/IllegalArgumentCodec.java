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
 * Utility interface, which specializes {@link UncheckedCodec} to {@link IllegalArgumentException}. This is useful
 * for migration purposes. Implementations should consider subclassing {@link AbstractIllegalArgumentCodec}.
 *
 * @param <P> Product type
 * @param <I> Input type
 */
@Beta
public interface IllegalArgumentCodec<P, I> extends UncheckedCodec<P, I, IllegalArgumentException> {

}
