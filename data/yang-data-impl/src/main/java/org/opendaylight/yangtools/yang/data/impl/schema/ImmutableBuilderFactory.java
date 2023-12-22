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
import javax.xml.transform.dom.DOMSource;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.AnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode.BuilderFactory;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAnyXmlNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAnydataNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUserLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUserMapNodeBuilder;
import org.osgi.service.component.annotations.Component;

/**
 * A {@link BuilderFactory} producing builders which produce immutable in-memory normalized nodes.
 */
@Singleton
@Component
@MetaInfServices
public final class ImmutableBuilderFactory implements BuilderFactory {
    @Inject
    public ImmutableBuilderFactory() {
        // Exposed for DI
    }

    @Override
    public <T> AnydataNode.Builder<T> newAnydataBuilder(final Class<T> objectModel) {
        return new ImmutableAnydataNodeBuilder<>(objectModel);
    }

    @Override
    public <T> AnyxmlNode.Builder<T, AnyxmlNode<T>> newAnyxmlBuilder(final Class<T> objectModel) {
        if (DOMSource.class.equals(objectModel)) {
            return (AnyxmlNode.Builder) new ImmutableAnyXmlNodeBuilder();
        }
        throw new IllegalArgumentException("Unsupported object model " + objectModel);
    }

    @Override
    public ChoiceNode.Builder newChoiceBuilder() {
        return new ImmutableChoiceNodeBuilder();
    }

    @Override
    public ChoiceNode.Builder newChoiceBuilder(final int sizeHint) {
        return new ImmutableChoiceNodeBuilder(sizeHint);
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
    public MapEntryNode.Builder newMapEntryBuilder() {
        return new ImmutableMapEntryNodeBuilder();
    }

    @Override
    public MapEntryNode.Builder newMapEntryBuilder(final int sizeHint) {
        return new ImmutableMapEntryNodeBuilder(sizeHint);
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

    @Override
    public UnkeyedListEntryNode.Builder newUnkeyedListEntryBuilder() {
        return new ImmutableUnkeyedListEntryNodeBuilder();
    }

    @Override
    public UnkeyedListEntryNode.Builder newUnkeyedListEntryBuilder(final int sizeHint) {
        return new ImmutableUnkeyedListEntryNodeBuilder(sizeHint);
    }

    @Override
    public UnkeyedListNode.Builder newUnkeyedListBuilder() {
        return new ImmutableUnkeyedListNodeBuilder();
    }

    @Override
    public UnkeyedListNode.Builder newUnkeyedListBuilder(final int sizeHint) {
        return new ImmutableUnkeyedListNodeBuilder(sizeHint);
    }

    @Override
    public <T> LeafNode.Builder<T> newLeafBuilder() {
        return new ImmutableLeafNodeBuilder<>();
    }

    @Override
    public <T> LeafSetEntryNode.Builder<T> newLeafSetEntryBuilder() {
        return new ImmutableLeafSetEntryNodeBuilder<>();
    }

    @Override
    public <T> SystemLeafSetNode.Builder<T> newSystemLeafSetBuilder() {
        return new ImmutableLeafSetNodeBuilder<>();
    }

    @Override
    public <T> SystemLeafSetNode.Builder<T> newSystemLeafSetBuilder(final int sizeHint) {
        return new ImmutableLeafSetNodeBuilder<>(sizeHint);
    }

    @Override
    public <T> UserLeafSetNode.Builder<T> newUserLeafSetBuilder() {
        return new ImmutableUserLeafSetNodeBuilder<>();
    }

    @Override
    public <T> UserLeafSetNode.Builder<T> newUserLeafSetBuilder(final int sizeHint) {
        return new ImmutableUserLeafSetNodeBuilder<>(sizeHint);
    }
}
