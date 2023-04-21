/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.binding.contract.Naming.IDENTIFIABLE_KEY_NAME;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.ListRuntimeType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;

abstract class KeyedListNodeCodecContext<I extends Identifier<D>, D extends DataObject & Identifiable<I>>
        extends ListNodeCodecContext<D> {
    private static final class Ordered<I extends Identifier<D>, D extends DataObject & Identifiable<I>>
            extends KeyedListNodeCodecContext<I, D> {
        Ordered(final DataContainerCodecPrototype<ListRuntimeType> prototype, final Method keyMethod,
                final IdentifiableItemCodec codec) {
            super(prototype, keyMethod, codec);
        }
    }

    static final class Unordered<I extends Identifier<D>, D extends DataObject & Identifiable<I>>
            extends KeyedListNodeCodecContext<I, D> {
        Unordered(final DataContainerCodecPrototype<ListRuntimeType> prototype, final Method keyMethod,
                final IdentifiableItemCodec codec) {
            super(prototype, keyMethod, codec);
        }

        @Override
        Map<I, D> fromMap(final MapNode map, final int size) {
            return LazyBindingMap.create(this, map, size);
        }
    }

    private final IdentifiableItemCodec codec;

    KeyedListNodeCodecContext(final DataContainerCodecPrototype<ListRuntimeType> prototype,
            final Method keyMethod, final IdentifiableItemCodec codec) {
        super(prototype, keyMethod);
        this.codec = requireNonNull(codec);
    }

    @SuppressWarnings("rawtypes")
    static KeyedListNodeCodecContext create(final DataContainerCodecPrototype<ListRuntimeType> prototype) {
        final Class<?> bindingClass = prototype.getBindingClass();
        final Method keyMethod;
        try {
            keyMethod = bindingClass.getMethod(IDENTIFIABLE_KEY_NAME);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Required method not available", e);
        }

        final ListRuntimeType type = prototype.getType();
        final IdentifiableItemCodec codec = prototype.getFactory().getPathArgumentCodec(bindingClass, type);

        return type.statement().ordering() == Ordering.SYSTEM ? new Unordered<>(prototype, keyMethod, codec)
            : new Ordered<>(prototype, keyMethod, codec);
    }

    @Override
    void addYangPathArgument(final InstanceIdentifier.PathArgument arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        /*
         * DOM Instance Identifier for list is always represent by two entries one for map and one for children. This
         * is also true for wildcarded instance identifiers
         */
        if (builder == null) {
            return;
        }

        super.addYangPathArgument(arg, builder);
        if (arg instanceof IdentifiableItem<?, ?> identifiable) {
            builder.add(codec.bindingToDom(identifiable));
        } else {
            // Adding wildcarded
            super.addYangPathArgument(arg, builder);
        }
    }

    @Override
    protected InstanceIdentifier.PathArgument getBindingPathArgument(final YangInstanceIdentifier.PathArgument domArg) {
        return domArg instanceof NodeIdentifierWithPredicates
            ? codec.domToBinding((NodeIdentifierWithPredicates) domArg) : super.getBindingPathArgument(domArg);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    NodeIdentifierWithPredicates serialize(final Identifier<?> key) {
        return codec.bindingToDom(IdentifiableItem.of((Class)getBindingClass(), (Identifier)key));
    }

    @NonNull Identifier<?> deserialize(final NodeIdentifierWithPredicates arg) {
        return codec.deserializeIdentifier(arg);
    }

    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(final InstanceIdentifier.PathArgument arg) {
        return arg instanceof IdentifiableItem
            ? codec.bindingToDom((IdentifiableItem<?, ?>) arg) : super.serializePathArgument(arg);
    }

    @Override
    public InstanceIdentifier.PathArgument deserializePathArgument(final YangInstanceIdentifier.PathArgument arg) {
        return arg instanceof NodeIdentifierWithPredicates
            ? codec.domToBinding((NodeIdentifierWithPredicates) arg) : super.deserializePathArgument(arg);
    }
}
