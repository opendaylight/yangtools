/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
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
import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class DataObjectCodecContext<D extends DataObject,T extends DataNodeContainer> extends DataContainerCodecContext<D,T> {
    private static final Logger LOG = LoggerFactory.getLogger(DataObjectCodecContext.class);
    private static final Lookup LOOKUP = MethodHandles.publicLookup();
    private static final MethodType CONSTRUCTOR_TYPE = MethodType.methodType(void.class, InvocationHandler.class);
    private static final MethodType DATAOBJECT_TYPE = MethodType.methodType(DataObject.class, InvocationHandler.class);
    private static final Comparator<Method> METHOD_BY_ALPHABET = new Comparator<Method>() {
        @Override
        public int compare(final Method o1, final Method o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private final ImmutableMap<String, LeafNodeCodecContext<?>> leafChild;
    private final ImmutableMap<YangInstanceIdentifier.PathArgument, NodeContextSupplier> byYang;
    private final ImmutableSortedMap<Method, NodeContextSupplier> byMethod;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byStreamClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byBindingArgClass;
    private final MethodHandle proxyConstructor;

    protected DataObjectCodecContext(final DataContainerCodecPrototype<T> prototype) {
        super(prototype);

        this.leafChild = factory().getLeafNodes(getBindingClass(), schema());

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
                for(final Class<?> cazeChild : choice.getCaseChildrenClasses()) {
                    byBindingArgClassBuilder.put(cazeChild, childProto);
                }
            }
        }
        this.byMethod = ImmutableSortedMap.copyOfSorted(byMethodBuilder);
        if (Augmentable.class.isAssignableFrom(getBindingClass())) {
            final ImmutableMap<AugmentationIdentifier, Type> augmentations = factory().getRuntimeContext()
                    .getAvailableAugmentationTypes(schema());
            for (final Entry<AugmentationIdentifier, Type> augment : augmentations.entrySet()) {
                final DataContainerCodecPrototype<?> augProto = getAugmentationPrototype(augment.getValue());
                if (augProto != null) {
                    byYangBuilder.put(augProto.getYangArg(), augProto);
                    byStreamClassBuilder.put(augProto.getBindingClass(), augProto);
                }
            }
        }

        this.byYang = ImmutableMap.copyOf(byYangBuilder);
        this.byStreamClass = ImmutableMap.copyOf(byStreamClassBuilder);
        byBindingArgClassBuilder.putAll(byStreamClass);
        this.byBindingArgClass = ImmutableMap.copyOf(byBindingArgClassBuilder);

        final Class<?> proxyClass = Proxy.getProxyClass(getBindingClass().getClassLoader(),  new Class[] { getBindingClass() });
        try {
            proxyConstructor = LOOKUP.findConstructor(proxyClass, CONSTRUCTOR_TYPE).asType(DATAOBJECT_TYPE);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to find contructor for class " + proxyClass);
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public <DV extends DataObject> DataContainerCodecContext<DV, ?> streamChild(final Class<DV> childClass) {
        DataContainerCodecPrototype<?> childProto = byStreamClass.get(childClass);
        if (childProto != null) {
            return (DataContainerCodecContext<DV,?>) childProto.get();
        }

        if (Augmentation.class.isAssignableFrom(childClass))  {
            /*
             * It is potentially mismatched valid augmentation - we look up equivalent augmentation
             * using reflection and walk all stream child and compare augmenations classes
             * if they are equivalent.
             *
             * FIXME: Cache mapping of mismatched augmentation to real one, to speed up lookup.
             */
            @SuppressWarnings("rawtypes")
            final Class<?> augTarget = BindingReflections.findAugmentationTarget((Class) childClass);
            if ((getBindingClass().equals(augTarget))) {
                for (final DataContainerCodecPrototype<?> realChild : byStreamClass.values()) {
                    if (Augmentation.class.isAssignableFrom(realChild.getBindingClass())
                            && BindingReflections.isSubstitutionFor(childClass,realChild.getBindingClass())) {
                        childProto = realChild;
                        break;
                    }
                }
            }
        }
        Preconditions.checkArgument(childProto != null, " Child %s is not valid child.",childClass);
        return (DataContainerCodecContext<DV, ?>) childProto.get();
    }


    @SuppressWarnings("unchecked")
    @Override
    public <DV extends DataObject> Optional<DataContainerCodecContext<DV, ?>> possibleStreamChild(
            Class<DV> childClass) {
        final DataContainerCodecPrototype<?> childProto = byStreamClass.get(childClass);
        if(childProto != null) {
            return Optional.<DataContainerCodecContext<DV,?>>of((DataContainerCodecContext<DV,?>) childProto.get());
        }
        return Optional.absent();
    }

    @Override
    public DataContainerCodecContext<?,?> bindingPathArgumentChild(final InstanceIdentifier.PathArgument arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {

        final Class<? extends DataObject> argType = arg.getType();
        final DataContainerCodecPrototype<?> ctxProto = byBindingArgClass.get(argType);
        if(ctxProto != null) {
            final DataContainerCodecContext<?,?> context = ctxProto.get();
            if(context instanceof ChoiceNodeCodecContext) {
                final ChoiceNodeCodecContext<?> choice = (ChoiceNodeCodecContext<?>) context;
                final DataContainerCodecContext<?,?> caze = choice.getCazeByChildClass(arg.getType());
                if(caze != null) {
                    choice.addYangPathArgument(arg, builder);
                    caze.addYangPathArgument(arg, builder);
                    return caze.bindingPathArgumentChild(arg, builder);
                }
                return null;
            }
            context.addYangPathArgument(arg, builder);
            return context;
        }
        // Argument is not valid child.
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public NodeCodecContext<D> yangPathArgumentChild(YangInstanceIdentifier.PathArgument arg) {
        if(arg instanceof NodeIdentifierWithPredicates) {
            arg = new NodeIdentifier(arg.getNodeType());
        }
        final NodeContextSupplier childSupplier = byYang.get(arg);
        Preconditions.checkArgument(childSupplier != null, "Argument %s is not valid child of %s", arg, schema());
        return (NodeCodecContext<D>) childSupplier.get();
    }

    protected final LeafNodeCodecContext<?> getLeafChild(final String name) {
        final LeafNodeCodecContext<?> value = leafChild.get(name);
        Preconditions.checkArgument(value != null, "Leaf %s is not valid for %s", name, getBindingClass());
        return value;
    }

    private DataContainerCodecPrototype<?> loadChildPrototype(final Class<?> childClass) {
        final DataSchemaNode origDef = factory().getRuntimeContext().getSchemaDefinition(childClass);
        // Direct instantiation or use in same module in which grouping
        // was defined.
        DataSchemaNode sameName;
        try {
            sameName = schema().getDataChildByName(origDef.getQName());
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
            final QName instantiedName = QName.create(namespace(), origDef.getQName().getLocalName());
            final DataSchemaNode potential = schema().getDataChildByName(instantiedName);
            // We check if it is really instantiated from same
            // definition as class was derived
            if (potential != null && origDef.equals(SchemaNodeUtils.getRootOriginalIfPossible(potential))) {
                childSchema = potential;
            } else {
                childSchema = null;
            }
        }
        Preconditions.checkArgument(childSchema != null, "Node %s does not have child named %s", schema(), childClass);
        return DataContainerCodecPrototype.from(childClass, childSchema, factory());
    }

    private DataContainerCodecPrototype<?> getAugmentationPrototype(final Type value) {
        final ClassLoadingStrategy loader = factory().getRuntimeContext().getStrategy();
        @SuppressWarnings("rawtypes")
        final Class augClass;
        try {
            augClass = loader.loadClass(value);
        } catch (final ClassNotFoundException e) {
            LOG.warn("Failed to load augmentation prototype for {}", value, e);
            return null;
        }

        @SuppressWarnings("unchecked")
        final Entry<AugmentationIdentifier, AugmentationSchema> augSchema = factory().getRuntimeContext()
                .getResolvedAugmentationSchema(schema(), augClass);
        return DataContainerCodecPrototype.from(augClass, augSchema.getKey(), augSchema.getValue(), factory());
    }

    @SuppressWarnings("rawtypes")
    Object getBindingChildValue(final Method method, final NormalizedNodeContainer domData) {
        final NodeCodecContext<?> childContext = byMethod.get(method).get();
        @SuppressWarnings("unchecked")
        final Optional<NormalizedNode<?, ?>> domChild = domData.getChild(childContext.getDomPathArgument());
        if (domChild.isPresent()) {
            return childContext.deserializeObject(domChild.get());
        }
        return null;
    }

    protected final D createBindingProxy(final NormalizedNodeContainer<?, ?, ?> node) {
        try {
            return (D) proxyConstructor.invokeExact((InvocationHandler)new LazyDataObject<>(this, node));
        } catch (final Throwable e) {
            throw Throwables.propagate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAllAugmentationsFrom(
            final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> data) {

        @SuppressWarnings("rawtypes")
        final Map map = new HashMap<>();

        for(final DataContainerCodecPrototype<?> value : byStreamClass.values()) {
            if(Augmentation.class.isAssignableFrom(value.getBindingClass())) {
                final Optional<NormalizedNode<?, ?>> augData = data.getChild(value.getYangArg());
                if(augData.isPresent()) {
                    map.put(value.getBindingClass(), value.get().deserializeObject(augData.get()));
                }
            }
        }
        return map;
    }

    public Collection<Method> getHashCodeAndEqualsMethods() {
        // FIXME: Sort method in same order as in hashCode for generated class.
        return byMethod.keySet();
    }

    @Override
    public InstanceIdentifier.PathArgument deserializePathArgument(YangInstanceIdentifier.PathArgument arg) {
        Preconditions.checkArgument(getDomPathArgument().equals(arg));
        return bindingArg();
    }

    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(InstanceIdentifier.PathArgument arg) {
        Preconditions.checkArgument(bindingArg().equals(arg));
        return getDomPathArgument();
    }
}
