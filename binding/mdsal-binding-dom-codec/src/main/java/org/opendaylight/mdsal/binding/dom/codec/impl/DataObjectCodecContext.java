/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.IncorrectNestingException;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.AugmentableRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is an implementation detail. It is public only due to technical reasons and may change at any time.
 */
@Beta
public abstract class DataObjectCodecContext<D extends DataObject, T extends CompositeRuntimeType>
        extends DataContainerCodecContext<D, T> {
    private static final Logger LOG = LoggerFactory.getLogger(DataObjectCodecContext.class);

    private static final VarHandle MISMATCHED_AUGMENTED;

    static {
        try {
            MISMATCHED_AUGMENTED = MethodHandles.lookup().findVarHandle(DataObjectCodecContext.class,
                "mismatchedAugmented", ImmutableMap.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final ImmutableMap<String, ValueNodeCodecContext> leafChild;
    private final ImmutableMap<YangInstanceIdentifier.PathArgument, NodeContextSupplier> byYang;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byStreamClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byBindingArgClass;
    private final ImmutableMap<YangInstanceIdentifier.PathArgument, DataContainerCodecPrototype<?>> augmentationByYang;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> augmentationByStream;
    private final @NonNull Class<? extends CodecDataObject<?>> generatedClass;
    private final MethodHandle proxyConstructor;

    // Note this the content of this field depends only of invariants expressed as this class's fields or
    // BindingRuntimeContext. It is only accessed via MISMATCHED_AUGMENTED above.
    @SuppressWarnings("unused")
    private volatile ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> mismatchedAugmented = ImmutableMap.of();

    DataObjectCodecContext(final DataContainerCodecPrototype<T> prototype) {
        this(prototype, CodecItemFactory.of());
    }

    DataObjectCodecContext(final DataContainerCodecPrototype<T> prototype, final CodecItemFactory itemFactory) {
        this(prototype, new CodecDataObjectAnalysis<>(prototype, itemFactory, null));
    }

    DataObjectCodecContext(final DataContainerCodecPrototype<T> prototype, final Method keyMethod) {
        this(prototype, new CodecDataObjectAnalysis<>(prototype, CodecItemFactory.of(), keyMethod));
    }

    private DataObjectCodecContext(final DataContainerCodecPrototype<T> prototype,
            final CodecDataObjectAnalysis<T> analysis) {
        super(prototype);

        // Inherit analysis stuff
        leafChild = analysis.leafNodes;
        proxyConstructor = analysis.proxyConstructor;
        generatedClass = analysis.generatedClass;
        byBindingArgClass = analysis.byBindingArgClass;
        byStreamClass = analysis.byStreamClass;
        byYang = analysis.byYang;

        // Deal with augmentations, which are not something we analysis provides
        final var augByYang = new HashMap<PathArgument, DataContainerCodecPrototype<?>>();
        final var augByStream = new HashMap<Class<?>, DataContainerCodecPrototype<?>>();
        for (var augment : analysis.possibleAugmentations) {
            final var augProto = loadAugmentPrototype(augment);
            if (augProto != null) {
                final var augYangArg = augProto.getYangArg();
                if (augByYang.putIfAbsent(augYangArg, augProto) == null) {
                    LOG.trace("Discovered new YANG mapping {} -> {} in {}", augYangArg, augProto, this);
                }
                final var augBindingClass = augProto.getBindingClass();
                if (augByStream.putIfAbsent(augBindingClass, augProto) == null) {
                    LOG.trace("Discovered new class mapping {} -> {} in {}", augBindingClass, augProto, this);
                }
            }
        }
        augmentationByYang = ImmutableMap.copyOf(augByYang);
        augmentationByStream = ImmutableMap.copyOf(augByStream);
    }

    @Override
    public final WithStatus getSchema() {
        // FIXME: Bad cast, we should be returning an EffectiveStatement perhaps?
        return (WithStatus) getType().statement();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends DataObject> DataContainerCodecContext<C, ?> streamChild(final Class<C> childClass) {
        return (DataContainerCodecContext<C, ?>) childNonNull(streamChildPrototype(childClass), childClass,
            "Child %s is not valid child of %s", getBindingClass(), childClass).get();
    }

    private DataContainerCodecPrototype<?> streamChildPrototype(final Class<?> childClass) {
        final DataContainerCodecPrototype<?> childProto = byStreamClass.get(childClass);
        if (childProto != null) {
            return childProto;
        }
        if (Augmentation.class.isAssignableFrom(childClass)) {
            return augmentationByClass(childClass);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends DataObject> Optional<DataContainerCodecContext<C, ?>> possibleStreamChild(
            final Class<C> childClass) {
        final DataContainerCodecPrototype<?> childProto = streamChildPrototype(childClass);
        if (childProto != null) {
            return Optional.of((DataContainerCodecContext<C, ?>) childProto.get());
        }
        return Optional.empty();
    }

    @Override
    public DataContainerCodecContext<?,?> bindingPathArgumentChild(final InstanceIdentifier.PathArgument arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {

        final Class<? extends DataObject> argType = arg.getType();
        DataContainerCodecPrototype<?> ctxProto = byBindingArgClass.get(argType);
        if (ctxProto == null && Augmentation.class.isAssignableFrom(argType)) {
            ctxProto = augmentationByClass(argType);
        }
        final DataContainerCodecContext<?, ?> context = childNonNull(ctxProto, argType,
            "Class %s is not valid child of %s", argType, getBindingClass()).get();
        if (context instanceof ChoiceNodeCodecContext<?> choice) {
            choice.addYangPathArgument(arg, builder);

            final Optional<? extends Class<? extends DataObject>> caseType = arg.getCaseType();
            final Class<? extends DataObject> type = arg.getType();
            final DataContainerCodecContext<?, ?> caze;
            if (caseType.isPresent()) {
                // Non-ambiguous addressing this should not pose any problems
                caze = choice.streamChild(caseType.orElseThrow());
            } else {
                caze = choice.getCaseByChildClass(type);
            }

            caze.addYangPathArgument(arg, builder);
            return caze.bindingPathArgumentChild(arg, builder);
        }
        context.addYangPathArgument(arg, builder);
        return context;
    }

    @Override
    public NodeCodecContext yangPathArgumentChild(final YangInstanceIdentifier.PathArgument arg) {
        final NodeContextSupplier childSupplier;
        if (arg instanceof NodeIdentifierWithPredicates) {
            childSupplier = byYang.get(new NodeIdentifier(arg.getNodeType()));
        } else if (arg instanceof AugmentationIdentifier) {
            childSupplier = augmentationByYang.get(arg);
        } else {
            childSupplier = byYang.get(arg);
        }

        return childNonNull(childSupplier, arg, "Argument %s is not valid child of %s", arg, getSchema()).get();
    }

    protected final ValueNodeCodecContext getLeafChild(final String name) {
        final ValueNodeCodecContext value = leafChild.get(name);
        if (value == null) {
            throw new IncorrectNestingException("Leaf %s is not valid for %s", name, getBindingClass());
        }
        return value;
    }

    private @Nullable DataContainerCodecPrototype<?> augmentationByClass(final @NonNull Class<?> childClass) {
        final DataContainerCodecPrototype<?> childProto = augmentationByStream.get(childClass);
        return childProto != null ? childProto : mismatchedAugmentationByClass(childClass);
    }

    private @Nullable DataContainerCodecPrototype<?> mismatchedAugmentationByClass(final @NonNull Class<?> childClass) {
        /*
         * It is potentially mismatched valid augmentation - we look up equivalent augmentation using reflection
         * and walk all stream child and compare augmentations classes if they are equivalent. When we find a match
         * we'll cache it so we do not need to perform reflection operations again.
         */
        final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> local =
                (ImmutableMap<Class<?>, DataContainerCodecPrototype<?>>) MISMATCHED_AUGMENTED.getAcquire(this);
        final DataContainerCodecPrototype<?> mismatched = local.get(childClass);
        return mismatched != null ? mismatched : loadMismatchedAugmentation(local, childClass);

    }

    private @Nullable DataContainerCodecPrototype<?> loadMismatchedAugmentation(
            final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> oldMismatched,
            final @NonNull Class<?> childClass) {
        @SuppressWarnings("rawtypes")
        final Class<?> augTarget = BindingReflections.findAugmentationTarget((Class) childClass);
        // Do not bother with proposals which are not augmentations of our class, or do not match what the runtime
        // context would load.
        if (getBindingClass().equals(augTarget) && belongsToRuntimeContext(childClass)) {
            for (final DataContainerCodecPrototype<?> realChild : augmentationByStream.values()) {
                if (Augmentation.class.isAssignableFrom(realChild.getBindingClass())
                        && isSubstitutionFor(childClass, realChild.getBindingClass())) {
                    return cacheMismatched(oldMismatched, childClass, realChild);
                }
            }
        }
        LOG.trace("Failed to resolve {} as a valid augmentation in {}", childClass, this);
        return null;
    }

    private @NonNull DataContainerCodecPrototype<?> cacheMismatched(
            final @NonNull ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> oldMismatched,
            final @NonNull Class<?> childClass, final @NonNull DataContainerCodecPrototype<?> prototype) {

        ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> expected = oldMismatched;
        while (true) {
            final Map<Class<?>, DataContainerCodecPrototype<?>> newMismatched =
                    ImmutableMap.<Class<?>, DataContainerCodecPrototype<?>>builderWithExpectedSize(expected.size() + 1)
                        .putAll(expected)
                        .put(childClass, prototype)
                        .build();

            final var witness = (ImmutableMap<Class<?>, DataContainerCodecPrototype<?>>)
                MISMATCHED_AUGMENTED.compareAndExchangeRelease(this, expected, newMismatched);
            if (witness == expected) {
                LOG.trace("Cached mismatched augmentation {} -> {} in {}", childClass, prototype, this);
                return prototype;
            }

            expected = witness;
            final DataContainerCodecPrototype<?> existing = expected.get(childClass);
            if (existing != null) {
                LOG.trace("Using raced mismatched augmentation {} -> {} in {}", childClass, existing, this);
                return existing;
            }
        }
    }

    private boolean belongsToRuntimeContext(final Class<?> cls) {
        final BindingRuntimeContext ctx = factory().getRuntimeContext();
        final Class<?> loaded;
        try {
            loaded = ctx.loadClass(Type.of(cls));
        } catch (ClassNotFoundException e) {
            LOG.debug("Proposed {} cannot be loaded in {}", cls, ctx, e);
            return false;
        }
        return cls.equals(loaded);
    }

    private @Nullable DataContainerCodecPrototype<?> loadAugmentPrototype(final AugmentRuntimeType augment) {
        // FIXME: in face of deviations this code should be looking at declared view, i.e. all possibilities at augment
        //        declaration site
        final var possibleChildren = augment.statement()
            .streamEffectiveSubstatements(SchemaTreeEffectiveStatement.class)
            .map(stmt -> (QName) stmt.argument())
            .collect(ImmutableSet.toImmutableSet());
        if (possibleChildren.isEmpty()) {
            return null;
        }

        final var factory = factory();
        final GeneratedType javaType = augment.javaType();
        final Class<? extends Augmentation<?>> augClass;
        try {
            augClass = factory.getRuntimeContext().loadClass(javaType);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(
                "RuntimeContext references type " + javaType + " but failed to load its class", e);
        }

        return DataContainerCodecPrototype.from(augClass, new AugmentationIdentifier(possibleChildren), augment,
            factory);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    protected final @NonNull D createBindingProxy(final DistinctNodeContainer<?, ?> node) {
        try {
            return (D) proxyConstructor.invokeExact(this, node);
        } catch (final Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAllAugmentationsFrom(
            final DistinctNodeContainer<PathArgument, NormalizedNode> data) {

        @SuppressWarnings("rawtypes")
        final Map map = new HashMap<>();

        for (final NormalizedNode childValue : data.body()) {
            if (childValue instanceof AugmentationNode augDomNode) {
                final DataContainerCodecPrototype<?> codecProto = augmentationByYang.get(augDomNode.getIdentifier());
                if (codecProto != null) {
                    final DataContainerCodecContext<?, ?> codec = codecProto.get();
                    map.put(codec.getBindingClass(), codec.deserializeObject(augDomNode));
                }
            }
        }
        for (final DataContainerCodecPrototype<?> value : augmentationByStream.values()) {
            final var augClass = value.getBindingClass();
            // Do not perform duplicate deserialization if we have already created the corresponding augmentation
            // and validate whether the proposed augmentation is valid ion this instantiation context.
            if (!map.containsKey(augClass)
                && ((AugmentableRuntimeType) getType()).augments().contains(value.getType())) {
                final NormalizedNode augData = data.childByArg(value.getYangArg());
                if (augData != null) {
                    // ... make sure we do not replace an e
                    map.putIfAbsent(augClass, value.get().deserializeObject(augData));
                }
            }
        }
        return map;
    }

    final @NonNull Class<? extends CodecDataObject<?>> generatedClass() {
        return generatedClass;
    }

    @Override
    public InstanceIdentifier.PathArgument deserializePathArgument(final YangInstanceIdentifier.PathArgument arg) {
        checkArgument(getDomPathArgument().equals(arg));
        return bindingArg();
    }

    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(final InstanceIdentifier.PathArgument arg) {
        checkArgument(bindingArg().equals(arg));
        return getDomPathArgument();
    }

}
