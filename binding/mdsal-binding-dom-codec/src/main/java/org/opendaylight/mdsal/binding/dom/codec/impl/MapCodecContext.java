/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.ListRuntimeType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.Key;
import org.opendaylight.yangtools.yang.binding.KeyAware;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;

abstract sealed class MapCodecContext<I extends Key<D>, D extends DataObject & KeyAware<I>>
        extends ListCodecContext<D> {
    private static final class Ordered<I extends Key<D>, D extends DataObject & KeyAware<I>>
            extends MapCodecContext<I, D> {
        Ordered(final MapCodecPrototype prototype, final Method keyMethod, final IdentifiableItemCodec codec) {
            super(prototype, keyMethod, codec);
        }
    }

    static final class Unordered<I extends Key<D>, D extends DataObject & KeyAware<I>>
            extends MapCodecContext<I, D> {
        private Unordered(final MapCodecPrototype prototype, final Method keyMethod,
                final IdentifiableItemCodec codec) {
            super(prototype, keyMethod, codec);
        }

        @Override
        Map<I, D> fromMap(final MapNode map, final int size) {
            return LazyBindingMap.of(this, map, size);
        }
    }

    private final IdentifiableItemCodec codec;

    private MapCodecContext(final MapCodecPrototype prototype, final Method keyMethod,
            final IdentifiableItemCodec codec) {
        super(prototype, keyMethod);
        this.codec = requireNonNull(codec);
    }

    static @NonNull MapCodecContext<?, ?>  of(final Class<? extends DataObject> cls, final ListRuntimeType type,
            final CodecContextFactory factory) {
        return of(new MapCodecPrototype(Item.of(cls), type, factory));
    }

    static @NonNull MapCodecContext<?, ?> of(final MapCodecPrototype prototype) {
        final var bindingClass = prototype.javaClass();
        final Method keyMethod;
        try {
            keyMethod = bindingClass.getMethod(Naming.KEY_AWARE_KEY_NAME);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Required method not available", e);
        }

        final var type = prototype.runtimeType();
        final var codec = prototype.contextFactory().getPathArgumentCodec(bindingClass, type);

        return type.statement().ordering() == Ordering.SYSTEM ? new Unordered<>(prototype, keyMethod, codec)
            : new Ordered<>(prototype, keyMethod, codec);
    }

    @Override
    void addYangPathArgument(final List<YangInstanceIdentifier.PathArgument> builder,
            final InstanceIdentifier.PathArgument arg) {
        /*
         * DOM Instance Identifier for list is always represent by two entries one for map and one for children. This
         * is also true for wildcarded instance identifiers
         */
        final var yangArg = getDomPathArgument();
        builder.add(yangArg);

        if (arg instanceof IdentifiableItem<?, ?> identifiable) {
            builder.add(codec.bindingToDom(identifiable));
        } else {
            // Adding wildcarded
            builder.add(yangArg);
        }
    }

    @Override
    protected final InstanceIdentifier.PathArgument getBindingPathArgument(
            final YangInstanceIdentifier.PathArgument domArg) {
        return domArg instanceof NodeIdentifierWithPredicates nip ? codec.domToBinding(nip)
            : super.getBindingPathArgument(domArg);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    final NodeIdentifierWithPredicates serialize(final Key<?> key) {
        return codec.bindingToDom(IdentifiableItem.of((Class)getBindingClass(), (Key)key));
    }

    final @NonNull Key<?> deserialize(final @NonNull NodeIdentifierWithPredicates arg) {
        return codec.deserializeIdentifier(arg);
    }

    @Override
    public final YangInstanceIdentifier.PathArgument serializePathArgument(final InstanceIdentifier.PathArgument arg) {
        return arg instanceof IdentifiableItem<?, ?> identifiable ? codec.bindingToDom(identifiable)
            : super.serializePathArgument(arg);
    }

    @Override
    public final InstanceIdentifier.PathArgument deserializePathArgument(
        final YangInstanceIdentifier.PathArgument arg) {
        return arg instanceof NodeIdentifierWithPredicates nip ? codec.domToBinding(nip)
            : super.deserializePathArgument(arg);
    }
}
