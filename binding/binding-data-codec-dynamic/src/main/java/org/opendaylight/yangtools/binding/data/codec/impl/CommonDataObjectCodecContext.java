/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.data.codec.api.CommonDataObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;


/**
 * Base implementation of {@link CommonDataObjectCodecTreeNode}, shared between {@link DataObjectCodecContext} and
 * {@link AugmentationCodecContext}. They share most of their mechanics, but notably:
 * <ol>
 *   <li>DataObjectCodecContext has an exact DistinctNodeContainer and YangInstanceIdentifier mapping and can be the
 *       target of augmentations (i.e. can implement Augmentable contract)</li>
 *   <li>AugmentationNodeContext has neither of those traits and really is just a filter of its parent
 *       DistinctNodeContainer</li>
 * </ol>
 *
 * <p>Unfortunately {@code Augmentation} is a also a {@link DataObject}, so things get a bit messy.
 *
 * <p>While this class is public, it not part of API surface and is an implementation detail. The only reason for it
 * being public is that it needs to be accessible by code generated at runtime.
 */
public abstract sealed class CommonDataObjectCodecContext<D extends DataObject, T extends CompositeRuntimeType>
        extends AnalyzedDataContainerCodecContext<D, T, CommonDataObjectCodecPrototype<T>>
        implements CommonDataObjectCodecTreeNode<D>
        permits AugmentationCodecContext, DataObjectCodecContext {
    CommonDataObjectCodecContext(final CommonDataObjectCodecPrototype<T> prototype,
            final DataContainerAnalysis<T> analysis) {
        super(prototype, analysis);
    }

    @Override
    @Deprecated(since = "13.0.0", forRemoval = true)
    public final WithStatus getSchema() {
        // FIXME: Bad cast, we should be returning an EffectiveStatement perhaps?
        return (WithStatus) prototype().runtimeType().statement();
    }

    /**
     * Returns deserialized Binding Path Argument from YANG instance identifier.
     */
    protected DataObjectStep<?> getBindingPathArgument(final PathArgument domArg) {
        return bindingArg();
    }

    protected final DataObjectStep<?> bindingArg() {
        return prototype().getBindingArg();
    }

    abstract @NonNull Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAllAugmentationsFrom(
        DataContainerNode data);
}
