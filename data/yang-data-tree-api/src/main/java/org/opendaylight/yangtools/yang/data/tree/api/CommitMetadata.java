/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.api;

import java.time.Instant;
import java.util.UUID;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Metadata about last modification of a {@link DataTree} instance and its components.
 */
@NonNullByDefault
public interface CommitMetadata extends Immutable {
    /**
     * The {@link UUID} of the last modification.
     *
     * @return the {@link UUID} of the last modification
     */
    UUID uuid();

    /**
     * The {@link Instant} when the last modification occurred.
     *
     * @return the {@link Instant} when the last modification occurred
     */
    Instant instant();
}
