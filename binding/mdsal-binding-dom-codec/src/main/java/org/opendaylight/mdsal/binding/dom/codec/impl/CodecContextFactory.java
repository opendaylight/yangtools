/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.loader.BindingClassLoader;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.ListRuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Immutable factory, which provides access to runtime context, create leaf nodes and provides path argument codecs.
 *
 * <p>
 * During lifetime of factory all calls for same arguments to method must return equal result (not necessary same
 * instance of result).
 */
sealed interface CodecContextFactory permits BindingCodecContext {
    /**
     * Returns immutable runtime context associated with this factory.
     *
     * @return runtime context
     */
    BindingRuntimeContext getRuntimeContext();

    /**
     * Returns leaf nodes for supplied data container and parent class.
     *
     * @param type Binding type for which leaves should be loaded.
     * @param schema  Instantiated schema of binding type.
     * @return Map of local name to leaf node context.
     */
    ImmutableMap<Method, ValueNodeCodecContext> getLeafNodes(Class<?> type, EffectiveStatement<?, ?> schema);

    /**
     * Returns Path argument codec for list item.
     *
     * @param listClz Type of list item
     * @param type Schema of list item
     * @return Path argument codec for supplied list item.
     */
    IdentifiableItemCodec getPathArgumentCodec(Class<?> listClz, ListRuntimeType type);

    /**
     * Return the codec loader associated with this factory.
     *
     * @return A codec loader instance
     */
    @NonNull BindingClassLoader getLoader();

    @NonNull DataObjectStreamer<?> getDataObjectSerializer(Class<?> type);

    DataObjectSerializer getEventStreamSerializer(Class<?> type);
}
