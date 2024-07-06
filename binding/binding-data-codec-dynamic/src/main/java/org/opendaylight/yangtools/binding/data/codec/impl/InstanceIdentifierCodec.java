/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import org.opendaylight.yangtools.binding.BindingInstanceIdentifier;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.KeylessStep;
import org.opendaylight.yangtools.binding.LeafListPropertyStep;
import org.opendaylight.yangtools.binding.LeafPropertyStep;
import org.opendaylight.yangtools.binding.PropertyIdentifier;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataContainerCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingInstanceIdentifierCodec;
import org.opendaylight.yangtools.binding.data.codec.api.BindingOpaqueObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingTypeObjectCodecTreeNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class InstanceIdentifierCodec implements BindingInstanceIdentifierCodec,
        //FIXME: this is not really an IllegalArgumentCodec, as it can legally return null from deserialize()
        ValueCodec<YangInstanceIdentifier, BindingInstanceIdentifier> {
    private final BindingCodecContext context;

    InstanceIdentifierCodec(final BindingCodecContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    public <T extends DataObject> DataObjectReference<T> toBinding(final YangInstanceIdentifier domPath) {
        final var steps = ImmutableList.<DataObjectStep<?>>builder();
        final var codec = context.getCodecContextNode(domPath, steps);
        return codec == null ? null : toDataObjectReference(steps.build(), codec);
    }

    @Override
    public BindingInstanceIdentifier toBindingInstanceIdentifier(final YangInstanceIdentifier domPath) {
        final var steps = ImmutableList.<DataObjectStep<?>>builder();
        return switch (context.getCodecContext(domPath, steps)) {
            case BindingDataContainerCodecTreeNode<?> dataContainer -> {
                final var ref = toDataObjectReference(steps.build(), dataContainer);
                if (ref == null) {
                    yield null;
                }
                try {
                    yield ref.toIdentifier();
                } catch (UnsupportedOperationException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            case BindingOpaqueObjectCodecTreeNode<?> opaqueObject -> {
                // FIXME: implement this
                throw new UnsupportedOperationException();
            }
            case BindingTypeObjectCodecTreeNode<?> typeObject -> {
                // FIXME: implement this
                throw new UnsupportedOperationException();
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <T extends DataObject> DataObjectReference<T> toDataObjectReference(
            final ImmutableList<DataObjectStep<?>> steps, final BindingDataContainerCodecTreeNode<?> codec) {
        return codec instanceof ListCodecContext && steps.getLast() instanceof KeylessStep
            // We ended up in list, but without key, which means it represent list as a whole, which is not binding
            // representable.
            // FIXME: this should be perfectly normal: it's just not a Identifier -- just as it could be referencing
            //        inexact steps, but can it really?
            ? null
            : (DataObjectReference<T>) DataObjectReference.ofUnsafeSteps(steps);
    }

    @Override
    public YangInstanceIdentifier fromBinding(final DataObjectReference<?> bindingPath) {
        final var domArgs = new ArrayList<PathArgument>();
        context.getCodecContextNode(bindingPath, domArgs);
        return YangInstanceIdentifier.of(domArgs);
    }

    @Override
    public YangInstanceIdentifier fromBinding(final PropertyIdentifier<?, ?> bindingPath) {
        final var domArgs = new ArrayList<PathArgument>();
        // resolve DataContainer part of the path and remember the codec
        final var containerCodec = context.getCodecContextNode(bindingPath.container(), domArgs);

        final var property = bindingPath.property();
        final var childArg = containerCodec.prototype().bindIdentifier(property.yangIdentifier());
        // This has a side-effect of validating childArg
        final var codec = containerCodec.yangPathArgumentChild(childArg);

        // AnydataNode, AnyxmlNode, LeafNode and LeafSetNode are all addressed by NodeIdentifier
        domArgs.add(childArg);

        switch (property) {
            case LeafPropertyStep<?, ?> leaf -> {
                // validate only
                if (!(codec instanceof LeafNodeCodecContext) && !(codec instanceof AbstractOpaqueCodecContext)) {
                    throw new IllegalArgumentException(leaf + " does not match context " + codec);
                }
            }
            case LeafListPropertyStep<?, ?> leafList -> {
                // validate and add step into the individual LeafSetEntryNode
                if (codec instanceof LeafSetNodeCodecContext leafListCodec) {
                    domArgs.add(new NodeWithValue<>(childArg.getNodeType(),
                        leafListCodec.getValueCodec().deserialize(leafList.value())));
                } else {
                    throw new IllegalArgumentException(leafList + " does not match context " + codec);
                }
            }
        }

        return YangInstanceIdentifier.of(domArgs);
    }

    @Override
    @Deprecated
    public YangInstanceIdentifier serialize(final BindingInstanceIdentifier input) {
        return fromBinding(input);
    }

    @Override
    @Deprecated
    public BindingInstanceIdentifier deserialize(final YangInstanceIdentifier input) {
        // FIXME: YANGTOOLS-1577: do not defer to InstanceIdentifier here
        final var binding = toBinding(input);
        if (binding == null) {
            throw new IllegalArgumentException(input + " cannot be represented as a BindingInstanceIdentifier");
        }
        try {
            return binding.toIdentifier();
        } catch (UnsupportedOperationException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
