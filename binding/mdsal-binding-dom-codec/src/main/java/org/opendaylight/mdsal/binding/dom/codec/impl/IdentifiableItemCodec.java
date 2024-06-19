/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.concepts.AbstractIllegalArgumentCodec;
import org.opendaylight.yangtools.util.ImmutableOffsetMap;
import org.opendaylight.yangtools.util.ImmutableOffsetMapTemplate;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Codec support for extracting the {@link Identifiable#key()} method return from a MapEntryNode.
 */
abstract class IdentifiableItemCodec
        extends AbstractIllegalArgumentCodec<NodeIdentifierWithPredicates, IdentifiableItem<?, ?>> {
    private static final class SingleKey extends IdentifiableItemCodec {
        private static final MethodType CTOR_TYPE = MethodType.methodType(Identifier.class, Object.class);

        private final ValueContext keyContext;
        private final MethodHandle ctor;
        private final QName keyName;

        SingleKey(final ListSchemaNode schema, final Class<? extends Identifier<?>> keyClass,
                final Class<?> identifiable, final QName keyName, final ValueContext keyContext) {
            super(schema, keyClass, identifiable);
            this.keyContext = requireNonNull(keyContext);
            this.keyName = requireNonNull(keyName);
            ctor = getConstructor(keyClass, 1).asType(CTOR_TYPE);
        }

        @Override
        Identifier<?> deserializeIdentifierImpl(final NodeIdentifierWithPredicates nip) throws Throwable {
            return (Identifier<?>) ctor.invokeExact(keyContext.deserialize(nip.getValue(keyName)));
        }

        @Override
        NodeIdentifierWithPredicates serializeIdentifier(final QName qname, final Identifier<?> key) {
            return NodeIdentifierWithPredicates.of(qname, keyName, keyContext.getAndSerialize(key));
        }
    }

    private static final class MultiKey extends IdentifiableItemCodec {
        private final ImmutableOffsetMapTemplate<QName> predicateTemplate;
        private final ImmutableOffsetMap<QName, ValueContext> keyValueContexts;
        private final ImmutableList<QName> keysInBindingOrder;
        private final MethodHandle ctor;

        MultiKey(final ListSchemaNode schema, final Class<? extends Identifier<?>> keyClass,
                final Class<?> identifiable, final Map<QName, ValueContext> keyValueContexts) {
            super(schema, keyClass, identifiable);

            final MethodHandle tmpCtor = getConstructor(keyClass, keyValueContexts.size());
            final MethodHandle inv = MethodHandles.spreadInvoker(tmpCtor.type(), 0);
            this.ctor = inv.asType(inv.type().changeReturnType(Identifier.class)).bindTo(tmpCtor);

            /*
             * We need to re-index to make sure we instantiate nodes in the order in which they are defined. We will
             * also need to instantiate values in the same order.
             */
            final List<QName> keyDef = schema.getKeyDefinition();
            predicateTemplate = ImmutableOffsetMapTemplate.ordered(keyDef);
            this.keyValueContexts = predicateTemplate.instantiateTransformed(keyValueContexts, (key, value) -> value);

            /*
             * When instantiating binding objects we need to specify constructor arguments in alphabetic order. If the
             * order matches definition order, we try to reuse the key definition.
             *
             * BUG-2755: remove this if order is made declaration-order-dependent
             */
            final List<QName> tmp = new ArrayList<>(keyDef);
            // This is not terribly efficient but gets the job done
            tmp.sort(Comparator.comparing(qname -> BindingMapping.getPropertyName(qname.getLocalName())));
            this.keysInBindingOrder = ImmutableList.copyOf(tmp.equals(keyDef) ? keyDef : tmp);
        }

        @Override
        Identifier<?> deserializeIdentifierImpl(final NodeIdentifierWithPredicates nip) throws Throwable {
            final Object[] bindingValues = new Object[keysInBindingOrder.size()];
            int offset = 0;
            for (final QName key : keysInBindingOrder) {
                bindingValues[offset++] = keyValueContexts.get(key).deserialize(nip.getValue(key));
            }

            return (Identifier<?>) ctor.invokeExact(bindingValues);
        }

        @Override
        NodeIdentifierWithPredicates serializeIdentifier(final QName qname, final Identifier<?> key) {
            final Object[] values = new Object[keyValueContexts.size()];
            int offset = 0;
            for (final ValueContext valueCtx : keyValueContexts.values()) {
                values[offset++] = valueCtx.getAndSerialize(key);
            }

            return NodeIdentifierWithPredicates.of(qname, predicateTemplate.instantiateWithValues(values));
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(IdentifiableItemCodec.class);

    private final Class<?> identifiable;
    private final QName qname;

    IdentifiableItemCodec(final ListSchemaNode schema, final Class<? extends Identifier<?>> keyClass,
            final Class<?> identifiable) {
        this.identifiable = requireNonNull(identifiable);
        this.qname = schema.getQName();
    }

    static IdentifiableItemCodec of(final ListSchemaNode schema,
            final Class<? extends Identifier<?>> keyClass, final Class<?> identifiable,
                    final Map<QName, ValueContext> keyValueContexts) {
        switch (keyValueContexts.size()) {
            case 0:
                throw new IllegalArgumentException("Key " + keyClass + " of " + identifiable + " has no components");
            case 1:
                final Entry<QName, ValueContext> entry = keyValueContexts.entrySet().iterator().next();
                return new SingleKey(schema, keyClass, identifiable, entry.getKey(), entry.getValue());
            default:
                return new MultiKey(schema, keyClass, identifiable, keyValueContexts);
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected final IdentifiableItem<?, ?> deserializeImpl(final NodeIdentifierWithPredicates input) {
        final Identifier<?> identifier = deserializeIdentifier(input);
        return IdentifiableItem.of((Class) identifiable, (Identifier) identifier);
    }

    @Override
    protected final NodeIdentifierWithPredicates serializeImpl(final IdentifiableItem<?, ?> input) {
        return serializeIdentifier(qname, input.getKey());
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    final @NonNull Identifier<?> deserializeIdentifier(final NodeIdentifierWithPredicates input) {
        try {
            return deserializeIdentifierImpl(input);
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException("Failed to deserialize " + input, e);
        }
    }

    @SuppressWarnings("checkstyle:illegalThrows")
    abstract @NonNull Identifier<?> deserializeIdentifierImpl(@NonNull NodeIdentifierWithPredicates nip)
            throws Throwable;

    abstract @NonNull NodeIdentifierWithPredicates serializeIdentifier(QName qname, Identifier<?> key);

    static MethodHandle getConstructor(final Class<? extends Identifier<?>> clazz, final int nrArgs) {
        for (final Constructor<?> ctor : clazz.getConstructors()) {
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
