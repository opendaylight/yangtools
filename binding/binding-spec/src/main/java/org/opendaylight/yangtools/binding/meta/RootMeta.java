/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.meta;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Metadata about the YANG module underlying a set of generated classes.
 *
 * @param <R> the {@link DataRoot} type this metadata is tied to
 * @param rootClass the {@link DataRoot} class this metadata is tied to
 * @param moduleInfo the {@link YangModuleInfo}
 * @since 15.0.0
 */
public record RootMeta<R extends DataRoot<R>>(
        @NonNull Class<R> rootClass,
        @NonNull YangModuleInfo moduleInfo) implements Immutable {
    public RootMeta {
        if (!DataRoot.class.isAssignableFrom(rootClass)) {
            throw new IllegalArgumentException(rootClass + " is not a DataRoot");
        }
        requireNonNull(moduleInfo);
    }
}
