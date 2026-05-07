/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A string corresponding to {@link Package#getName()}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface PackageName extends Comparable<PackageName>, Immutable
        permits BindingPackageName, JavaPackageName {

    @Override
    int hashCode();

    @Override
    boolean equals(@Nullable Object obj);

    @Override
    String toString();
}
