/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base interface for values stored in {@link UnionTypeObject}s. Each UnionTypeObject is expected to define a sealed
 * interface extending this interface and define a {@link TypeObject} extending that interface for each of the possible
 * union branches.
 *
 * @param <T> the sealed {@link UnionValue} type
 * @since 16.0.0
 */
@NonNullByDefault
public interface UnionValue<T extends UnionValue<T>> {
    // nothing ese
}
