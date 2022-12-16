/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.BindingObject;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.AugmentableRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is an implementation detail. It is public only due to technical reasons and may change at any time.
 */
@Beta
public abstract sealed class DataObjectCodecContext<D extends DataObject, T extends CompositeRuntimeType>
        extends CommonDataObjectCodecContext<D, T> implements BindingDataObjectCodecTreeNode<D>
        permits CaseCodecContext, ContainerLikeCodecContext, ListCodecContext, NotificationCodecContext {
    private static final Logger LOG = LoggerFactory.getLogger(DataObjectCodecContext.class);

    private static final MethodType CONSTRUCTOR_TYPE = MethodType.methodType(void.class,
        CommonDataObjectCodecContext.class, DataContainerNode.class);
    private static final MethodType DATAOBJECT_TYPE = MethodType.methodType(DataObject.class,
        DataObjectCodecContext.class, DataContainerNode.class);

    private final ImmutableMap<Class<?>, AugmentationCodecPrototype<?>> augmentToPrototype;
    private final ImmutableMap<NodeIdentifier, Class<?>> yangToAugmentClass;
    private final @NonNull Class<? extends CodecDataObject<?>> generatedClass;
    private final MethodHandle proxyConstructor;
    private final ImmutableMap<Class<?>, AugmentationCodecPrototype<?>> mismatchedAugmented;

    DataObjectCodecContext(final CommonDataObjectCodecPrototype<T> prototype) {
        this(prototype, new DataContainerAnalysis<>(prototype), null);
    }

    DataObjectCodecContext(final CommonDataObjectCodecPrototype<T> prototype,
            final Class<? extends DataObject> caseClass) {
        this(prototype, new DataContainerAnalysis<>(prototype, caseClass), null);
    }

    DataObjectCodecContext(final CommonDataObjectCodecPrototype<T> prototype, final Method keyMethod) {
        this(prototype, new DataContainerAnalysis<>(prototype), keyMethod);
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
        final var augClassToProto = new HashMap<Class<?>, AugmentationCodecPrototype<?>>();
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

        // Calculate possible mismatched augmentations.
        final var substitutions = new HashMap<Class<?>, AugmentationCodecPrototype<?>>();
        if (!augmentToPrototype.isEmpty()) {
            final var context = prototype().contextFactory().runtimeContext();
            // get the precalculated possible substitutions for each augmentation
            for (var entry : augmentToPrototype.entrySet()) {
                final var subsForAug = context.getTypes().getSubstitutionsForAugment(entry.getValue().runtimeType());
                for (var sub : subsForAug) {
                    try {
                        substitutions.put(context.loadClass(sub.javaType()), entry.getValue());
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException(
                            "RuntimeContext references type " + sub.javaType() + " but failed to load its class", e);
                    }
                }
            }
        }
        mismatchedAugmented = ImmutableMap.copyOf(substitutions);
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

    private @Nullable AugmentationCodecPrototype<?> getAugmentationProtoByClass(final @NonNull Class<?> augmClass) {
        final var childProto = augmentToPrototype.get(augmClass);
        return childProto != null ? childProto : mismatchedAugmentationByClass(augmClass);
    }

    private @Nullable AugmentationCodecPrototype<?> mismatchedAugmentationByClass(final @NonNull Class<?> childClass) {
        /*
         * It is potentially mismatched valid augmentation - we look up equivalent augmentation using precalculated
         * mapping.
         */
        return mismatchedAugmented.get(childClass);
    }

    private @Nullable AugmentationCodecPrototype<?> loadAugmentPrototype(final AugmentRuntimeType augment) {
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
            augClass = factory.runtimeContext().loadClass(javaType);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(
                "RuntimeContext references type " + javaType + " but failed to load its class", e);
        }
        return new AugmentationCodecPrototype<>(augClass, augment, factory, childPaths);
    }

    @Override
    @SuppressWarnings("unchecked")
    Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAllAugmentationsFrom(final DataContainerNode data) {
        /**
         * Due to augmentation fields are at same level as direct children the data of each augmentation needs to be
         * aggregated into own container node, then only deserialized using associated prototype.
         */
        final var builders = new HashMap<Class<?>, DataContainerNodeBuilder<?, ?>>();
        for (var childValue : data.body()) {
            final var bindingClass = yangToAugmentClass.get(childValue.name());
            if (bindingClass != null) {
                builders.computeIfAbsent(bindingClass,
                    key -> ImmutableNodes.newContainerBuilder()
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
    public DataObjectStep<?> deserializePathArgument(final PathArgument arg) {
        checkArgument(getDomPathArgument().equals(arg));
        return bindingArg();
    }

    @Override
    public PathArgument serializePathArgument(final DataObjectStep<?> step) {
        checkArgument(bindingArg().equals(step));
        return getDomPathArgument();
    }

    @Override
    public final NormalizedNode serialize(final D data) {
        return serializeImpl(data);
    }

    @Override
    public final void writeTo(final NormalizedNodeStreamWriter writer, final D dataContainer)
            throws IOException {
        eventStreamSerializer().serialize(dataContainer, new BindingToNormalizedStreamWriter(this, writer));
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

    @Override
    Object deserializeObject(final NormalizedNode normalizedNode) {
        return deserialize(normalizedNode);
    }
}
