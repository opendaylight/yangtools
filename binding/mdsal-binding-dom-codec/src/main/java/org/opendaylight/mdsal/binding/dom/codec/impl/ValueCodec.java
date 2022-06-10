/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.eclipse.jdt.annotation.NonNull;

interface ValueCodec<S, D> {
    /**
     * Produce an internal object based on an external object.
     *
     * @param input Input object
     * @return Product derived from input
     * @throws NullPointerException if input is null
     * @throws IllegalArgumentException when input is not valid
     */
    @NonNull D deserialize(@NonNull S input);

    /**
     * Produce an external object based on an internal object.
     *
     * @param input Input
     * @return An external form object
     * @throws NullPointerException if input is null
     * @throws IllegalArgumentException when input is not valid
     */
    @NonNull S serialize(@NonNull D input);
}
