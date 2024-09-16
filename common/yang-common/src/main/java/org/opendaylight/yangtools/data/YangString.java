/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.data;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A YANG {@code string} value.
 */
@NonNullByDefault
public abstract non-sealed class YangString implements CharSequence, Comparable<YangString>, ScalarValue {

    @Override
    public abstract String toString();
}
