/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import java.io.IOException;
import org.opendaylight.yangtools.yang.binding.BindingSerializer;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;

/**
 * Serializer of Binding objects to Normalized Node which uses {@link BindingNormalizedNodeCache} to
 * cache already serialized values.
 *
 * <p>
 * This serializer implements {@link BindingStreamEventWriter} along with {@link BindingSerializer}.
 *
 * <p>
 * {@link BindingSerializer} interface is used by generated implementations of
 * {@link org.opendaylight.yangtools.yang.binding.DataObjectSerializer} to provide Binding object
 * for inspection and to prevent streaming of already serialized object.
 */
final class CachingNormalizedNodeSerializer extends ForwardingBindingStreamEventWriter implements
        BindingSerializer<Object, DataObject> {

    private final NormalizedNodeResult domResult;
    private final NormalizedNodeWriterWithAddChild domWriter;
    private final BindingToNormalizedStreamWriter delegate;
    private final AbstractBindingNormalizedNodeCacheHolder cacheHolder;

    CachingNormalizedNodeSerializer(final AbstractBindingNormalizedNodeCacheHolder cacheHolder,
            final DataContainerCodecContext<?, ?> subtreeRoot) {
        this.cacheHolder = cacheHolder;
        this.domResult = new NormalizedNodeResult();
        this.domWriter = new NormalizedNodeWriterWithAddChild(domResult);
        this.delegate = BindingToNormalizedStreamWriter.create(subtreeRoot, domWriter);
    }

    @Override
    protected BindingStreamEventWriter delegate() {
        return delegate;
    }

    NormalizedNode<?, ?> build() {
        return domResult.getResult();
    }

    /**
     * Serializes input if it is cached, returns null otherwise.
     *
     * <p>
     * If input is cached it uses {@link NormalizedNodeWriterWithAddChild#addChild(NormalizedNode)}
     * to provide already serialized value to underlying NormalizedNodeWriter in order to reuse
     * value instead of creating new one using Normalized Node stream APIs.
     *
     * <p>
     * Note that this optional is serialization of child node invoked from
     * {@link org.opendaylight.yangtools.yang.binding.DataObjectSerializer}, which may opt-out from
     * streaming of data when non-null result is returned.
     */
    @Override
    public NormalizedNode<?, ?> serialize(final DataObject input) {
        final BindingNormalizedNodeCache cachingSerializer = getCacheSerializer(input.implementedInterface());
        if (cachingSerializer != null) {
            final NormalizedNode<?, ?> domData = cachingSerializer.get(input);
            domWriter.addChild(domData);
            return domData;
        }
        return null;
    }

    /**
     * Serializes supplied data using stream writer with child cache enabled or using cache directly
     * if cache is avalaible also for supplied Codec node.
     *
     * @param cacheHolder Binding to Normalized Node Cache holder
     * @param subtreeRoot Codec Node for provided data object
     * @param data Data to be serialized
     * @return Normalized Node representation of data.
     */
    static NormalizedNode<?, ?> serialize(final AbstractBindingNormalizedNodeCacheHolder cacheHolder,
            final DataContainerCodecContext<?, ?> subtreeRoot, final DataObject data) {
        final BindingNormalizedNodeCache cache = cacheHolder.getCachingSerializer(subtreeRoot);
        if (cache != null) {
            return cache.get(data);
        }
        return serializeUsingStreamWriter(cacheHolder, subtreeRoot, data);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private BindingNormalizedNodeCache getCacheSerializer(final Class type) {
        if (cacheHolder.isCached(type)) {
            final DataContainerCodecContext<?, ?> currentCtx = (DataContainerCodecContext<?, ?>) delegate.current();
            if (type.equals(currentCtx.getBindingClass())) {
                return cacheHolder.getCachingSerializer(currentCtx);
            }
            return cacheHolder.getCachingSerializer(currentCtx.streamChild(type));
        }
        return null;
    }

    /**
     * Serializes supplied data using stream writer with child cache enabled.
     *
     * @param cacheHolder Binding to Normalized Node Cache holder
     * @param subtreeRoot Codec Node for provided data object
     * @param data Data to be serialized
     * @return Normalized Node representation of data.
     */
    static NormalizedNode<?, ?> serializeUsingStreamWriter(final AbstractBindingNormalizedNodeCacheHolder cacheHolder,
            final DataContainerCodecContext<?, ?> subtreeRoot, final DataObject data) {
        final CachingNormalizedNodeSerializer writer = new CachingNormalizedNodeSerializer(cacheHolder, subtreeRoot);
        try {
            subtreeRoot.eventStreamSerializer().serialize(data, writer);
            return writer.build();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}