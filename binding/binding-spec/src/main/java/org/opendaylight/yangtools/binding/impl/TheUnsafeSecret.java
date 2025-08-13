/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.UnsafeSecret;

/**
 * The only implementation of {@link UnsafeSecret}.
 */
@NonNullByDefault
public final class TheUnsafeSecret implements UnsafeSecret {
    public static final UnsafeSecret INSTANCE = new TheUnsafeSecret();

    private TheUnsafeSecret() {
        // Hidden on purpose
    }
}
