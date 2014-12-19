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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
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

abstract class DataObjectCodecContext<T extends DataNodeContainer> extends DataContainerCodecContext<T> {
    private static final Logger LOG = LoggerFactory.getLogger(DataObjectCodecContext.class);
    private static final Class<?>[] CONSTRUCTOR_ARGS = new Class[] { InvocationHandler.class };
    private static final Comparator<Method> METHOD_BY_ALPHABET = new Comparator<Method>() {
        @Override
        public int compare(final Method o1, final Method o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private final ImmutableMap<String, LeafNodeCodecContext> leafChild;
    private final ImmutableMap<YangInstanceIdentifier.PathArgument, NodeContextSupplier> byYang;
    private final ImmutableSortedMap<Method, NodeContextSupplier> byMethod;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byStreamClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byBindingArgClass;
    private final Constructor<?> proxyConstructor;

    // FIXME: this field seems to be unused
    private final Method augmentationGetter;

    protected DataObjectCodecContext(final DataContainerCodecPrototype<T> prototype) {
        super(prototype);

        this.leafChild = factory().getLeafNodes(bindingClass(), schema());

        Map<Class<?>, Method> clsToMethod = BindingReflections.getChildrenClassToMethod(bindingClass());

        Map<YangInstanceIdentifier.PathArgument, NodeContextSupplier> byYangBuilder = new HashMap<>();
        SortedMap<Method, NodeContextSupplier> byMethodBuilder = new TreeMap<>(METHOD_BY_ALPHABET);
        Map<Class<?>, DataContainerCodecPrototype<?>> byStreamClassBuilder = new HashMap<>();
        Map<Class<?>, DataContainerCodecPrototype<?>> byBindingArgClassBuilder = new HashMap<>();

        // Adds leaves to mapping
        for (LeafNodeCodecContext leaf : leafChild.values()) {
            byMethodBuilder.put(leaf.getGetter(), leaf);
            byYangBuilder.put(leaf.getDomPathArgument(), leaf);
        }

        for (Entry<Class<?>, Method> childDataObj : clsToMethod.entrySet()) {
            DataContainerCodecPrototype<?> childProto = loadChildPrototype(childDataObj.getKey());
            byMethodBuilder.put(childDataObj.getValue(), childProto);
            byStreamClassBuilder.put(childProto.getBindingClass(), childProto);
            byYangBuilder.put(childProto.getYangArg(), childProto);
            if (childProto.isChoice()) {
                ChoiceNodeCodecContext choice = (ChoiceNodeCodecContext) childProto.get();
                for(Class<?> cazeChild : choice.getCaseChildrenClasses()) {
                    byBindingArgClassBuilder.put(cazeChild, childProto);
                }
            }
        }
        this.byMethod = ImmutableSortedMap.copyOfSorted(byMethodBuilder);
        if (Augmentable.class.isAssignableFrom(bindingClass())) {
            try {
                augmentationGetter = bindingClass().getMethod("getAugmentation", Class.class);
            } catch (NoSuchMethodException | SecurityException e) {
               throw new IllegalStateException("Could not get required method.",e);
            }
            ImmutableMap<AugmentationIdentifier, Type> augmentations = factory().getRuntimeContext()
                    .getAvailableAugmentationTypes(schema());
            for (Entry<AugmentationIdentifier, Type> augment : augmentations.entrySet()) {
                DataContainerCodecPrototype<?> augProto = getAugmentationPrototype(augment.getValue());
                if (augProto != null) {
                    byYangBuilder.put(augProto.getYangArg(), augProto);
                    byStreamClassBuilder.put(augProto.getBindingClass(), augProto);
                }
            }
        } else {
            augmentationGetter = null;
        }

        this.byYang = ImmutableMap.copyOf(byYangBuilder);
        this.byStreamClass = ImmutableMap.copyOf(byStreamClassBuilder);
        byBindingArgClassBuilder.putAll(byStreamClass);
        this.byBindingArgClass = ImmutableMap.copyOf(byBindingArgClassBuilder);

        final Class<?> proxyClass = Proxy.getProxyClass(bindingClass().getClassLoader(),  new Class[] { bindingClass() });
        try {
            proxyConstructor = proxyClass.getConstructor(CONSTRUCTOR_ARGS);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Failed to find constructor");
        }
    }

    @Override
    protected DataContainerCodecContext<?> getStreamChild(final Class<?> childClass) {
        DataContainerCodecPrototype<?> childProto = byStreamClass.get(childClass);
        if (childProto != null) {
            return childProto.get();
        }

        if (Augmentation.class.isAssignableFrom(childClass))  {
            /*
             * It is potentially mismatched valid augmentation - we look up equivalent augmentation
             * using reflection and walk all stream child and compare augmenations classes
             * if they are equivalent.
             *
             * FIXME: Cache mapping of mismatched augmentation to real one, to speed up lookup.
             */
            Class<?> augTarget = BindingReflections.findAugmentationTarget((Class) childClass);
            if ((bindingClass().equals(augTarget))) {
                for (DataContainerCodecPrototype<?> realChild : byStreamClass.values()) {
                    if (Augmentation.class.isAssignableFrom(realChild.getBindingClass())
                            && BindingReflections.isSubstitutionFor(childClass,realChild.getBindingClass())) {
                        childProto = realChild;
                        break;
                    }
                }
            }
        }
        Preconditions.checkArgument(childProto != null, " Child %s is not valid child.",childClass);
        return childProto.get();
    }

    @Override
    protected Optional<DataContainerCodecContext<?>> getPossibleStreamChild(final Class<?> childClass) {
        DataContainerCodecPrototype<?> childProto = byStreamClass.get(childClass);
        if(childProto != null) {
            return Optional.<DataContainerCodecContext<?>>of(childProto.get());
        }
        return Optional.absent();
    }

    @Override
    protected DataContainerCodecContext<?> getIdentifierChild(final InstanceIdentifier.PathArgument arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {

        Class<? extends DataObject> argType = arg.getType();
        DataContainerCodecPrototype<?> ctxProto = byBindingArgClass.get(argType);
        Preconditions.checkArgument(ctxProto != null,"Invalid child");

        DataContainerCodecContext<?> context = ctxProto.get();
        if(context instanceof ChoiceNodeCodecContext) {
            ChoiceNodeCodecContext casted = (ChoiceNodeCodecContext) context;
            casted.addYangPathArgument(arg, builder);
            DataContainerCodecContext<?> caze = casted.getCazeByChildClass(arg.getType());
            caze.addYangPathArgument(arg, builder);
            return caze.getIdentifierChild(arg, builder);
        }
        context.addYangPathArgument(arg, builder);
        return context;
    }

    @Override
    protected NodeCodecContext getYangIdentifierChild(YangInstanceIdentifier.PathArgument arg) {
        if(arg instanceof NodeIdentifierWithPredicates) {
            arg = new NodeIdentifier(arg.getNodeType());
        }
        NodeContextSupplier childSupplier = byYang.get(arg);
        Preconditions.checkArgument(childSupplier != null, "Argument %s is not valid child of %s", arg, schema());
        return childSupplier.get();
    }

    protected final LeafNodeCodecContext getLeafChild(final String name) {
        final LeafNodeCodecContext value = leafChild.get(name);
        Preconditions.checkArgument(value != null, "Leaf %s is not valid for %s", name, bindingClass());
        return value;
    }

    private DataContainerCodecPrototype<?> loadChildPrototype(final Class<?> childClass) {
        DataSchemaNode origDef = factory().getRuntimeContext().getSchemaDefinition(childClass);
        // Direct instantiation or use in same module in which grouping
        // was defined.
        DataSchemaNode sameName;
        try {
            sameName = schema().getDataChildByName(origDef.getQName());
        } catch (IllegalArgumentException e) {
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
            QName instantiedName = QName.create(namespace(), origDef.getQName().getLocalName());
            DataSchemaNode potential = schema().getDataChildByName(instantiedName);
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
        ClassLoadingStrategy loader = factory().getRuntimeContext().getStrategy();
        @SuppressWarnings("rawtypes")
        final Class augClass;
        try {
            augClass = loader.loadClass(value);
        } catch (ClassNotFoundException e) {
            LOG.warn("Failed to load augmentation prototype for {}", value, e);
            return null;
        }

        Entry<AugmentationIdentifier, AugmentationSchema> augSchema = factory().getRuntimeContext()
                .getResolvedAugmentationSchema(schema(), augClass);
        return DataContainerCodecPrototype.from(augClass, augSchema.getKey(), augSchema.getValue(), factory());
    }

    @SuppressWarnings("rawtypes")
    Object getBindingChildValue(final Method method, final NormalizedNodeContainer domData) {
        NodeCodecContext childContext = byMethod.get(method).get();
        Optional<NormalizedNode<?, ?>> domChild = domData.getChild(childContext.getDomPathArgument());
        if (domChild.isPresent()) {
            return childContext.dataFromNormalizedNode(domChild.get());
        }
        return null;
    }

    protected final DataObject createBindingProxy(final NormalizedNodeContainer<?, ?, ?> node) {
        try {
            return (DataObject) proxyConstructor.newInstance(new Object[] { new LazyDataObject(this, node) });
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to construct proxy for " + node, e);
        }
    }

    public Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAllAugmentationsFrom(
            final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> data) {

        @SuppressWarnings("rawtypes")
        Map map = new HashMap<>();

        for(DataContainerCodecPrototype<?> value : byStreamClass.values()) {
            if(Augmentation.class.isAssignableFrom(value.getBindingClass())) {
                Optional<NormalizedNode<?, ?>> augData = data.getChild(value.getYangArg());
                if(augData.isPresent()) {
                    map.put(value.getBindingClass(), value.get().dataFromNormalizedNode(augData.get()));
                }
            }
        }
        return map;
    }

    public Collection<Method> getHashCodeAndEqualsMethods() {
        // FIXME: Sort method in same order as in hashCode for generated class.
        return byMethod.keySet();
    }

}
