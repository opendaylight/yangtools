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
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.AugmentableRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingObject;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is an implementation detail. It is public only due to technical reasons and may change at any time.
 */
@Beta
public abstract sealed class DataObjectCodecContext<D extends DataObject, T extends CompositeRuntimeType>
        extends AbstractDataObjectCodecContext<D, T> implements BindingDataObjectCodecTreeNode<D>
        permits CaseCodecContext, ContainerLikeCodecContext, ListCodecContext, NotificationCodecContext {
    private static final Logger LOG = LoggerFactory.getLogger(DataObjectCodecContext.class);

    private static final MethodType CONSTRUCTOR_TYPE = MethodType.methodType(void.class,
        AbstractDataObjectCodecContext.class, DataContainerNode.class);
    private static final MethodType DATAOBJECT_TYPE = MethodType.methodType(DataObject.class,
        DataObjectCodecContext.class, DataContainerNode.class);
    private static final VarHandle MISMATCHED_AUGMENTED;

    static {
        try {
            MISMATCHED_AUGMENTED = MethodHandles.lookup().findVarHandle(DataObjectCodecContext.class,
                "mismatchedAugmented", ImmutableMap.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final ImmutableMap<Class<?>, AugmentationCodecPrototype> augmentToPrototype;
    private final ImmutableMap<NodeIdentifier, Class<?>> yangToAugmentClass;
    private final @NonNull Class<? extends CodecDataObject<?>> generatedClass;
    private final MethodHandle proxyConstructor;

    // Note this the content of this field depends only of invariants expressed as this class's fields or
    // BindingRuntimeContext. It is only accessed via MISMATCHED_AUGMENTED above.
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile ImmutableMap<Class<?>, CommonDataObjectCodecPrototype<?>> mismatchedAugmented = ImmutableMap.of();

    DataObjectCodecContext(final CommonDataObjectCodecPrototype<T> prototype) {
        this(prototype, CodecItemFactory.of());
    }

    DataObjectCodecContext(final CommonDataObjectCodecPrototype<T> prototype, final CodecItemFactory itemFactory) {
        this(prototype, new DataContainerAnalysis<>(prototype, itemFactory), null);
    }

    DataObjectCodecContext(final CommonDataObjectCodecPrototype<T> prototype, final Method keyMethod) {
        this(prototype, new DataContainerAnalysis<>(prototype, CodecItemFactory.of()), keyMethod);
    }

    private DataObjectCodecContext(final CommonDataObjectCodecPrototype<T> prototype,
            final DataContainerAnalysis<T> analysis, final Method keyMethod) {
        super(prototype, analysis);

        final var bindingClass = getBindingClass();

        // Final bits: generate the appropriate class, As a side effect we identify what Augmentations are possible
        final List<AugmentRuntimeType> possibleAugmentations;
        final var loader = prototype().contextFactory().getLoader();
        if (Augmentable.class.isAssignableFrom(bindingClass)) {
            // Verify we have the appropriate backing runtimeType
            final var runtimeType = prototype.runtimeType();
            if (!(runtimeType instanceof AugmentableRuntimeType augmentableRuntimeType)) {
                throw new VerifyException(
                    "Unexpected type %s backing augmenable %s".formatted(runtimeType, bindingClass));
            }

            possibleAugmentations = augmentableRuntimeType.augments();
            generatedClass = CodecDataObjectGenerator.generateAugmentable(loader, bindingClass, analysis.leafContexts,
                analysis.daoProperties, keyMethod);
        } else {
            possibleAugmentations = List.of();
            generatedClass = CodecDataObjectGenerator.generate(loader, bindingClass, analysis.leafContexts,
                analysis.daoProperties, keyMethod);
        }

        // All done: acquire the constructor: it is supposed to be public
        final MethodHandle ctor;
        try {
            ctor = MethodHandles.publicLookup().findConstructor(generatedClass, CONSTRUCTOR_TYPE);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new LinkageError("Failed to find contructor for class " + generatedClass, e);
        }

        proxyConstructor = ctor.asType(DATAOBJECT_TYPE);

        // Deal with augmentations, which are not something we analysis provides
        final var augPathToBinding = new HashMap<NodeIdentifier, Class<?>>();
        final var augClassToProto = new HashMap<Class<?>, AugmentationCodecPrototype>();
        for (var augment : possibleAugmentations) {
            final var augProto = loadAugmentPrototype(augment);
            if (augProto != null) {
                final var augBindingClass = augProto.javaClass();
                for (var childPath : augProto.getChildArgs()) {
                    augPathToBinding.putIfAbsent(childPath, augBindingClass);
                }
                augClassToProto.putIfAbsent(augBindingClass, augProto);
            }
        }
        yangToAugmentClass = ImmutableMap.copyOf(augPathToBinding);
        augmentToPrototype = ImmutableMap.copyOf(augClassToProto);
    }

    @Override
    final DataContainerPrototype<?, ?> pathChildPrototype(final Class<? extends DataObject> argType) {
        final var child = super.pathChildPrototype(argType);
        return child != null ? child : augmentToPrototype.get(argType);
    }

    @Override
    final DataContainerPrototype<?, ?> streamChildPrototype(final Class<?> childClass) {
        final var child = super.streamChildPrototype(childClass);
        if (child == null && Augmentation.class.isAssignableFrom(childClass)) {
            return getAugmentationProtoByClass(childClass);
        }
        return child;
    }

    @Override
    final CodecContextSupplier yangChildSupplier(final NodeIdentifier arg) {
        final var child = super.yangChildSupplier(arg);
        if (child == null) {
            final var augClass = yangToAugmentClass.get(arg);
            if (augClass != null) {
                return augmentToPrototype.get(augClass);
            }
        }
        return child;
    }

    private @Nullable AugmentationCodecPrototype getAugmentationProtoByClass(final @NonNull Class<?> augmClass) {
        final var childProto = augmentToPrototype.get(augmClass);
        return childProto != null ? childProto : mismatchedAugmentationByClass(augmClass);
    }

    private @Nullable AugmentationCodecPrototype mismatchedAugmentationByClass(final @NonNull Class<?> childClass) {
        /*
         * It is potentially mismatched valid augmentation - we look up equivalent augmentation using reflection
         * and walk all stream child and compare augmentations classes if they are equivalent. When we find a match
         * we'll cache it so we do not need to perform reflection operations again.
         */
        final var local = (ImmutableMap<Class<?>, AugmentationCodecPrototype>) MISMATCHED_AUGMENTED.getAcquire(this);
        final var mismatched = local.get(childClass);
        return mismatched != null ? mismatched : loadMismatchedAugmentation(local, childClass);
    }

    private @Nullable AugmentationCodecPrototype loadMismatchedAugmentation(
            final ImmutableMap<Class<?>, AugmentationCodecPrototype> oldMismatched,
            final @NonNull Class<?> childClass) {
        @SuppressWarnings("rawtypes")
        final Class<?> augTarget = findAugmentationTarget((Class) childClass);
        // Do not bother with proposals which are not augmentations of our class, or do not match what the runtime
        // context would load.
        if (getBindingClass().equals(augTarget) && belongsToRuntimeContext(childClass)) {
            for (var realChild : augmentToPrototype.values()) {
                final var realClass = realChild.javaClass();
                if (Augmentation.class.isAssignableFrom(realClass) && isSubstitutionFor(childClass, realClass)) {
                    return cacheMismatched(oldMismatched, childClass, realChild);
                }
            }
        }
        LOG.trace("Failed to resolve {} as a valid augmentation in {}", childClass, this);
        return null;
    }

    private @NonNull AugmentationCodecPrototype cacheMismatched(
            final @NonNull ImmutableMap<Class<?>, AugmentationCodecPrototype> oldMismatched,
            final @NonNull Class<?> childClass, final @NonNull AugmentationCodecPrototype prototype) {
        var expected = oldMismatched;
        while (true) {
            final var newMismatched =
                ImmutableMap.<Class<?>, CommonDataObjectCodecPrototype<?>>builderWithExpectedSize(expected.size() + 1)
                    .putAll(expected)
                    .put(childClass, prototype)
                    .build();

            final var witness = (ImmutableMap<Class<?>, AugmentationCodecPrototype>)
                MISMATCHED_AUGMENTED.compareAndExchangeRelease(this, expected, newMismatched);
            if (witness == expected) {
                LOG.trace("Cached mismatched augmentation {} -> {} in {}", childClass, prototype, this);
                return prototype;
            }

            expected = witness;
            final var existing = expected.get(childClass);
            if (existing != null) {
                LOG.trace("Using raced mismatched augmentation {} -> {} in {}", childClass, existing, this);
                return existing;
            }
        }
    }

    private boolean belongsToRuntimeContext(final Class<?> cls) {
        final var ctx = prototype().contextFactory().getRuntimeContext();
        final Class<?> loaded;
        try {
            loaded = ctx.loadClass(Type.of(cls));
        } catch (ClassNotFoundException e) {
            LOG.debug("Proposed {} cannot be loaded in {}", cls, ctx, e);
            return false;
        }
        return cls.equals(loaded);
    }

    private @Nullable AugmentationCodecPrototype loadAugmentPrototype(final AugmentRuntimeType augment) {
        // FIXME: in face of deviations this code should be looking at declared view, i.e. all possibilities at augment
        //        declaration site
        final var childPaths = augment.statement()
            .streamEffectiveSubstatements(SchemaTreeEffectiveStatement.class)
            .map(stmt -> new NodeIdentifier((QName) stmt.argument()))
            .collect(ImmutableSet.toImmutableSet());

        if (childPaths.isEmpty()) {
            return null;
        }

        final var factory = prototype().contextFactory();
        final GeneratedType javaType = augment.javaType();
        final Class<? extends Augmentation<?>> augClass;
        try {
            augClass = factory.getRuntimeContext().loadClass(javaType);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(
                "RuntimeContext references type " + javaType + " but failed to load its class", e);
        }
        return new AugmentationCodecPrototype(augClass, augment, factory, childPaths);
    }

    @Override
    @SuppressWarnings("unchecked")
    Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAllAugmentationsFrom(final DataContainerNode data) {
        /**
         * Due to augmentation fields are at same level as direct children the data of each augmentation needs to be
         * aggregated into own container node, then only deserialized using associated prototype.
         */
        final var builders = new HashMap<Class<?>, DataContainerNodeBuilder>();
        for (var childValue : data.body()) {
            final var bindingClass = yangToAugmentClass.get(childValue.name());
            if (bindingClass != null) {
                builders.computeIfAbsent(bindingClass,
                    key -> Builders.containerBuilder()
                        .withNodeIdentifier(new NodeIdentifier(data.name().getNodeType())))
                        .addChild(childValue);
            }
        }
        @SuppressWarnings("rawtypes")
        final var map = new HashMap();
        for (final var entry : builders.entrySet()) {
            final var bindingClass = entry.getKey();
            final var codecProto = augmentToPrototype.get(bindingClass);
            if (codecProto != null) {
                final var bindingObj = codecProto.getCodecContext().deserializeObject(entry.getValue().build());
                if (bindingObj != null) {
                    map.put(bindingClass, bindingObj);
                }
            }
        }
        return map;
    }

    @Override
    public InstanceIdentifier.PathArgument deserializePathArgument(final PathArgument arg) {
        checkArgument(getDomPathArgument().equals(arg));
        return bindingArg();
    }

    @Override
    public PathArgument serializePathArgument(final InstanceIdentifier.PathArgument arg) {
        checkArgument(bindingArg().equals(arg));
        return getDomPathArgument();
    }

    @Override
    public NormalizedNode serialize(final D data) {
        return serializeImpl(data);
    }

    @Override
    public final BindingNormalizedNodeCachingCodec<D> createCachingCodec(
            final ImmutableCollection<Class<? extends BindingObject>> cacheSpecifier) {
        return createCachingCodec(this, cacheSpecifier);
    }

    final @NonNull Class<? extends CodecDataObject<?>> generatedClass() {
        return generatedClass;
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    final @NonNull D createBindingProxy(final DataContainerNode node) {
        try {
            return (D) proxyConstructor.invokeExact(this, node);
        } catch (final Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException(e);
        }
    }
}
