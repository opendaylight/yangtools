/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

class SchemaRootCodecContext extends DataContainerCodecContext<SchemaContext> {

    private final LoadingCache<Class<?>, DataContainerCodecContext<?>> children = CacheBuilder.newBuilder().build(
            new CacheLoader<Class<?>, DataContainerCodecContext<?>>() {
                @Override
                public DataContainerCodecContext<?> load(final Class<?> key) {
                    Class<Object> parent = ClassLoaderUtils.findFirstGenericArgument(key, ChildOf.class);
                    Preconditions.checkArgument(DataRoot.class.isAssignableFrom(parent));
                    QName qname = BindingReflections.findQName(key);
                    DataSchemaNode childSchema = schema().getDataChildByName(qname);
                    return DataContainerCodecPrototype.from(key, childSchema, factory()).get();
                }
            });

    private SchemaRootCodecContext(final DataContainerCodecPrototype<SchemaContext> dataPrototype) {
        super(dataPrototype);
    }

    /**
     * Creates RootNode from supplied CodecContextFactory.
     *
     * @param factory
     *            CodecContextFactory
     * @return
     */
    static SchemaRootCodecContext create(final CodecContextFactory factory) {
        DataContainerCodecPrototype<SchemaContext> prototype = DataContainerCodecPrototype.rootPrototype(factory);
        return new SchemaRootCodecContext(prototype);
    }

    @Override
    protected DataContainerCodecContext<?> getStreamChild(final Class<?> childClass) {
        return children.getUnchecked(childClass);
    }

    @Override
    protected Optional<DataContainerCodecContext<?>> getPossibleStreamChild(final Class<?> childClass) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    protected YangInstanceIdentifier.PathArgument getDomPathArgument() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected NodeCodecContext getYangIdentifierChild(final YangInstanceIdentifier.PathArgument arg) {
        // FIXME: Optimize this
        QName childQName = arg.getNodeType();
        DataSchemaNode childSchema = schema().getDataChildByName(childQName);
        Preconditions.checkArgument(childSchema != null, "Argument %s is not valid child of %s", arg, schema());
        if (childSchema instanceof DataNodeContainer || childSchema instanceof ChoiceNode) {
            Class<?> childCls = factory().getRuntimeContext().getClassForSchema(childSchema);
            DataContainerCodecContext<?> childNode = getStreamChild(childCls);
            return childNode;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    protected Object dataFromNormalizedNode(final NormalizedNode<?, ?> normalizedNode) {
        throw new UnsupportedOperationException("Could not create Binding data representation for root");
    }
}