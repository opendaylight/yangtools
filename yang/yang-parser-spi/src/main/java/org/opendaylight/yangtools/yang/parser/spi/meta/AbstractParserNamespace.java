/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNull;

@Beta
// FIXME: YANGTOOLS-1204: integrate this into ParserNamespace
public abstract class AbstractParserNamespace<K, V> implements ParserNamespace<K, V> {
    private final @NonNull NamespaceBehaviour<K, V, ?> behaviour;
    private final @NonNull ModelProcessingPhase phase;

    protected AbstractParserNamespace(final ModelProcessingPhase phase, final NamespaceBehaviour<K, V, ?> behaviour) {
        this.phase = requireNonNull(phase);
        this.behaviour = requireNonNull(behaviour);
    }

    @Override
    public final NamespaceBehaviour<K, V, ?> behaviour() {
        return behaviour;
    }

    @Override
    public final ModelProcessingPhase phase() {
        return phase;
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("behaviour", behaviour).add("phase", phase).toString();
    }
}
