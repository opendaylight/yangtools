/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import org.opendaylight.yangtools.binding.impl.TheUnsafeSecret;

/**
 * Marker interface for unsafe access. An instance of this interface is provided within this module and acts as proof of
 * invocation path.
 *
 * @since 15.0.3
 */
public sealed interface UnsafeSecret permits TheUnsafeSecret {
    // Nothing else
}
