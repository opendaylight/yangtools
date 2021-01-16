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

@Beta
public abstract class AbstractParserNamespace<K, V> implements ParserNamespace<K, V> {
    private final NamespaceBehaviour<K, V, ?> behaviour;

    protected AbstractParserNamespace(final NamespaceBehaviour<K, V, ?> behaviour) {
        this.behaviour = requireNonNull(behaviour);
    }

    @Override
    public final NamespaceBehaviour<K, V, ?> behaviour() {
        return behaviour;
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("behaviour", behaviour).toString();
    }
}
