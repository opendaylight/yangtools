/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.impl.CompositeNodeTOImpl;
import org.opendaylight.yangtools.yang.data.impl.SimpleNodeTOImpl;
import org.opendaylight.yangtools.yang.data.impl.codec.CodecRegistry;
import org.opendaylight.yangtools.yang.data.impl.codec.IdentifierCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.InstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.ValueWithQName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstanceIdentifierCodecImpl implements InstanceIdentifierCodec {
    private static final Logger LOG = LoggerFactory.getLogger(InstanceIdentifierCodecImpl.class);

    private final CodecRegistry codecRegistry;

    private final Map<Class<?>,Set<List<QName>>> augmentationAdapted = new WeakHashMap<>();

    private final Map<Class<?>, Map<List<QName>, Class<?>>> classToPreviousAugment = Collections
            .synchronizedMap(new WeakHashMap<Class<?>, Map<List<QName>, Class<?>>>());

    public InstanceIdentifierCodecImpl(final CodecRegistry registry) {
        this.codecRegistry = registry;
    }

    @Override
    public InstanceIdentifier<? extends Object> deserialize(
            final org.opendaylight.yangtools.yang.data.api.InstanceIdentifier input) {
        Class<?> baType = null;
        List<org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument> biArgs = input.getPath();
        List<QName> scannedPath = new ArrayList<>(biArgs.size());
        List<InstanceIdentifier.PathArgument> baArgs = new ArrayList<InstanceIdentifier.PathArgument>(biArgs.size());
        for (org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument biArg : biArgs) {

            scannedPath.add(biArg.getNodeType());
            org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument baArg = deserializePathArgument(
                    biArg, scannedPath);
            if (baArg != null) {
                baType = baArg.getType();
            }
            Map<List<QName>, Class<?>> injectAugment = classToPreviousAugment.get(baType);
            if (injectAugment != null) {
                @SuppressWarnings("unchecked")
                Class<? extends DataObject> augment = (Class<? extends DataObject>) injectAugment.get(scannedPath);
                if (augment != null) {
                    baArgs.add(new Item(augment));
                }
            }
            baArgs.add(baArg);
        }
        InstanceIdentifier<?> ret = InstanceIdentifier.create(baArgs);
        LOG.debug("DOM Instance Identifier {} deserialized to {}", input, ret);
        return ret;
    }

    @Override
    public InstanceIdentifier<? extends Object> deserialize(
            final org.opendaylight.yangtools.yang.data.api.InstanceIdentifier input,
            final InstanceIdentifier<?> bindingIdentifier) {
        return deserialize(input);
    }

    private InstanceIdentifier.PathArgument deserializeNodeIdentifier(
            final NodeIdentifier argument, final List<QName> processedPath) {
        @SuppressWarnings("rawtypes")
        final Class cls = codecRegistry.getClassForPath(processedPath);
        @SuppressWarnings("unchecked")
        Item<DataObject> item = new Item<>(cls);
        return item;
    }

    private InstanceIdentifier.PathArgument deserializeNodeIdentifierWithPrecicates(
            final NodeIdentifierWithPredicates argument, final List<QName> processedPath) {
        @SuppressWarnings("rawtypes")
        final Class type = codecRegistry.getClassForPath(processedPath);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final IdentifierCodec codec = codecRegistry
                .<Identifiable<? extends Object>> getIdentifierCodecForIdentifiable(type);
        CompositeNode _compositeNode = this.toCompositeNode(argument);
        @SuppressWarnings("unchecked")
        ValueWithQName<CompositeNode> deserialize = codec.deserialize(_compositeNode);
        Object value = null;
        if (deserialize != null) {
            value = deserialize.getValue();
        }
        return CodecTypeUtils.newIdentifiableItem(type, value);
    }

    public CompositeNode toCompositeNode(final NodeIdentifierWithPredicates predicates) {
        Set<Map.Entry<QName, Object>> keyValues = predicates.getKeyValues().entrySet();
        List<Node<?>> values = new ArrayList<>(keyValues.size());
        for (Map.Entry<QName, Object> keyValue : keyValues) {
            values.add(new SimpleNodeTOImpl<Object>(keyValue.getKey(), null, keyValue.getValue()));
        }
        return new CompositeNodeTOImpl(predicates.getNodeType(), null, values);
    }

    @Override
    public org.opendaylight.yangtools.yang.data.api.InstanceIdentifier serialize(final InstanceIdentifier<?> input) {
        Class<?> previousAugmentation = null;
        Iterable<InstanceIdentifier.PathArgument> pathArgs = input.getPathArguments();
        QName previousQName = null;
        List<PathArgument> components = new ArrayList<>();
        List<QName> qnamePath = new ArrayList<>();
        for (InstanceIdentifier.PathArgument baArg : pathArgs) {
            if (!Augmentation.class.isAssignableFrom(baArg.getType())) {
                PathArgument biArg = serializePathArgumentAndUpdateMapping(qnamePath, baArg, previousQName,previousAugmentation);
                components.add(biArg);
                qnamePath.add(biArg.getNodeType());
                previousQName = biArg.getNodeType();
                previousAugmentation = null;
            } else {
                previousQName = codecRegistry.getQNameForAugmentation(baArg.getType());
                previousAugmentation = baArg.getType();
                ensureAugmentation(qnamePath,previousQName,baArg.getType());
            }
        }
        org.opendaylight.yangtools.yang.data.api.InstanceIdentifier ret =
                org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.create(components);
        LOG.debug("Binding Instance Identifier {} serialized to DOM InstanceIdentifier {}", input, ret);
        return ret;
    }

    private synchronized void ensureAugmentation(final List<QName> augPath, final QName augQName, final Class<? extends DataObject> type) {
        Set<List<QName>> augPotential = augmentationAdapted.get(type);
        if(augPotential == null) {
            augPotential = new HashSet<>();
            augmentationAdapted.put(type, augPotential);
        }
        ImmutableList<QName> augTargetPath = ImmutableList.copyOf(augPath);
        if(augPotential.contains(augPath)) {
            return;
        }

        for(Class<? extends DataObject> child : BindingReflections.getChildrenClasses(type)) {
            Item<? extends DataObject> baArg = new Item<>(child);
            PathArgument biArg = serializePathArgumentAndUpdateMapping(augPath, baArg, augQName,type);
        }
        augPotential.add(augTargetPath);
    }


    public Class<? extends Object> updateAugmentationInjection(final Class<? extends DataObject> class1,
            final List<QName> list, final Class<?> augmentation) {
        if (classToPreviousAugment.get(class1) == null) {
            classToPreviousAugment.put(class1, new ConcurrentHashMap<List<QName>, Class<?>>());
        }
        return classToPreviousAugment.get(class1).put(list, augmentation);
    }

    private PathArgument serializeItem(final Item<?> argument, final QName previousQname) {
        Class<?> type = argument.getType();
        QName qname = BindingReflections.findQName(type);
        if (previousQname == null || (BindingReflections.isAugmentationChild(argument.getType()))) {
            return new NodeIdentifier(qname);
        }
        return new NodeIdentifier(QName.create(previousQname, qname.getLocalName()));
    }

    private PathArgument serializeIdentifiableItem(final IdentifiableItem<?,?> argument, final QName previousQname) {
        Map<QName, Object> predicates = new HashMap<>();
        @SuppressWarnings("rawtypes")
        Class type = argument.getType();
        @SuppressWarnings("unchecked")
        IdentifierCodec<? extends Object> keyCodec = codecRegistry.getIdentifierCodecForIdentifiable(type);
        QName qname = BindingReflections.findQName(type);
        if (previousQname != null && !(BindingReflections.isAugmentationChild(argument.getType()))) {
            qname = QName.create(previousQname, qname.getLocalName());
        }
        @SuppressWarnings({ "rawtypes", "unchecked" })
        ValueWithQName combinedInput = new ValueWithQName(previousQname, argument.getKey());
        @SuppressWarnings("unchecked")
        CompositeNode compositeOutput = keyCodec.serialize(combinedInput);
        for (Node<?> outputValue : compositeOutput.getValue()) {
            predicates.put(outputValue.getNodeType(), outputValue.getValue());
        }
        if (previousQname == null) {
            return new NodeIdentifierWithPredicates(qname, predicates);
        }
        return new NodeIdentifierWithPredicates(qname, predicates);
    }

    private org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument deserializePathArgument(
            final PathArgument argument, final List<QName> processedPath) {
        if (argument instanceof NodeIdentifier) {
            return deserializeNodeIdentifier((NodeIdentifier) argument, processedPath);
        } else if (argument instanceof NodeIdentifierWithPredicates) {
            return deserializeNodeIdentifierWithPrecicates((NodeIdentifierWithPredicates) argument, processedPath);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: "
                    + Arrays.<Object> asList(argument, processedPath).toString());
        }
    }

    private PathArgument serializePathArgumentAndUpdateMapping(final List<QName> parentPath, final InstanceIdentifier.PathArgument baArg, final QName previousQName, final Class<?> previousAugmentation) {
        org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument biArg = serializePathArgument(baArg, previousQName);
        List<QName> qnamePath = new ArrayList<>(parentPath);
        qnamePath.add(biArg.getNodeType());
        ImmutableList<QName> currentPath = ImmutableList.copyOf(qnamePath);
        codecRegistry.putPathToClass(currentPath, baArg.getType());
        if (previousAugmentation != null) {
            updateAugmentationInjection(baArg.getType(), currentPath, previousAugmentation);
        }
        return biArg;
    }

    private PathArgument serializePathArgument(
            final InstanceIdentifier.PathArgument argument,
            final QName previousQname) {
        if (argument instanceof IdentifiableItem) {
            return serializeIdentifiableItem((IdentifiableItem<?,?>) argument, previousQname);
        } else if (argument instanceof Item) {
            return serializeItem((Item<?>) argument, previousQname);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: "
                    + Arrays.<Object> asList(argument, previousQname).toString());
        }
    }



}
