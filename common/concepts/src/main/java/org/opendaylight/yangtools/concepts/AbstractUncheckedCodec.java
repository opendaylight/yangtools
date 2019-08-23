/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An abstract base class enforcing nullness contract around {@link UncheckedCodec} interface.
 *
 * @param <P> Product type
 * @param <I> Input type
 * @param <X> Error exception type
 */
@Beta
@NonNullByDefault
public abstract class AbstractUncheckedCodec<P, I, X extends RuntimeException> extends AbstractCodec<P, I, X>
        implements UncheckedCodec<P, I, X> {
    @Override
    protected abstract @NonNull I deserializeImpl(@NonNull P product);

    // implementation is guarded from nulls and verified not to return null
    @Override
    protected abstract @NonNull P serializeImpl(@NonNull I input);
}
