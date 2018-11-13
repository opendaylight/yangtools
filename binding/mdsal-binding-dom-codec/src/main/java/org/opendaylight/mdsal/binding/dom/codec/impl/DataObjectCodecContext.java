/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSortedMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
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
    private static final Comparator<Method> METHOD_BY_ALPHABET = Comparator.comparing(Method::getName);

    private final ImmutableMap<String, LeafNodeCodecContext<?>> leafChild;
    private final ImmutableMap<YangInstanceIdentifier.PathArgument, NodeContextSupplier> byYang;
    private final ImmutableSortedMap<Method, NodeContextSupplier> byMethod;
    private final ImmutableMap<Method, NodeContextSupplier> nonnullMethods;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byStreamClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byBindingArgClass;
    private final ImmutableMap<AugmentationIdentifier, Type> possibleAugmentations;
    private final MethodHandle proxyConstructor;

    private final ConcurrentMap<YangInstanceIdentifier.PathArgument, DataContainerCodecPrototype<?>> byYangAugmented =
            new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, DataContainerCodecPrototype<?>> byStreamAugmented = new ConcurrentHashMap<>();

    private volatile ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> mismatchedAugmented = ImmutableMap.of();

    DataObjectCodecContext(final DataContainerCodecPrototype<T> prototype) {
        super(prototype);

        final Class<D> bindingClass = getBindingClass();
        this.leafChild = factory().getLeafNodes(bindingClass, getSchema());

        final Map<Class<?>, Method> clsToMethod = BindingReflections.getChildrenClassToMethod(bindingClass);

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
            final Method method = childDataObj.getValue();
            verify(!method.isDefault(), "Unexpected default method %s in %s", method, bindingClass);
            final DataContainerCodecPrototype<?> childProto = loadChildPrototype(childDataObj.getKey());
            byMethodBuilder.put(method, childProto);
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

        final Map<Class<?>, Method> clsToNonnull = BindingReflections.getChildrenClassToNonnullMethod(bindingClass);
        final Map<Method, NodeContextSupplier> nonnullMethodsBuilder = new HashMap<>();
        for (final Entry<Class<?>, Method> entry : clsToNonnull.entrySet()) {
            final Method method = entry.getValue();
            if (!method.isDefault()) {
                LOG.warn("Ignoring non-default method {} in {}", method, bindingClass);
                continue;
            }
            final DataContainerCodecPrototype<?> supplier = byStreamClass.get(entry.getKey());
            if (supplier != null) {
                nonnullMethodsBuilder.put(method, supplier);
            } else {
                LOG.warn("Failed to look up data handler for method {}", method);
            }
        }

        nonnullMethods = ImmutableMap.copyOf(nonnullMethodsBuilder);

        if (Augmentable.class.isAssignableFrom(bindingClass)) {
            this.possibleAugmentations = factory().getRuntimeContext().getAvailableAugmentationTypes(getSchema());
        } else {
            this.possibleAugmentations = ImmutableMap.of();
        }
        reloadAllAugmentations();

        final Class<?> proxyClass = Proxy.getProxyClass(bindingClass.getClassLoader(), bindingClass,
            AugmentationHolder.class);
        try {
            proxyConstructor = MethodHandles.publicLookup().findConstructor(proxyClass, CONSTRUCTOR_TYPE)
                    .asType(DATAOBJECT_TYPE);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to find contructor for class " + proxyClass, e);
        }
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_OF_PUTIFABSENT_IGNORED")
    private void reloadAllAugmentations() {
        for (final Type augment : possibleAugmentations.values()) {
            final DataContainerCodecPrototype<?> augProto = getAugmentationPrototype(augment);
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
        final DataContainerCodecContext<?, ?> context =
                childNonNull(ctxProto, argType, "Class %s is not valid child of %s", argType, getBindingClass()).get();
        if (context instanceof ChoiceNodeCodecContext) {
            final ChoiceNodeCodecContext<?> choice = (ChoiceNodeCodecContext<?>) context;
            choice.addYangPathArgument(arg, builder);

            final Optional<? extends Class<? extends DataObject>> caseType = arg.getCaseType();
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

    private @Nullable DataContainerCodecPrototype<?> augmentationByClass(final @NonNull Class<?> childClass) {
        DataContainerCodecPrototype<?> lookup = augmentationByClassOrEquivalentClass(childClass);
        if (lookup != null || !isPotentialAugmentation(childClass)) {
            return lookup;
        }

        // Attempt to reload all augmentations using TCCL and lookup again
        reloadAllAugmentations();
        lookup = augmentationByClassOrEquivalentClass(childClass);
        if (lookup != null) {
            return lookup;
        }

        // Still no result, this can be caused by TCCL not being set up properly -- try the class's ClassLoader
        // if it is present;
        final ClassLoader loader = childClass.getClassLoader();
        if (loader == null) {
            return null;
        }

        LOG.debug("Class {} not loaded via TCCL, attempting to recover", childClass);
        ClassLoaderUtils.runWithClassLoader(loader, this::reloadAllAugmentations);
        return augmentationByClassOrEquivalentClass(childClass);
    }

    private boolean isPotentialAugmentation(final Class<?> childClass) {
        final JavaTypeName name = JavaTypeName.create(childClass);
        for (Type type : possibleAugmentations.values()) {
            if (name.equals(type.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    private @Nullable DataContainerCodecPrototype<?> augmentationByClassOrEquivalentClass(
            final @NonNull Class<?> childClass) {
        final DataContainerCodecPrototype<?> childProto = byStreamAugmented.get(childClass);
        if (childProto != null) {
            return childProto;
        }

        /*
         * It is potentially mismatched valid augmentation - we look up equivalent augmentation using reflection
         * and walk all stream child and compare augmentations classes if they are equivalent. When we find a match
         * we'll cache it so we do not need to perform reflection operations again.
         */
        final DataContainerCodecPrototype<?> mismatched = mismatchedAugmented.get(childClass);
        if (mismatched != null) {
            return mismatched;
        }

        @SuppressWarnings("rawtypes")
        final Class<?> augTarget = BindingReflections.findAugmentationTarget((Class) childClass);
        if (getBindingClass().equals(augTarget)) {
            for (final DataContainerCodecPrototype<?> realChild : byStreamAugmented.values()) {
                if (Augmentation.class.isAssignableFrom(realChild.getBindingClass())
                        && BindingReflections.isSubstitutionFor(childClass, realChild.getBindingClass())) {
                    return cacheMismatched(childClass, realChild);
                }
            }
        }
        return null;
    }

    private synchronized DataContainerCodecPrototype<?> cacheMismatched(final Class<?> childClass,
            final DataContainerCodecPrototype<?> prototype) {
        // Original access was unsynchronized, we need to perform additional checking
        final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> local = mismatchedAugmented;
        final DataContainerCodecPrototype<?> existing = local.get(childClass);
        if (existing != null) {
            return existing;
        }

        final Builder<Class<?>, DataContainerCodecPrototype<?>> builder = ImmutableMap.builderWithExpectedSize(
            local.size() + 1);
        builder.putAll(local);
        builder.put(childClass, prototype);

        mismatchedAugmented = builder.build();
        return prototype;
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

    Object getBindingChildValue(final Method method, final NormalizedNodeContainer<?, ?, ?> domData) {
        return method.isDefault() ? getBindingChildValue(nonnullMethods, method, domData, dummy -> ImmutableList.of())
                : getBindingChildValue(byMethod, method, domData, NodeCodecContext::defaultObject);
    }

    @SuppressWarnings("rawtypes")
    private static Object getBindingChildValue(final ImmutableMap<Method, NodeContextSupplier> map, final Method method,
            final NormalizedNodeContainer domData, final Function<NodeCodecContext<?>, Object> getDefaultObject) {
        final NodeCodecContext<?> childContext = verifyNotNull(map.get(method),
            "Cannot find data handler for method %s", method).get();

        @SuppressWarnings("unchecked")
        final Optional<NormalizedNode<?, ?>> domChild = domData.getChild(childContext.getDomPathArgument());

        // We do not want to use Optional.map() here because we do not want to invoke defaultObject() when we have
        // normal value because defaultObject() may end up throwing an exception intentionally.
        return domChild.isPresent() ? childContext.deserializeObject(domChild.get())
                : getDefaultObject.apply(childContext);
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
            final Optional<NormalizedNode<?, ?>> augData = data.getChild(value.getYangArg());
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
        checkArgument(getDomPathArgument().equals(arg));
        return bindingArg();
    }

    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(final InstanceIdentifier.PathArgument arg) {
        checkArgument(bindingArg().equals(arg));
        return getDomPathArgument();
    }
}
