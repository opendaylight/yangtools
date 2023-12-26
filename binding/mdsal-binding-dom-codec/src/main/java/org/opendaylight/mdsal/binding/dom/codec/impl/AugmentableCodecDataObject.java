/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;

/**
 * A base class for {@link DataObject}s which are also {@link Augmentable}, backed by {@link DataObjectCodecContext}.
 * While this class is public, it not part of API surface and is an implementation detail. The only reason for it being
 * public is that it needs to be accessible by code generated at runtime.
 *
 * @param <T> DataObject type
 */
public abstract class AugmentableCodecDataObject<T extends DataObject & Augmentable<T>>
        extends CodecDataObject<T> implements Augmentable<T> {
    private static final VarHandle CACHED_AUGMENTATIONS;

    static {
        try {
            CACHED_AUGMENTATIONS = MethodHandles.lookup().findVarHandle(AugmentableCodecDataObject.class,
                "cachedAugmentations", ImmutableMap.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // Used via VarHandle
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile ImmutableMap<Class<? extends Augmentation<T>>, Augmentation<T>> cachedAugmentations;

    protected AugmentableCodecDataObject(final AbstractDataObjectCodecContext<T, ?> context,
            final DataContainerNode data) {
        super(context, data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <A extends Augmentation<T>> @Nullable A augmentation(final Class<A> augmentationType) {
        requireNonNull(augmentationType, "Supplied augmentation must not be null.");

        final var aug = acquireAugmentations();
        if (aug != null) {
            return (A) aug.get(augmentationType);
        }

        @SuppressWarnings("rawtypes")
        final var augCtx = (AugmentationCodecContext<A>) codecContext().streamChild((Class) augmentationType);
        // Due to binding specification not representing grouping instantiations we can end up having the same
        // augmentation applied to a grouping multiple times. While these augmentations have the same shape, they are
        // still represented by distinct binding classes and therefore we need to make sure the result matches
        // the augmentation the user is requesting -- otherwise a strict receiver would end up with a cryptic
        // ClassCastException.
        if (augCtx != null && augmentationType.isAssignableFrom(augCtx.getBindingClass())) {
            final var augObj = augCtx.filterFrom(codecData());
            if (augObj != null) {
                return augObj;
            }
        }
        return null;
    }

    @Override
    public final ImmutableMap<Class<? extends Augmentation<T>>, Augmentation<T>> augmentations() {
        final var local = acquireAugmentations();
        return local != null ? local : loadAugmentations();
    }

    private ImmutableMap<Class<? extends Augmentation<T>>, Augmentation<T>> acquireAugmentations() {
        return (ImmutableMap<Class<? extends Augmentation<T>>, Augmentation<T>>) CACHED_AUGMENTATIONS.getAcquire(this);
    }

    @SuppressWarnings("unchecked")
    private @NonNull ImmutableMap<Class<? extends Augmentation<T>>, Augmentation<T>> loadAugmentations() {
        @SuppressWarnings("rawtypes")
        final Map extracted = codecContext().getAllAugmentationsFrom(codecData());
        final var ret = ImmutableMap.<Class<? extends Augmentation<T>>, Augmentation<T>>copyOf(extracted);
        final Object witness = CACHED_AUGMENTATIONS.compareAndExchangeRelease(this, null, ret);
        return witness == null ? ret : (ImmutableMap<Class<? extends Augmentation<T>>, Augmentation<T>>) witness;
    }
}
