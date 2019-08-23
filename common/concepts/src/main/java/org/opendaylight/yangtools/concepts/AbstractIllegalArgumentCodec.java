/*
 * Copyright (c) 2019 PANTHEEN.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An abstract base class enforcing nullness contract around {@link IllegalArgumentCodec} interface.
 *
 * @param <P> Product type
 * @param <I> Input type
 */
@Beta
@NonNullByDefault
public abstract class AbstractIllegalArgumentCodec<P, I> extends AbstractUncheckedCodec<P, I, IllegalArgumentException>
        implements IllegalArgumentCodec<P, I> {

}
