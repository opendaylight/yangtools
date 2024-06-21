/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

// FIXME: sealed once we have JDK17+
abstract class CompositeValueCodec extends AbstractValueCodec<Object, Object> {
    static final class OfIdentity extends CompositeValueCodec {
        private final IdentityCodec valueCodec;

        OfIdentity(final Class<?> valueType, final IdentityCodec codec) {
            super(valueType);
            valueCodec = requireNonNull(codec);
        }

        @Override
        Object bindingToDom(final Object bindingValue) {
            checkArgument(bindingValue instanceof BaseIdentity, "Unexpected Binding value %s", bindingValue);
            return valueCodec.fromBinding((BaseIdentity) bindingValue);
        }

        @Override
        Object domToBinding(final Object domValue) {
            checkArgument(domValue instanceof QName, "Unexpected DOM value %s", domValue);
            return valueCodec.toBinding((QName) domValue);
        }
    }

    static final class OfInstanceIdentifier extends CompositeValueCodec {
        private final InstanceIdentifierCodec valueCodec;

        OfInstanceIdentifier(final Class<?> valueType, final InstanceIdentifierCodec codec) {
            super(valueType);
            valueCodec = requireNonNull(codec);
        }

        @Override
        Object domToBinding(final Object domValue) {
            checkArgument(domValue instanceof YangInstanceIdentifier, "Unexpected DOM value %s", domValue);
            final var binding = valueCodec.toBinding((YangInstanceIdentifier) domValue);
            checkArgument(binding != null, "Cannot represent %s in binding", domValue);
            return binding;
        }

        @Override
        Object bindingToDom(final Object bindingValue) {
            checkArgument(bindingValue instanceof InstanceIdentifier, "Unexpected Binding value %s", bindingValue);
            return valueCodec.fromBinding((InstanceIdentifier<?>) bindingValue);
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
