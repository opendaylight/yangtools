/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableMap;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.AugmentableRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ContainerLikeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ContainerRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ListRuntimeType;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.OpaqueObject;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;

/**
 * Analysis of a {@link DataObject} specialization class. The primary point of this class is to separate out creation
 * indices needed for {@link #proxyConstructor}. Since we want to perform as much indexing as possible in a single pass,
 * we also end up indexing things that are not strictly required to arrive at that constructor.
 */
final class CodecDataObjectAnalysis<R extends CompositeRuntimeType> {
    private static final MethodType CONSTRUCTOR_TYPE = MethodType.methodType(void.class,
        AbstractDataObjectCodecContext.class, DataContainerNode.class);
    private static final MethodType DATAOBJECT_TYPE = MethodType.methodType(DataObject.class,
        AbstractDataObjectCodecContext.class, DataContainerNode.class);

    final @NonNull ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byStreamClass;
    final @NonNull ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byBindingArgClass;
    final @NonNull ImmutableMap<NodeIdentifier, CodecContextSupplier> byYang;
    final @NonNull ImmutableMap<String, ValueNodeCodecContext> leafNodes;
    final @NonNull Class<? extends CodecDataObject<?>> generatedClass;
    final @NonNull List<AugmentRuntimeType> possibleAugmentations;
    final @NonNull MethodHandle proxyConstructor;

    CodecDataObjectAnalysis(final DataContainerCodecPrototype<R> prototype, final CodecItemFactory itemFactory,
            final Method keyMethod) {
        // Preliminaries from prototype
        @SuppressWarnings("unchecked")
        final Class<DataObject> bindingClass = Class.class.cast(prototype.getBindingClass());
        final var runtimeType = prototype.getType();
        final var factory = prototype.getFactory();
        final var leafContexts = factory.getLeafNodes(bindingClass, runtimeType.statement());

        // Reflection-based on the passed class
        final var clsToMethod = getChildrenClassToMethod(bindingClass);

        // Indexing part: be very careful around what gets done when
        final var byYangBuilder = new HashMap<NodeIdentifier, CodecContextSupplier>();

        // Step 1: add leaf children
        var leafBuilder = ImmutableMap.<String, ValueNodeCodecContext>builderWithExpectedSize(leafContexts.size());
        for (var leaf : leafContexts.values()) {
            leafBuilder.put(leaf.getSchema().getQName().getLocalName(), leaf);
            byYangBuilder.put(leaf.getDomPathArgument(), leaf);
        }
        leafNodes = leafBuilder.build();

        final var byBindingArgClassBuilder = new HashMap<Class<?>, DataContainerCodecPrototype<?>>();
        final var byStreamClassBuilder = new HashMap<Class<?>, DataContainerCodecPrototype<?>>();
        final var daoProperties = new HashMap<Class<?>, PropertyInfo>();
        for (var childDataObj : clsToMethod.entrySet()) {
            final var method = childDataObj.getValue();
            verify(!method.isDefault(), "Unexpected default method %s in %s", method, bindingClass);

            final var retClass = childDataObj.getKey();
            if (OpaqueObject.class.isAssignableFrom(retClass)) {
                // Filter OpaqueObjects, they are not containers
                continue;
            }

            // Record getter method
            daoProperties.put(retClass, new PropertyInfo.Getter(method));

            final var childProto = getChildPrototype(runtimeType, factory, itemFactory, retClass);
            byStreamClassBuilder.put(childProto.getBindingClass(), childProto);
            byYangBuilder.put(childProto.getYangArg(), childProto);

            // FIXME: It really feels like we should be specializing DataContainerCodecPrototype so as to ditch
            //        createInstance() and then we could do an instanceof check instead.
            if (childProto.getType() instanceof ChoiceRuntimeType) {
                final var choice = (ChoiceCodecContext<?>) childProto.get();
                for (var cazeChild : choice.getCaseChildrenClasses()) {
                    byBindingArgClassBuilder.put(cazeChild, childProto);
                }
            }
        }

        // Snapshot before below processing
        byStreamClass = ImmutableMap.copyOf(byStreamClassBuilder);

        // Slight footprint optimization: we do not want to copy byStreamClass, as that would force its entrySet view
        // to be instantiated. Furthermore the two maps can easily end up being equal -- hence we can reuse
        // byStreamClass for the purposes of both.
        byBindingArgClassBuilder.putAll(byStreamClassBuilder);
        byBindingArgClass = byStreamClassBuilder.equals(byBindingArgClassBuilder) ? byStreamClass
            : ImmutableMap.copyOf(byBindingArgClassBuilder);

        // Find all non-default nonnullFoo() methods and update the corresponding property info
        for (var entry : getChildrenClassToNonnullMethod(bindingClass).entrySet()) {
            final var method = entry.getValue();
            if (!method.isDefault()) {
                daoProperties.compute(entry.getKey(), (key, value) -> new PropertyInfo.GetterAndNonnull(
                    verifyNotNull(value, "No getter for %s", key).getterMethod(), method));
            }
        }
        // At this point all indexing is done: byYangBuilder should not be referenced
        byYang = ImmutableMap.copyOf(byYangBuilder);

        // Final bits: generate the appropriate class, As a side effect we identify what Augmentations are possible
        if (Augmentable.class.isAssignableFrom(bindingClass)) {
            // Verify we have the appropriate backing runtimeType
            if (!(runtimeType instanceof AugmentableRuntimeType augmentableRuntimeType)) {
                throw new VerifyException(
                    "Unexpected type %s backing augmenable %s".formatted(runtimeType, bindingClass));
            }

            possibleAugmentations = augmentableRuntimeType.augments();
            generatedClass = CodecDataObjectGenerator.generateAugmentable(factory.getLoader(), bindingClass,
                leafContexts, daoProperties, keyMethod);
        } else {
            possibleAugmentations = List.of();
            generatedClass = CodecDataObjectGenerator.generate(factory.getLoader(), bindingClass, leafContexts,
                daoProperties, keyMethod);
        }

        // All done: acquire the constructor: it is supposed to be public
        final MethodHandle ctor;
        try {
            ctor = MethodHandles.publicLookup().findConstructor(generatedClass, CONSTRUCTOR_TYPE);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new LinkageError("Failed to find contructor for class " + generatedClass, e);
        }

        proxyConstructor = ctor.asType(DATAOBJECT_TYPE);
    }

    private static @NonNull DataContainerCodecPrototype<?> getChildPrototype(final CompositeRuntimeType type,
            final CodecContextFactory factory, final CodecItemFactory itemFactory,
            final Class<? extends DataContainer> childClass) {
        final var child = type.bindingChild(JavaTypeName.create(childClass));
        if (child == null) {
            throw DataContainerCodecContext.childNullException(factory.getRuntimeContext(), childClass,
                "Node %s does not have child named %s", type, childClass);
        }
        final var item = itemFactory.createItem(childClass, child.statement());
        if (child instanceof ContainerLikeRuntimeType containerLike) {
            if (child instanceof ContainerRuntimeType container
                && container.statement().findFirstEffectiveSubstatement(PresenceEffectiveStatement.class).isEmpty()) {
                return new StructuralContainerCodecPrototype(item, container, factory);
            }
            return new ContainerLikeCodecPrototype(item, containerLike, factory);
        } else if (child instanceof ListRuntimeType list) {
            return list.keyType() != null ? new MapCodecPrototype(item, list, factory)
                : new ListCodecPrototype(item, list, factory);
        } else if (child instanceof ChoiceRuntimeType choice) {
            return new ChoiceCodecPrototype(item, choice, factory);
        } else {
            throw new UnsupportedOperationException("Unhandled type " + child);
        }
    }


    // FIXME: MDSAL-780: these methods perform analytics using java.lang.reflect to acquire the basic shape of the
    //                   class. This is not exactly AOT friendly, as most of the information should be provided by
    //                   CompositeRuntimeType.
    //
    //                   As as first cut, CompositeRuntimeType should provide the mapping between YANG children and the
    //                   corresponding GETTER_PREFIX/NONNULL_PREFIX method names, If we have that, the use in this
    //                   class should be fine.
    //
    //                   The second cut is binding the actual Method invocations, which is fine here, as this class is
    //                   all about having a run-time generated class. AOT would be providing an alternative, where the
    //                   equivalent class would be generated at compile-time and hence would bind to the methods
    //                   directly -- and AOT equivalent of this class would really be a compile-time generated registry
    //                   to those classes' entry points.

    /**
     * Scans supplied class and returns an iterable of all data children classes.
     *
     * @param type YANG Modeled Entity derived from DataContainer
     * @return Iterable of all data children, which have YANG modeled entity
     */
    private static Map<Class<? extends DataContainer>, Method> getChildrenClassToMethod(final Class<?> type) {
        return getChildClassToMethod(type, Naming.GETTER_PREFIX);
    }

    private static Map<Class<? extends DataContainer>, Method> getChildrenClassToNonnullMethod(final Class<?> type) {
        return getChildClassToMethod(type, Naming.NONNULL_PREFIX);
    }

    private static Map<Class<? extends DataContainer>, Method> getChildClassToMethod(final Class<?> type,
            final String prefix) {
        checkArgument(type != null, "Target type must not be null");
        checkArgument(DataContainer.class.isAssignableFrom(type), "Supplied type %s must be derived from DataContainer",
            type);
        final var ret = new HashMap<Class<? extends DataContainer>, Method>();
        for (Method method : type.getMethods()) {
            DataContainerCodecContext.getYangModeledReturnType(method, prefix)
                .ifPresent(entity -> ret.put(entity, method));
        }
        return ret;
    }
}
