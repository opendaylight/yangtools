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
 * The concept of a serializer, which produces an object from some input.
 *
 * @param <P> Product type
 * @param <I> Input type
 */
// FIXME: 4.0.0: we need error reporting, is this concept even useful?
public interface Deserializer<P, I> {
    /**
     * Produce an object base on input.
     *
     * @param input Input object
     * @return Product derived from input
     */
    @NonNull P deserialize(@NonNull I input);
}
