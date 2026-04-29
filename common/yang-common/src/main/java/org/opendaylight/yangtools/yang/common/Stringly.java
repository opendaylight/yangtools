/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Dedicated base type for types representing a {@code type string} value. These come in three basic shapes:
 * <ol>
 *   <li>{@link JString}, which encapsulates an unvalidated String</li>
 *   <li>{@link VString}, which encapsulates a valid, perhaps non-canonical, value of a YANG-defined type</li>
 *   <li>{@link CString}, which encapsulates a canonical value of a YANG-defined type</li>
 * </ol>
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public sealed interface Stringly permits JString, VString {
    /**
     * {@return the raw String representation}
     */
    String toRawString();

    @Override
    int hashCode();

    @Override
    boolean equals(@Nullable Object obj);

    @Override
    String toString();
}
