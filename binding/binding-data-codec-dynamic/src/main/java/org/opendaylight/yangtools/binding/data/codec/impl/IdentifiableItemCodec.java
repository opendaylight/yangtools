/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;
import org.opendaylight.yangtools.binding.KeyStep;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.util.ImmutableOffsetMap;
import org.opendaylight.yangtools.util.ImmutableOffsetMapTemplate;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Codec support for extracting the {@link KeyAware#key()} method return from a MapEntryNode.
 */
abstract sealed class IdentifiableItemCodec {
    private static final class SingleKey extends IdentifiableItemCodec {
        private static final MethodType CTOR_TYPE = MethodType.methodType(Key.class, Object.class);

        private final ValueContext keyContext;
        private final MethodHandle ctor;
        private final QName keyName;

        SingleKey(final ListEffectiveStatement schema, final Class<? extends Key<?>> keyClass,
                final Class<?> identifiable, final QName keyName, final ValueContext keyContext) {
            super(schema, keyClass, identifiable);
            this.keyContext = requireNonNull(keyContext);
            this.keyName = requireNonNull(keyName);
            ctor = getConstructor(keyClass, 1).asType(CTOR_TYPE);
        }

        @Override
        Key<?> deserializeIdentifierImpl(final NodeIdentifierWithPredicates nip) throws Throwable {
            return (Key<?>) ctor.invokeExact(keyContext.deserialize(nip.getValue(keyName)));
        }

        @Override
        NodeIdentifierWithPredicates serializeIdentifier(final QName qname, final Key<?> key) {
            return NodeIdentifierWithPredicates.of(qname, keyName, keyContext.getAndSerialize(key));
        }
    }

    private static final class MultiKey extends IdentifiableItemCodec {
        private final ImmutableOffsetMapTemplate<QName> predicateTemplate;
        private final ImmutableOffsetMap<QName, ValueContext> keyValueContexts;
        private final List<QName> keysInBindingOrder;
        private final MethodHandle ctor;

        MultiKey(final ListEffectiveStatement schema, final Class<? extends Key<?>> keyClass,
                final Class<?> identifiable, final Map<QName, ValueContext> keyValueContexts) {
            super(schema, keyClass, identifiable);

            final var tmpCtor = getConstructor(keyClass, keyValueContexts.size());
            final var inv = MethodHandles.spreadInvoker(tmpCtor.type(), 0);
            ctor = inv.asType(inv.type().changeReturnType(Key.class)).bindTo(tmpCtor);

            /*
             * We need to re-index to make sure we instantiate nodes in the order in which they are defined. We will
             * also need to instantiate values in the same order.
             */
            final var keyDef = schema.findFirstEffectiveSubstatementArgument(KeyEffectiveStatement.class).orElseThrow();
            final var keys = keyDef.asList();
            predicateTemplate = ImmutableOffsetMapTemplate.ordered(keys);
            this.keyValueContexts = predicateTemplate.instantiateTransformed(keyValueContexts, (key, value) -> value);

            /*
             * When instantiating binding objects we need to specify constructor arguments in alphabetic order. If the
             * order matches definition order, we try to reuse the key definition.
             *
             * BUG-2755: remove this if order is made declaration-order-dependent
             */
            final var tmp = keys.stream()
                // This is not terribly efficient but gets the job done
                .sorted(Comparator.comparing(leaf -> Naming.getPropertyName(leaf.getLocalName())))
                .collect(Collectors.toUnmodifiableList());
            keysInBindingOrder = tmp.equals(keys) ? keys : tmp;
        }

        @Override
        Key<?> deserializeIdentifierImpl(final NodeIdentifierWithPredicates nip) throws Throwable {
            final var bindingValues = new Object[keysInBindingOrder.size()];
            int offset = 0;
            for (var key : keysInBindingOrder) {
                bindingValues[offset++] = keyValueContexts.get(key).deserialize(nip.getValue(key));
            }

            return (Key<?>) ctor.invokeExact(bindingValues);
        }

        @Override
        NodeIdentifierWithPredicates serializeIdentifier(final QName qname, final Key<?> key) {
            final var values = new Object[keyValueContexts.size()];
            int offset = 0;
            for (var valueCtx : keyValueContexts.values()) {
                values[offset++] = valueCtx.getAndSerialize(key);
            }

            return NodeIdentifierWithPredicates.of(qname, predicateTemplate.instantiateWithValues(values));
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(IdentifiableItemCodec.class);

    private final @NonNull Class<?> identifiable;
    private final @NonNull QName qname;

    private IdentifiableItemCodec(final ListEffectiveStatement schema, final Class<? extends Key<?>> keyClass,
            final Class<?> identifiable) {
        this.identifiable = requireNonNull(identifiable);
        qname = schema.argument();
    }

    static IdentifiableItemCodec of(final ListEffectiveStatement schema, final Class<? extends Key<?>> keyClass,
            final Class<?> identifiable, final Map<QName, ValueContext> keyValueContexts) {
        return switch (keyValueContexts.size()) {
            case 0 -> throw new IllegalArgumentException(
                "Key " + keyClass + " of " + identifiable + " has no components");
            case 1 -> {
                final var entry = keyValueContexts.entrySet().iterator().next();
                yield new SingleKey(schema, keyClass, identifiable, entry.getKey(), entry.getValue());
            }
            default -> new MultiKey(schema, keyClass, identifiable, keyValueContexts);
        };
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    final @NonNull KeyStep<?, ?> domToBinding(final NodeIdentifierWithPredicates input) {
        return new KeyStep(identifiable, deserializeIdentifier(requireNonNull(input)));
    }

    final @NonNull NodeIdentifierWithPredicates bindingToDom(final KeyStep<?, ?> input) {
        return serializeIdentifier(qname, input.key());
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    final @NonNull Key<?> deserializeIdentifier(final @NonNull NodeIdentifierWithPredicates input) {
        try {
            return deserializeIdentifierImpl(input);
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException("Failed to deserialize " + input, e);
        }
    }

    @SuppressWarnings("checkstyle:illegalThrows")
    abstract @NonNull Key<?> deserializeIdentifierImpl(@NonNull NodeIdentifierWithPredicates nip) throws Throwable;

    abstract @NonNull NodeIdentifierWithPredicates serializeIdentifier(QName qname, Key<?> key);

    static MethodHandle getConstructor(final Class<? extends Key<?>> clazz, final int nrArgs) {
        for (var ctor : clazz.getConstructors()) {
            // Check argument count
            if (ctor.getParameterCount() != nrArgs) {
                LOG.debug("Skipping {} due to argument count mismatch", ctor);
                continue;
            }

            // Do not consider deprecated constructors
            if (isDeprecated(ctor)) {
                LOG.debug("Skipping deprecated constructor {}", ctor);
                continue;
            }

            // Do not consider copy constructors
            if (clazz.equals(ctor.getParameterTypes()[0])) {
                LOG.debug("Skipping copy constructor {}", ctor);
                continue;
            }

            try {
                return MethodHandles.publicLookup().unreflectConstructor(ctor);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot access constructor " + ctor + " in class " + clazz, e);
            }
        }
        throw new IllegalArgumentException("Supplied class " + clazz + " does not have required constructor.");
    }

    // This could be inlined, but then it throws off Eclipse analysis, which thinks the return is always non-null
    private static boolean isDeprecated(final Constructor<?> ctor) {
        return ctor.getAnnotation(Deprecated.class) != null;
    }
}
