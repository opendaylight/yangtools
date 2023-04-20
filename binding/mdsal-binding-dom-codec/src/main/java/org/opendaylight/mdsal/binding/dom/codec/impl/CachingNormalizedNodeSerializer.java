/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializer of Binding objects to Normalized Node which uses {@link DataObjectNormalizedNodeCache} to
 * cache already serialized values.
 *
 * <p>
 * This serializer implements {@link BindingStreamEventWriter} along with {@link BindingSerializer}.
 *
 * <p>
 * {@link BindingSerializer} interface is used by generated implementations of {@link DataObjectSerializer} to provide
 * Binding object for inspection and to prevent streaming of already serialized object.
 */
final class CachingNormalizedNodeSerializer extends ForwardingBindingStreamEventWriter
        implements BindingSerializer<Object, DataObject> {
    private static final Logger LOG = LoggerFactory.getLogger(CachingNormalizedNodeSerializer.class);

    private final NormalizedNodeResult domResult = new NormalizedNodeResult();
    private final NormalizedNodeWriterWithAddChild domWriter = new NormalizedNodeWriterWithAddChild(domResult);
    private final AbstractBindingNormalizedNodeCacheHolder cacheHolder;
    private final BindingToNormalizedStreamWriter delegate;

    CachingNormalizedNodeSerializer(final AbstractBindingNormalizedNodeCacheHolder cacheHolder,
            final DataContainerCodecContext<?, ?> subtreeRoot) {
        this.cacheHolder = requireNonNull(cacheHolder);
        delegate = new BindingToNormalizedStreamWriter(subtreeRoot, domWriter);
    }

    @Override
    protected AnydataBindingStreamWriter delegate() {
        return delegate;
    }

    @Override
    public void leafNode(final String localName, final Object value) throws IOException {
        if (value instanceof TypeObject typed) {
            // TypeObject is a tagging interface used for generated classes which wrap derived and restricted types.
            // They are immutable and hence we can safely wrap them in LeafNodes and reuse them, if directed to do so.
            final var type = typed.getClass();
            if (cacheHolder.isCached(type)) {
                final var context = ((DataObjectCodecContext<?, ?>) delegate.current()).getLeafChild(localName);
                if (context instanceof LeafNodeCodecContext.OfTypeObject<?> typeContext) {
                    final var cache = cacheHolder.getCachingSerializer(typeContext);
                    if (cache != null) {
                        // We have a cache hit and are thus done
                        domWriter.addChild(cache.get(typed));
                        return;
                    }

                    LOG.debug("Unexpected failure to acquire cache for context {}, skipping caching", context);
                } else {
                    LOG.debug("Context {} does not match expected TypeObject {}, skipping caching", context, typed);
                }
            }
        }
        super.leafNode(localName, value);
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
     * {@link org.opendaylight.mdsal.binding.dom.codec.impl.DataObjectSerializer}, which may opt-out from
     * streaming of data when non-null result is returned.
     */
    @Override
    public NormalizedNode serialize(final DataObject input) {
        final var cachingSerializer = getCacheSerializer(input.implementedInterface());
        if (cachingSerializer != null) {
            final var domData = cachingSerializer.get(input);
            domWriter.addChild(domData);
            return domData;
        }
        return null;
    }

    private AbstractBindingNormalizedNodeCache<DataObject, ?> getCacheSerializer(
            final Class<? extends DataObject> type) {
        if (cacheHolder.isCached(type)) {
            final var currentCtx = (DataContainerCodecContext<?, ?>) delegate.current();
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
    static NormalizedNode serializeUsingStreamWriter(final AbstractBindingNormalizedNodeCacheHolder cacheHolder,
            final DataContainerCodecContext<?, ?> subtreeRoot, final DataObject data) {
        final var writer = new CachingNormalizedNodeSerializer(cacheHolder, subtreeRoot);
        try {
            subtreeRoot.eventStreamSerializer().serialize(data, writer);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        return writer.domResult.getResult();
    }
}