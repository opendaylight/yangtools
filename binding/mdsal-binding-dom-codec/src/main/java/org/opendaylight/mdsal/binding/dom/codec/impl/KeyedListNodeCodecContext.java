/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.IDENTIFIABLE_KEY_NAME;

import java.lang.reflect.Method;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class KeyedListNodeCodecContext<D extends DataObject & Identifiable<?>> extends ListNodeCodecContext<D> {
    private final IdentifiableItemCodec codec;

    private KeyedListNodeCodecContext(final DataContainerCodecPrototype<ListSchemaNode> prototype,
            final Method keyMethod, final IdentifiableItemCodec codec) {
        super(prototype, keyMethod);
        this.codec = requireNonNull(codec);
    }

    @SuppressWarnings("rawtypes")
    static KeyedListNodeCodecContext create(final DataContainerCodecPrototype<ListSchemaNode> prototype) {
        final Class<?> bindingClass = prototype.getBindingClass();
        final Method keyMethod;
        try {
            keyMethod = bindingClass.getMethod(IDENTIFIABLE_KEY_NAME);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Required method not available", e);
        }

        final IdentifiableItemCodec codec = prototype.getFactory().getPathArgumentCodec(bindingClass,
            prototype.getSchema());
        return new KeyedListNodeCodecContext<>(prototype, keyMethod, codec);
    }

    @Override
    protected void addYangPathArgument(final InstanceIdentifier.PathArgument arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        /*
         * DOM Instance Identifier for list is always represent by two entries one for map and one for children. This
         * is also true for wildcarded instance identifiers
         */
        if (builder == null) {
            return;
        }

        super.addYangPathArgument(arg, builder);
        if (arg instanceof IdentifiableItem) {
            builder.add(codec.serialize((IdentifiableItem<?, ?>) arg));
        } else {
            // Adding wildcarded
            super.addYangPathArgument(arg, builder);
        }
    }

    @Override
    protected InstanceIdentifier.PathArgument getBindingPathArgument(final YangInstanceIdentifier.PathArgument domArg) {
        if (domArg instanceof NodeIdentifierWithPredicates) {
            return codec.deserialize((NodeIdentifierWithPredicates) domArg);
        }
        return super.getBindingPathArgument(domArg);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    NodeIdentifierWithPredicates serialize(final Identifier<?> key) {
        return codec.serialize(IdentifiableItem.of((Class)getBindingClass(), (Identifier)key));
    }

    @NonNull Identifier<?> deserialize(final NodeIdentifierWithPredicates arg) {
        return codec.deserializeIdentifier(arg);
    }

    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(final InstanceIdentifier.PathArgument arg) {
        if (arg instanceof IdentifiableItem) {
            return codec.serialize((IdentifiableItem<?, ?>) arg);
        }
        return super.serializePathArgument(arg);
    }

    @Override
    public InstanceIdentifier.PathArgument deserializePathArgument(final YangInstanceIdentifier.PathArgument arg) {
        if (arg instanceof NodeIdentifierWithPredicates) {
            return codec.deserialize((NodeIdentifierWithPredicates) arg);
        }
        return super.deserializePathArgument(arg);
    }
}
