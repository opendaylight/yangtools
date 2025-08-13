/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import java.util.function.BiFunction;
import org.opendaylight.yangtools.binding.impl.TheUnsafeSecret;
import org.opendaylight.yangtools.binding.lib.CodeHelpers;

/**
 * Marker interface for unsafe access. An instance of this interface is provided through
 * {@link CodeHelpers#newUnsafeScalar(Object, Function, BiFunction)} and acts as proof of invocation path.
 */
public sealed interface UnsafeSecret permits TheUnsafeSecret {
    // Nothing else
}
