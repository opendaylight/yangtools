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
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataContainer;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataNode;

@Beta
public abstract class AbstractOpaqueDataContainerBuilder<T extends OpaqueDataContainer>
        extends AbstractOpaqueDataNodeBuilder<T> {
    private final List<@NonNull OpaqueDataNode> children;

    AbstractOpaqueDataContainerBuilder() {
        this.children = new ArrayList<>();
    }

    AbstractOpaqueDataContainerBuilder(final int size) {
        this.children = new ArrayList<>(size);
    }

    public AbstractOpaqueDataContainerBuilder<T> withChild(final OpaqueDataNode child) {
        children.add(requireNonNull(child));
        return this;
    }

    public OpaqueDataValueBuilder withValue(final Object value) {
        checkState(children.isEmpty(), "Unexpected children %s", children);
        return new OpaqueDataValueBuilder().withIdentifier(identifier()).withValue(value);
    }

    @Override
    final T build(final NodeIdentifier identifier) {
        return build(identifier, ImmutableList.copyOf(children));
    }

    abstract @NonNull T build(@NonNull NodeIdentifier identifier, @NonNull ImmutableList<OpaqueDataNode> children);
}
