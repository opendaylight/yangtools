/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class DataObjectCodecContext<D extends DataObject, T extends DataNodeContainer & WithStatus>
        extends DataContainerCodecContext<D, T> {
    private static final Logger LOG = LoggerFactory.getLogger(DataObjectCodecContext.class);
    private static final MethodType CONSTRUCTOR_TYPE = MethodType.methodType(void.class, InvocationHandler.class);
    private static final MethodType DATAOBJECT_TYPE = MethodType.methodType(DataObject.class, InvocationHandler.class);
    private static final Comparator<Method> METHOD_BY_ALPHABET = (o1, o2) -> o1.getName().compareTo(o2.getName());

    private final ImmutableMap<String, LeafNodeCodecContext<?>> leafChild;
    private final ImmutableMap<YangInstanceIdentifier.PathArgument, NodeContextSupplier> byYang;
    private final ImmutableSortedMap<Method, NodeContextSupplier> byMethod;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byStreamClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byBindingArgClass;
    private final ImmutableMap<AugmentationIdentifier, Type> possibleAugmentations;
    private final MethodHandle proxyConstructor;

    private final ConcurrentMap<YangInstanceIdentifier.PathArgument, DataContainerCodecPrototype<?>> byYangAugmented =
            new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, DataContainerCodecPrototype<?>> byStreamAugmented = new ConcurrentHashMap<>();

    DataObjectCodecContext(final DataContainerCodecPrototype<T> prototype) {
        super(prototype);

        this.leafChild = factory().getLeafNodes(getBindingClass(), getSchema());

        final Map<Class<?>, Method> clsToMethod = BindingReflections.getChildrenClassToMethod(getBindingClass());

        final Map<YangInstanceIdentifier.PathArgument, NodeContextSupplier> byYangBuilder = new HashMap<>();
        final SortedMap<Method, NodeContextSupplier> byMethodBuilder = new TreeMap<>(METHOD_BY_ALPHABET);
        final Map<Class<?>, DataContainerCodecPrototype<?>> byStreamClassBuilder = new HashMap<>();
        final Map<Class<?>, DataContainerCodecPrototype<?>> byBindingArgClassBuilder = new HashMap<>();

        // Adds leaves to mapping
        for (final LeafNodeCodecContext<?> leaf : leafChild.values()) {
            byMethodBuilder.put(leaf.getGetter(), leaf);
            byYangBuilder.put(leaf.getDomPathArgument(), leaf);
        }

        for (final Entry<Class<?>, Method> childDataObj : clsToMethod.entrySet()) {
            final DataContainerCodecPrototype<?> childProto = loadChildPrototype(childDataObj.getKey());
            byMethodBuilder.put(childDataObj.getValue(), childProto);
            byStreamClassBuilder.put(childProto.getBindingClass(), childProto);
            byYangBuilder.put(childProto.getYangArg(), childProto);
            if (childProto.isChoice()) {
                final ChoiceNodeCodecContext<?> choice = (ChoiceNodeCodecContext<?>) childProto.get();
                for (final Class<?> cazeChild : choice.getCaseChildrenClasses()) {
                    byBindingArgClassBuilder.put(cazeChild, childProto);
                }
            }
        }
        this.byMethod = ImmutableSortedMap.copyOfSorted(byMethodBuilder);
        this.byYang = ImmutableMap.copyOf(byYangBuilder);
        this.byStreamClass = ImmutableMap.copyOf(byStreamClassBuilder);
        byBindingArgClassBuilder.putAll(byStreamClass);
        this.byBindingArgClass = ImmutableMap.copyOf(byBindingArgClassBuilder);


        if (Augmentable.class.isAssignableFrom(getBindingClass())) {
            this.possibleAugmentations = factory().getRuntimeContext().getAvailableAugmentationTypes(getSchema());
        } else {
            this.possibleAugmentations = ImmutableMap.of();
        }
        reloadAllAugmentations();

        final Class<?> proxyClass = Proxy.getProxyClass(getBindingClass().getClassLoader(), getBindingClass(),
            AugmentationHolder.class);
        try {
            proxyConstructor = MethodHandles.publicLookup().findConstructor(proxyClass, CONSTRUCTOR_TYPE)
                    .asType(DATAOBJECT_TYPE);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to find contructor for class " + proxyClass, e);
        }
    }

    private void reloadAllAugmentations() {
        for (final Entry<AugmentationIdentifier, Type> augment : possibleAugmentations.entrySet()) {
            final DataContainerCodecPrototype<?> augProto = getAugmentationPrototype(augment.getValue());
            if (augProto != null) {
                byYangAugmented.putIfAbsent(augProto.getYangArg(), augProto);
                byStreamAugmented.putIfAbsent(augProto.getBindingClass(), augProto);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends DataObject> DataContainerCodecContext<C, ?> streamChild(final Class<C> childClass) {
        final DataContainerCodecPrototype<?> childProto = streamChildPrototype(childClass);
        return (DataContainerCodecContext<C, ?>) childNonNull(childProto, childClass, " Child %s is not valid child.",
                childClass).get();
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
        return Optional.absent();
    }

    @Override
    public DataContainerCodecContext<?,?> bindingPathArgumentChild(final InstanceIdentifier.PathArgument arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {

        final Class<? extends DataObject> argType = arg.getType();
        DataContainerCodecPrototype<?> ctxProto = byBindingArgClass.get(argType);
        if (ctxProto == null && Augmentation.class.isAssignableFrom(argType)) {
            ctxProto = augmentationByClass(argType);
        }
        final DataContainerCodecContext<?, ?> context =
                childNonNull(ctxProto, argType, "Class %s is not valid child of %s", argType, getBindingClass()).get();
        if (context instanceof ChoiceNodeCodecContext) {
            final ChoiceNodeCodecContext<?> choice = (ChoiceNodeCodecContext<?>) context;
            choice.addYangPathArgument(arg, builder);

            final java.util.Optional<? extends Class<? extends DataObject>> caseType = arg.getCaseType();
            final Class<? extends DataObject> type = arg.getType();
            final DataContainerCodecContext<?, ?> caze;
            if (caseType.isPresent()) {
                // Non-ambiguous addressing this should not pose any problems
                caze = choice.streamChild(caseType.get());
            } else {
                caze = choice.getCaseByChildClass(type);
            }

            caze.addYangPathArgument(arg, builder);
            return caze.bindingPathArgumentChild(arg, builder);
        }
        context.addYangPathArgument(arg, builder);
        return context;
    }

    @SuppressWarnings("unchecked")
    @Override
    public NodeCodecContext<D> yangPathArgumentChild(final YangInstanceIdentifier.PathArgument arg) {
        final NodeContextSupplier childSupplier;
        if (arg instanceof NodeIdentifierWithPredicates) {
            childSupplier = byYang.get(new NodeIdentifier(arg.getNodeType()));
        } else if (arg instanceof AugmentationIdentifier) {
            childSupplier = yangAugmentationChild((AugmentationIdentifier) arg);
        } else {
            childSupplier = byYang.get(arg);
        }

        return (NodeCodecContext<D>) childNonNull(childSupplier, arg,
            "Argument %s is not valid child of %s", arg, getSchema()).get();
    }

    protected final LeafNodeCodecContext<?> getLeafChild(final String name) {
        final LeafNodeCodecContext<?> value = leafChild.get(name);
        return IncorrectNestingException.checkNonNull(value, "Leaf %s is not valid for %s", name, getBindingClass());
    }

    private DataContainerCodecPrototype<?> loadChildPrototype(final Class<?> childClass) {
        final DataSchemaNode origDef = factory().getRuntimeContext().getSchemaDefinition(childClass);
        // Direct instantiation or use in same module in which grouping
        // was defined.
        DataSchemaNode sameName;
        try {
            sameName = getSchema().getDataChildByName(origDef.getQName());
        } catch (final IllegalArgumentException e) {
            sameName = null;
        }
        final DataSchemaNode childSchema;
        if (sameName != null) {
            // Exactly same schema node
            if (origDef.equals(sameName)) {
                childSchema = sameName;
                // We check if instantiated node was added via uses
                // statement and is instantiation of same grouping
            } else if (origDef.equals(SchemaNodeUtils.getRootOriginalIfPossible(sameName))) {
                childSchema = sameName;
            } else {
                // Node has same name, but clearly is different
                childSchema = null;
            }
        } else {
            // We are looking for instantiation via uses in other module
            final QName instantiedName = origDef.getQName().withModule(namespace());
            final DataSchemaNode potential = getSchema().getDataChildByName(instantiedName);
            // We check if it is really instantiated from same
            // definition as class was derived
            if (potential != null && origDef.equals(SchemaNodeUtils.getRootOriginalIfPossible(potential))) {
                childSchema = potential;
            } else {
                childSchema = null;
            }
        }
        final DataSchemaNode nonNullChild =
                childNonNull(childSchema, childClass, "Node %s does not have child named %s", getSchema(), childClass);
        return DataContainerCodecPrototype.from(createBindingArg(childClass, nonNullChild), nonNullChild, factory());
    }

    @SuppressWarnings("unchecked")
    Item<?> createBindingArg(final Class<?> childClass, final DataSchemaNode childSchema) {
        return Item.of((Class<? extends DataObject>) childClass);
    }

    private DataContainerCodecPrototype<?> yangAugmentationChild(final AugmentationIdentifier arg) {
        final DataContainerCodecPrototype<?> firstTry = byYangAugmented.get(arg);
        if (firstTry != null) {
            return firstTry;
        }
        if (possibleAugmentations.containsKey(arg)) {
            reloadAllAugmentations();
            return byYangAugmented.get(arg);
        }
        return null;
    }

    @Nullable
    private DataContainerCodecPrototype<?> augmentationByClass(@Nonnull final Class<?> childClass) {
        final DataContainerCodecPrototype<?> firstTry = augmentationByClassOrEquivalentClass(childClass);
        if (firstTry != null) {
            return firstTry;
        }
        reloadAllAugmentations();
        return augmentationByClassOrEquivalentClass(childClass);
    }

    @Nullable
    private DataContainerCodecPrototype<?> augmentationByClassOrEquivalentClass(@Nonnull final Class<?> childClass) {
        final DataContainerCodecPrototype<?> childProto = byStreamAugmented.get(childClass);
        if (childProto != null) {
            return childProto;
        }

        /*
         * It is potentially mismatched valid augmentation - we look up equivalent augmentation
         * using reflection and walk all stream child and compare augmenations classes if they are
         * equivalent.
         *
         * FIXME: Cache mapping of mismatched augmentation to real one, to speed up lookup.
         */
        @SuppressWarnings("rawtypes")
        final Class<?> augTarget = BindingReflections.findAugmentationTarget((Class) childClass);
        if (getBindingClass().equals(augTarget)) {
            for (final DataContainerCodecPrototype<?> realChild : byStreamAugmented.values()) {
                if (Augmentation.class.isAssignableFrom(realChild.getBindingClass())
                        && BindingReflections.isSubstitutionFor(childClass, realChild.getBindingClass())) {
                    return realChild;
                }
            }
        }
        return null;
    }

    private DataContainerCodecPrototype<?> getAugmentationPrototype(final Type value) {
        final ClassLoadingStrategy loader = factory().getRuntimeContext().getStrategy();
        @SuppressWarnings("rawtypes")
        final Class augClass;
        try {
            augClass = loader.loadClass(value);
        } catch (final ClassNotFoundException e) {
            LOG.debug("Failed to load augmentation prototype for {}. Will be retried when needed.", value, e);
            return null;
        }

        @SuppressWarnings("unchecked")
        final Entry<AugmentationIdentifier, AugmentationSchemaNode> augSchema = factory().getRuntimeContext()
                .getResolvedAugmentationSchema(getSchema(), augClass);
        return DataContainerCodecPrototype.from(augClass, augSchema.getKey(), augSchema.getValue(), factory());
    }

    @SuppressWarnings("rawtypes")
    Object getBindingChildValue(final Method method, final NormalizedNodeContainer domData) {
        final NodeCodecContext<?> childContext = byMethod.get(method).get();
        @SuppressWarnings("unchecked")
        final java.util.Optional<NormalizedNode<?, ?>> domChild = domData.getChild(childContext.getDomPathArgument());
        if (domChild.isPresent()) {
            return childContext.deserializeObject(domChild.get());
        } else if (childContext instanceof LeafNodeCodecContext) {
            return ((LeafNodeCodecContext)childContext).defaultObject();
        } else {
            return null;
        }
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    protected final D createBindingProxy(final NormalizedNodeContainer<?, ?, ?> node) {
        try {
            return (D) proxyConstructor.invokeExact((InvocationHandler)new LazyDataObject<>(this, node));
        } catch (final Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAllAugmentationsFrom(
            final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> data) {

        @SuppressWarnings("rawtypes")
        final Map map = new HashMap<>();

        for (final NormalizedNode<?, ?> childValue : data.getValue()) {
            if (childValue instanceof AugmentationNode) {
                final AugmentationNode augDomNode = (AugmentationNode) childValue;
                final DataContainerCodecPrototype<?> codecProto = yangAugmentationChild(augDomNode.getIdentifier());
                if (codecProto != null) {
                    final DataContainerCodecContext<?, ?> codec = codecProto.get();
                    map.put(codec.getBindingClass(), codec.deserializeObject(augDomNode));
                }
            }
        }
        for (final DataContainerCodecPrototype<?> value : byStreamAugmented.values()) {
            final java.util.Optional<NormalizedNode<?, ?>> augData = data.getChild(value.getYangArg());
            if (augData.isPresent()) {
                map.put(value.getBindingClass(), value.get().deserializeObject(augData.get()));
            }
        }
        return map;
    }

    Collection<Method> getHashCodeAndEqualsMethods() {
        return byMethod.keySet();
    }

    @Override
    public InstanceIdentifier.PathArgument deserializePathArgument(final YangInstanceIdentifier.PathArgument arg) {
        Preconditions.checkArgument(getDomPathArgument().equals(arg));
        return bindingArg();
    }

    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(final InstanceIdentifier.PathArgument arg) {
        Preconditions.checkArgument(bindingArg().equals(arg));
        return getDomPathArgument();
    }
}
