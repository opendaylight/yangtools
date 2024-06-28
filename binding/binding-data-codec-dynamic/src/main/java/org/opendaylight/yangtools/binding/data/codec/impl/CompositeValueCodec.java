/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.BindingInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

abstract sealed class CompositeValueCodec extends AbstractValueCodec<Object, Object> {
    static final class OfIdentity extends CompositeValueCodec {
        private final IdentityCodec valueCodec;

        OfIdentity(final Class<?> valueType, final IdentityCodec codec) {
            super(valueType);
            valueCodec = requireNonNull(codec);
        }

        @Override
        QName bindingToDom(final Object bindingValue) {
            return switch (bindingValue) {
                case BaseIdentity identity -> valueCodec.fromBinding(identity);
                default -> throw new IllegalArgumentException("Unexpected Binding value " + bindingValue);
            };
        }

        @Override
        BaseIdentity domToBinding(final Object domValue) {
            return switch (domValue) {
                case QName qname -> valueCodec.toBinding(qname);
                default -> throw new IllegalArgumentException("Unexpected DOM value " + domValue);
            };
        }
    }

    static final class OfInstanceIdentifier extends CompositeValueCodec {
        private final InstanceIdentifierCodec valueCodec;

        OfInstanceIdentifier(final Class<?> valueType, final InstanceIdentifierCodec codec) {
            super(valueType);
            valueCodec = requireNonNull(codec);
        }

        @Override
        BindingInstanceIdentifier domToBinding(final Object domValue) {
            return switch (domValue) {
                case YangInstanceIdentifier yiid -> valueCodec.deserialize(yiid);
                default -> throw new IllegalArgumentException("Unexpected DOM value " + domValue);
            };
        }

        @Override
        YangInstanceIdentifier bindingToDom(final Object bindingValue) {
            return switch (bindingValue) {
                case BindingInstanceIdentifier bid -> valueCodec.fromBinding(bid);
                default -> throw new IllegalArgumentException("Unexpected Binding value " + bindingValue);
            };
        }
    }

    private final EncapsulatedValueCodec typeObjectCodec;

    private CompositeValueCodec(final Class<?> valueType) {
        typeObjectCodec = EncapsulatedValueCodec.ofUnchecked(valueType);
    }

    @Override
    protected Object deserializeImpl(final Object input) {
        return typeObjectCodec.deserialize(domToBinding(input));
    }

    abstract @NonNull Object domToBinding(@NonNull Object domValue);

    @Override
    protected Object serializeImpl(final Object input) {
        return bindingToDom(typeObjectCodec.serialize(input));
    }

    abstract @NonNull Object bindingToDom(@NonNull Object bindingValue);
}
