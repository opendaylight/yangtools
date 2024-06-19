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
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.binding.runtime.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.dom.codec.api.IncorrectNestingException;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.OpaqueObject;
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

/**
 * This class is an implementation detail. It is public only due to technical reasons and may change at any time.
 */
@Beta
public abstract class DataObjectCodecContext<D extends DataObject, T extends DataNodeContainer & WithStatus>
        extends DataContainerCodecContext<D, T> {
    private static final class Augmentations implements Immutable {
        final ImmutableMap<YangInstanceIdentifier.PathArgument, DataContainerCodecPrototype<?>> byYang;
        final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byStream;

        Augmentations(final ImmutableMap<YangInstanceIdentifier.PathArgument, DataContainerCodecPrototype<?>> byYang,
            final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byStream) {
            this.byYang = requireNonNull(byYang);
            this.byStream = requireNonNull(byStream);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(DataObjectCodecContext.class);
    private static final MethodType CONSTRUCTOR_TYPE = MethodType.methodType(void.class,
        DataObjectCodecContext.class, NormalizedNodeContainer.class);
    private static final MethodType DATAOBJECT_TYPE = MethodType.methodType(DataObject.class,
        DataObjectCodecContext.class, NormalizedNodeContainer.class);
    private static final Augmentations EMPTY_AUGMENTATIONS = new Augmentations(ImmutableMap.of(), ImmutableMap.of());

    private final ImmutableMap<String, ValueNodeCodecContext> leafChild;
    private final ImmutableMap<YangInstanceIdentifier.PathArgument, NodeContextSupplier> byYang;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byStreamClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byBindingArgClass;
    private final ImmutableMap<AugmentationIdentifier, Type> possibleAugmentations;
    private final MethodHandle proxyConstructor;

    // FIXME: the presence of these two volatile fields may be preventing us from being able to improve
    //        DataContainerCodecPrototype.get() publication.
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<DataObjectCodecContext, Augmentations>
        AUGMENTATIONS_UPDATER = AtomicReferenceFieldUpdater.newUpdater(DataObjectCodecContext.class,
            Augmentations.class, "augmentations");
    private volatile Augmentations augmentations = EMPTY_AUGMENTATIONS;

    private volatile ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> mismatchedAugmented = ImmutableMap.of();

    DataObjectCodecContext(final DataContainerCodecPrototype<T> prototype) {
        this(prototype, null);
    }

    DataObjectCodecContext(final DataContainerCodecPrototype<T> prototype, final Method keyMethod) {
        super(prototype);

        final Class<D> bindingClass = getBindingClass();

        final ImmutableMap<Method, ValueNodeCodecContext> tmpLeaves = factory().getLeafNodes(bindingClass, getSchema());
        final Map<Class<?>, Method> clsToMethod = BindingReflections.getChildrenClassToMethod(bindingClass);

        final Map<YangInstanceIdentifier.PathArgument, NodeContextSupplier> byYangBuilder = new HashMap<>();
        final Map<Class<?>, DataContainerCodecPrototype<?>> byStreamClassBuilder = new HashMap<>();
        final Map<Class<?>, DataContainerCodecPrototype<?>> byBindingArgClassBuilder = new HashMap<>();

        // Adds leaves to mapping
        final Builder<String, ValueNodeCodecContext> leafChildBuilder =
                ImmutableMap.builderWithExpectedSize(tmpLeaves.size());
        for (final Entry<Method, ValueNodeCodecContext> entry : tmpLeaves.entrySet()) {
            final ValueNodeCodecContext leaf = entry.getValue();
            leafChildBuilder.put(leaf.getSchema().getQName().getLocalName(), leaf);
            byYangBuilder.put(leaf.getDomPathArgument(), leaf);
        }
        this.leafChild = leafChildBuilder.build();

        final Map<Method, Class<?>> tmpDataObjects = new HashMap<>();
        for (final Entry<Class<?>, Method> childDataObj : clsToMethod.entrySet()) {
            final Method method = childDataObj.getValue();
            verify(!method.isDefault(), "Unexpected default method %s in %s", method, bindingClass);

            final Class<?> retClass = childDataObj.getKey();
            if (OpaqueObject.class.isAssignableFrom(retClass)) {
                // Filter OpaqueObjects, they are not containers
                continue;
            }

            final DataContainerCodecPrototype<?> childProto = loadChildPrototype(retClass);
            tmpDataObjects.put(method, childProto.getBindingClass());
            byStreamClassBuilder.put(childProto.getBindingClass(), childProto);
            byYangBuilder.put(childProto.getYangArg(), childProto);
            if (childProto.isChoice()) {
                final ChoiceNodeCodecContext<?> choice = (ChoiceNodeCodecContext<?>) childProto.get();
                for (final Class<?> cazeChild : choice.getCaseChildrenClasses()) {
                    byBindingArgClassBuilder.put(cazeChild, childProto);
                }
            }
        }

        this.byYang = ImmutableMap.copyOf(byYangBuilder);
        this.byStreamClass = ImmutableMap.copyOf(byStreamClassBuilder);

        // Slight footprint optimization: we do not want to copy byStreamClass, as that would force its entrySet view
        // to be instantiated. Furthermore the two maps can easily end up being equal -- hence we can reuse
        // byStreamClass for the purposes of both.
        byBindingArgClassBuilder.putAll(byStreamClassBuilder);
        this.byBindingArgClass = byStreamClassBuilder.equals(byBindingArgClassBuilder) ? this.byStreamClass
                : ImmutableMap.copyOf(byBindingArgClassBuilder);

        final Class<? extends CodecDataObject<?>> generatedClass;
        if (Augmentable.class.isAssignableFrom(bindingClass)) {
            this.possibleAugmentations = factory().getRuntimeContext().getAvailableAugmentationTypes(getSchema());
            generatedClass = CodecDataObjectGenerator.generateAugmentable(prototype.getFactory().getLoader(),
                bindingClass, tmpLeaves, tmpDataObjects, keyMethod);
        } else {
            this.possibleAugmentations = ImmutableMap.of();
            generatedClass = CodecDataObjectGenerator.generate(prototype.getFactory().getLoader(), bindingClass,
                tmpLeaves, tmpDataObjects, keyMethod);
        }
        reloadAllAugmentations();

        final MethodHandle ctor;
        try {
            ctor = MethodHandles.publicLookup().findConstructor(generatedClass, CONSTRUCTOR_TYPE);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new LinkageError("Failed to find contructor for class " + generatedClass, e);
        }

        proxyConstructor = ctor.asType(DATAOBJECT_TYPE);
    }

    // This method could be synchronized, but that would mean that concurrent attempts to load an invalid augmentation
    // would end up being unnecessarily contended -- blocking real progress and not being able to run concurrently
    // while producing no effect. We therefore use optimistic read + CAS.
    private void reloadAllAugmentations() {
        // Load current values
        Augmentations oldAugmentations = augmentations;

        // FIXME: can we detect when we have both maps fully populated and skip all of this?

        // Scratch space for additions
        final Map<PathArgument, DataContainerCodecPrototype<?>> addByYang = new HashMap<>();
        final Map<Class<?>, DataContainerCodecPrototype<?>> addByStream = new HashMap<>();

        // Iterate over all possibilities, checking for modifications.
        for (final Type augment : possibleAugmentations.values()) {
            final DataContainerCodecPrototype<?> augProto = getAugmentationPrototype(augment);
            if (augProto != null) {
                final PathArgument yangArg = augProto.getYangArg();
                final Class<?> bindingClass = augProto.getBindingClass();
                if (!oldAugmentations.byYang.containsKey(yangArg)) {
                    if (addByYang.putIfAbsent(yangArg, augProto) == null) {
                        LOG.trace("Discovered new YANG mapping {} -> {} in {}", yangArg, augProto, this);
                    }
                }
                if (!oldAugmentations.byStream.containsKey(bindingClass)) {
                    if (addByStream.putIfAbsent(bindingClass, augProto) == null) {
                        LOG.trace("Discovered new class mapping {} -> {} in {}", bindingClass, augProto, this);
                    }
                }
            }
        }

        while (true) {
            if (addByYang.isEmpty() && addByStream.isEmpty()) {
                LOG.trace("No new augmentations discovered in {}", this);
                return;
            }

            // We have some additions, propagate them out
            final Augmentations newAugmentations = new Augmentations(concatMaps(oldAugmentations.byYang, addByYang),
                concatMaps(oldAugmentations.byStream, addByStream));
            if (AUGMENTATIONS_UPDATER.compareAndSet(this, oldAugmentations, newAugmentations)) {
                // Success, we are done
                return;
            }

            // We have raced installing new augmentations, read them again, remove everything present in the installed
            // once and try again. This may mean that we end up not doing anything, but that's fine.
            oldAugmentations = augmentations;

            // We could use Map.removeAll(oldAugmentations.byYang.keySet()), but that forces the augmentation's keyset
            // to be materialized, which we otherwise do not need. Hence we do this the other way around, instantiating
            // our temporary maps' keySets and iterating over them. That's fine as we'll be throwing those maps away.
            removeMapKeys(addByYang, oldAugmentations.byYang);
            removeMapKeys(addByStream, oldAugmentations.byStream);
        }
    }

    private static <K, V> ImmutableMap<K, V> concatMaps(final ImmutableMap<K, V> old, final Map<K, V> add) {
        if (add.isEmpty()) {
            return old;
        }

        final Builder<K, V> builder = ImmutableMap.builderWithExpectedSize(old.size() + add.size());
        builder.putAll(old);
        builder.putAll(add);
        return builder.build();
    }

    private static <K, V> void removeMapKeys(final Map<K, V> removeFrom, final ImmutableMap<K, V> map) {
        final Iterator<K> it = removeFrom.keySet().iterator();
        while (it.hasNext()) {
            if (map.containsKey(it.next())) {
                it.remove();
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
        final DataContainerCodecContext<?, ?> context = childNonNull(ctxProto, argType,
            "Class %s is not valid child of %s", argType, getBindingClass()).get();
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

    @Override
    public NodeCodecContext yangPathArgumentChild(final YangInstanceIdentifier.PathArgument arg) {
        final NodeContextSupplier childSupplier;
        if (arg instanceof NodeIdentifierWithPredicates) {
            childSupplier = byYang.get(new NodeIdentifier(arg.getNodeType()));
        } else if (arg instanceof AugmentationIdentifier) {
            childSupplier = yangAugmentationChild((AugmentationIdentifier) arg);
        } else {
            childSupplier = byYang.get(arg);
        }

        return childNonNull(childSupplier, arg, "Argument %s is not valid child of %s", arg, getSchema()).get();
    }

    protected final ValueNodeCodecContext getLeafChild(final String name) {
        final ValueNodeCodecContext value = leafChild.get(name);
        if (value == null) {
            throw IncorrectNestingException.create("Leaf %s is not valid for %s", name, getBindingClass());
        }
        return value;
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
        final DataContainerCodecPrototype<?> firstTry = augmentations.byYang.get(arg);
        if (firstTry != null) {
            return firstTry;
        }
        if (possibleAugmentations.containsKey(arg)) {
            // Try to load augmentations, which will potentially update knownAugmentations, hence we re-load that field
            // again.
            reloadAllAugmentations();
            return augmentations.byYang.get(arg);
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
        // if it is present
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
        // Perform a single load, so we can reuse it if we end up going to the reflection-based slow path
        final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> local = augmentations.byStream;
        final DataContainerCodecPrototype<?> childProto = local.get(childClass);
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
            for (final DataContainerCodecPrototype<?> realChild : local.values()) {
                if (Augmentation.class.isAssignableFrom(realChild.getBindingClass())
                        && BindingReflections.isSubstitutionFor(childClass, realChild.getBindingClass())) {
                    return cacheMismatched(childClass, realChild);
                }
            }
        }
        LOG.trace("Failed to resolve {} as a valid augmentation in {}", childClass, this);
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
        LOG.trace("Cached mismatched augmentation {} -> {} in {}", childClass, prototype, this);
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

    @SuppressWarnings("checkstyle:illegalCatch")
    protected final @NonNull D createBindingProxy(final NormalizedNodeContainer<?, ?, ?> node) {
        try {
            return (D) proxyConstructor.invokeExact(this, node);
        } catch (final Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException(e);
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
        for (final DataContainerCodecPrototype<?> value : augmentations.byStream.values()) {
            final Optional<NormalizedNode<?, ?>> augData = data.getChild(value.getYangArg());
            if (augData.isPresent()) {
                map.put(value.getBindingClass(), value.get().deserializeObject(augData.get()));
            }
        }
        return map;
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
