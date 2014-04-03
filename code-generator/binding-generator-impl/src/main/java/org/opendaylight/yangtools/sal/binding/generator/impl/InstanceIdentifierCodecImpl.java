/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.sal.binding.generator.impl.CodecTypeUtils;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifier;
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

    private final Map<Class<?>, Map<List<QName>, Class<?>>> classToPreviousAugment = Collections
            .synchronizedMap(new WeakHashMap<Class<?>, Map<List<QName>, Class<?>>>());

    public InstanceIdentifierCodecImpl(final CodecRegistry registry) {
        this.codecRegistry = registry;
    }

    @Override
    public InstanceIdentifier<? extends Object> deserialize(
            org.opendaylight.yangtools.yang.data.api.InstanceIdentifier input) {
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
                Class<? extends DataObject> augment = (Class<? extends DataObject>) injectAugment.get(scannedPath);
                if (augment != null) {
                    baArgs.add(new Item(augment));
                }
            }
            baArgs.add(baArg);
        }
        InstanceIdentifier ret = InstanceIdentifier.create(baArgs);
        LOG.debug("DOM Instance Identifier {} deserialized to {}", input, ret);
        return ret;
    }

    @Override
    public InstanceIdentifier<? extends Object> deserialize(
            final org.opendaylight.yangtools.yang.data.api.InstanceIdentifier input,
            InstanceIdentifier<?> bindingIdentifier) {
        return deserialize(input);
    }

    private org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument _deserializePathArgument(
            final NodeIdentifier argument, final List<QName> processedPath) {
        final Class cls = codecRegistry.getClassForPath(processedPath);
        Item<DataObject> item = new Item<>(cls);
        return item;
    }

    private org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument _deserializePathArgument(
            final NodeIdentifierWithPredicates argument, final List<QName> processedPath) {
        final Class type = codecRegistry.getClassForPath(processedPath);
        final IdentifierCodec codec = codecRegistry
                .<Identifiable<? extends Object>> getIdentifierCodecForIdentifiable(type);
        CompositeNode _compositeNode = this.toCompositeNode(argument);
        ValueWithQName<CompositeNode> deserialize = codec.deserialize(_compositeNode);
        Object value = null;
        if (deserialize != null) {
            value = deserialize.getValue();
        }
        return CodecTypeUtils.newIdentifiableItem(type, value);
    }

    public CompositeNode toCompositeNode(NodeIdentifierWithPredicates predicates) {
        Set<Map.Entry<QName, Object>> keyValues = predicates.getKeyValues().entrySet();
        List<Node<?>> values = new ArrayList<>(keyValues.size());
        for (Map.Entry<QName, Object> keyValue : keyValues) {
            values.add(new SimpleNodeTOImpl<Object>(keyValue.getKey(), null, keyValue.getValue()));
        }
        return new CompositeNodeTOImpl(predicates.getNodeType(), null, values);
    }

    @Override
    public org.opendaylight.yangtools.yang.data.api.InstanceIdentifier serialize(InstanceIdentifier<?> input) {
        Class<?> previousAugmentation = null;
        List<InstanceIdentifier.PathArgument> pathArgs = input.getPath();
        QName previousQName = null;
        List<PathArgument> components = new ArrayList<>(pathArgs.size());
        List<QName> qnamePath = new ArrayList<>(pathArgs.size());
        for (InstanceIdentifier.PathArgument baArg : pathArgs) {

            if (!Augmentation.class.isAssignableFrom(baArg.getType())) {
                org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument biArg = serializePathArgument(
                        baArg, previousQName);
                previousQName = biArg.getNodeType();
                components.add(biArg);
                qnamePath.add(biArg.getNodeType());
                ImmutableList<QName> immutableList = ImmutableList.copyOf(qnamePath);
                codecRegistry.putPathToClass(immutableList, baArg.getType());
                if (previousAugmentation != null) {
                    updateAugmentationInjection(baArg.getType(), immutableList, previousAugmentation);
                }
                previousAugmentation = null;
            } else {
                previousQName = codecRegistry.getQNameForAugmentation(baArg.getType());
                previousAugmentation = baArg.getType();
            }
        }
        org.opendaylight.yangtools.yang.data.api.InstanceIdentifier ret = new org.opendaylight.yangtools.yang.data.api.InstanceIdentifier(
                components);
        LOG.debug("Binding Instance Identifier {} serialized to DOM InstanceIdentifier {}", input, ret);
        return ret;
    }

    public Class<? extends Object> updateAugmentationInjection(Class<? extends DataObject> class1,
            ImmutableList<QName> list, Class<?> augmentation) {
        if (classToPreviousAugment.get(class1) == null) {
            classToPreviousAugment.put(class1, new ConcurrentHashMap<List<QName>, Class<?>>());
        }
        return classToPreviousAugment.get(class1).put(list, augmentation);
    }

    private PathArgument _serializePathArgument(Item<?> argument, QName previousQname) {
        Class<?> type = argument.getType();
        QName qname = BindingReflections.findQName(type);
        if (previousQname == null || (BindingReflections.isAugmentationChild(argument.getType()))) {
            return new NodeIdentifier(qname);
        }
        return new NodeIdentifier(QName.create(previousQname, qname.getLocalName()));
    }

    private PathArgument _serializePathArgument(IdentifiableItem argument, QName previousQname) {
        Map<QName, Object> predicates = new HashMap<>();
        Class type = argument.getType();
        IdentifierCodec<? extends Object> keyCodec = codecRegistry.getIdentifierCodecForIdentifiable(type);
        QName qname = BindingReflections.findQName(type);
        if (previousQname != null && !(BindingReflections.isAugmentationChild(argument.getType()))) {
            qname = QName.create(previousQname, qname.getLocalName());
        }
        ValueWithQName combinedInput = new ValueWithQName(previousQname, argument.getKey());
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
            return _deserializePathArgument((NodeIdentifier) argument, processedPath);
        } else if (argument instanceof NodeIdentifierWithPredicates) {
            return _deserializePathArgument((NodeIdentifierWithPredicates) argument, processedPath);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: "
                    + Arrays.<Object> asList(argument, processedPath).toString());
        }
    }

    private PathArgument serializePathArgument(
            final org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument argument,
            final QName previousQname) {
        if (argument instanceof IdentifiableItem) {
            return _serializePathArgument((IdentifiableItem) argument, previousQname);
        } else if (argument instanceof Item) {
            return _serializePathArgument((Item<?>) argument, previousQname);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: "
                    + Arrays.<Object> asList(argument, previousQname).toString());
        }
    }

}
