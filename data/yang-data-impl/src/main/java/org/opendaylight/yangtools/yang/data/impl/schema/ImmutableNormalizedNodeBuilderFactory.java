/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeBuilderFactory;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAnydataNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUserMapNodeBuilder;
import org.osgi.service.component.annotations.Component;

/**
 * A {@link NormalizedNodeBuilderFactory} producing builders which produce immutable in-memory normalized nodes.
 */
@Singleton
@Component
@MetaInfServices
final class ImmutableNormalizedNodeBuilderFactory implements NormalizedNodeBuilderFactory {
    @Inject
    public ImmutableNormalizedNodeBuilderFactory() {
        // Exposed for DI
    }

    @Override
    public <T> AnydataNode.Builder<T> newAnydataBuilder(final Class<T> objectModel) {
        return new ImmutableAnydataNodeBuilder<>(objectModel);
    }

    @Override
    public ContainerNode.Builder newContainerBuilder() {
        return new ImmutableContainerNodeBuilder();
    }

    @Override
    public ContainerNode.Builder newContainerBuilder(final int sizeHint) {
        return new ImmutableContainerNodeBuilder(sizeHint);
    }

    @Override
    public SystemMapNode.Builder newSystemMapBuilder() {
        return new ImmutableMapNodeBuilder();
    }

    @Override
    public SystemMapNode.Builder newSystemMapBuilder(final int sizeHint) {
        return new ImmutableMapNodeBuilder(sizeHint);
    }

    @Override
    public UserMapNode.Builder newUserMapBuilder() {
        return new ImmutableUserMapNodeBuilder();
    }

    @Override
    public UserMapNode.Builder newUserMapBuilder(final int sizeHint) {
        return new ImmutableUserMapNodeBuilder(sizeHint);
    }
}
