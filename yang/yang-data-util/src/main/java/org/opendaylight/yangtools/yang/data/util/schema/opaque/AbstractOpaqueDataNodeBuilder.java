/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.schema.opaque;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataNode;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueIdentifier;

@Beta
public abstract class AbstractOpaqueDataNodeBuilder<T extends OpaqueDataNode> implements Builder<T> {
    private OpaqueIdentifier identifier;

    AbstractOpaqueDataNodeBuilder() {
        // Hidden on purpose
    }

    final OpaqueIdentifier identifier() {
        return identifier;
    }

    public AbstractOpaqueDataNodeBuilder<T> withIdentifier(final OpaqueIdentifier newIdentifier) {
        checkState(identifier == null, "Identifier already set to %s", identifier);
        identifier = requireNonNull(newIdentifier);
        return this;
    }

    public abstract OpaqueDataValueBuilder withValue(Object value);

    @Override
    public final T build() {
        checkState(identifier != null, "Identifier not set");
        return build(identifier);
    }

    abstract @NonNull T build(@NonNull OpaqueIdentifier identifier);
}
