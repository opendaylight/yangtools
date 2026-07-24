/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Dedicated type for holding a class comment. It exposes a javadoc-encoded string.
 */
@FunctionalInterface
@NonNullByDefault
interface TypeComment extends Immutable {
    /**
     * Return a javadoc snippet. This snippet is guaranteed to be safe for direct inclusion in a Java block comment.
     *
     * @return Javadoc snippet.
     */
    String getJavadoc();
}
