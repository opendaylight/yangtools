/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;

/**
 * Factory for {@link BindingStreamEventWriter}, which provides stream writers which translates data and delegates
 * calls to {@link NormalizedNodeStreamWriter}.
 */
public interface BindingNormalizedNodeWriterFactory {
    /**
     * Creates a {@link BindingStreamEventWriter} for data tree path which will translate to NormalizedNode model
     * and invoke proper events on supplied {@link NormalizedNodeStreamWriter}.
     *
     * <p>Also provides translation of supplied Instance Identifier to {@link YangInstanceIdentifier} so client code,
     * does not need to translate that separately.
     *
     * <p>If {@link YangInstanceIdentifier} is not needed, please use
     * {@link #newWriter(DataObjectReference, NormalizedNodeStreamWriter)} method to conserve resources.
     *
     * @param path
     *            Binding Path in conceptual data tree, for which writer should
     *            be instantiated
     * @param domWriter
     *            Stream writer on which events will be invoked.
     * @return Instance Identifier and {@link BindingStreamEventWriter}
     *         which will write to supplied {@link NormalizedNodeStreamWriter}.
     * @throws IllegalArgumentException If supplied Instance Identifier is not valid.
     */
    @NonNull Entry<YangInstanceIdentifier, BindingStreamEventWriter> newWriterAndIdentifier(
            @NonNull DataObjectReference<?> path, @NonNull NormalizedNodeStreamWriter domWriter);

    /**
     * Creates a {@link BindingStreamEventWriter} for data tree path which will translate to NormalizedNode model
     * and invoke proper events on supplied {@link NormalizedNodeStreamWriter}.
     *
     * <p>This variant does not provide YANG instance identifier and is useful for use-cases, where
     * {@link DataObjectReference} translation is done in other way, or YANG instance identifier is unnecessary
     * (e.g. notifications, RPCs).
     *
     * @param path Binding Path in conceptual data tree, for which writer should
     *            be instantiated
     * @param domWriter Stream writer on which events will be invoked.
     * @return {@link BindingStreamEventWriter}
     *         which will write to supplied {@link NormalizedNodeStreamWriter}.
     * @throws IllegalArgumentException If supplied Instance Identifier is not valid.
     */
    @NonNull BindingStreamEventWriter newWriter(@NonNull DataObjectReference<?> path,
            @NonNull NormalizedNodeStreamWriter domWriter);

    /**
     * Creates a {@link BindingStreamEventWriter} for RPC data which will translate to NormalizedNode model and invoke
     * proper events on supplied {@link NormalizedNodeStreamWriter}.
     *
     * @param rpcInputOrOutput Binding class representing RPC input or output,
     *            for which writer should be instantiated
     * @param domWriter
     *            Stream writer on which events will be invoked.
     * @return {@link BindingStreamEventWriter} which will write to supplied
     *         {@link NormalizedNodeStreamWriter}.
     */
    @NonNull BindingStreamEventWriter newRpcWriter(@NonNull Class<? extends DataContainer> rpcInputOrOutput,
            @NonNull NormalizedNodeStreamWriter domWriter);

    /**
     * Creates a {@link BindingStreamEventWriter} for notification which will translate to NormalizedNode model
     * and invoke proper events on supplied {@link NormalizedNodeStreamWriter}.
     *
     * @param notification Binding class representing notification,
     *            for which writer should be instantiated
     * @param domWriter
     *            Stream writer on which events will be invoked.
     * @return {@link BindingStreamEventWriter} which will write to supplied
     *         {@link NormalizedNodeStreamWriter}.
     */
    @NonNull BindingStreamEventWriter newNotificationWriter(@NonNull Class<? extends Notification<?>> notification,
            @NonNull NormalizedNodeStreamWriter domWriter);

    /**
     * Creates a {@link BindingStreamEventWriter} for action input which will translate to NormalizedNode model
     * and invoke proper events on supplied {@link NormalizedNodeStreamWriter}.
     *
     * @param action Binding class representing action for which writer should be instantiated
     * @param domWriter Stream writer on which events will be invoked.
     * @return {@link BindingStreamEventWriter} which will write to supplied {@link NormalizedNodeStreamWriter}.
     */
    @NonNull BindingStreamEventWriter newActionInputWriter(@NonNull Class<? extends Action<?, ?, ?>> action,
            @NonNull NormalizedNodeStreamWriter domWriter);

    /**
     * Creates a {@link BindingStreamEventWriter} for action output which will translate to NormalizedNode model
     * and invoke proper events on supplied {@link NormalizedNodeStreamWriter}.
     *
     * @param action Binding class representing action for which writer should be instantiated
     * @param domWriter Stream writer on which events will be invoked.
     * @return {@link BindingStreamEventWriter} which will write to supplied {@link NormalizedNodeStreamWriter}.
     */
    @NonNull BindingStreamEventWriter newActionOutputWriter(@NonNull Class<? extends Action<?, ?, ?>> action,
            @NonNull NormalizedNodeStreamWriter domWriter);
}
