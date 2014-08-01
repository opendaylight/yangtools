/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.binding.data.codec.impl.CodecContextNode.CodecContextFactory;
import org.opendaylight.yangtools.binding.data.codec.impl.CodecContextNode.DataContainerNode;
import org.opendaylight.yangtools.binding.data.codec.impl.CodecContextNode.LeafNode;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

public class BindingCodecContext implements CodecContextFactory, Immutable {

    private static final String GETTER_PREFIX = "get";
    private final CodecContextNode.RootNode root;
    private final BindingRuntimeContext context;
    private final Codec<YangInstanceIdentifier,InstanceIdentifier<?>> instanceIdentifierCodec;

    public BindingCodecContext(final BindingRuntimeContext context) {
        this.context = context;
        this.root = CodecContextNode.root(this);
        this.instanceIdentifierCodec = new InstanceIdentifierCodec();
    }

    @Override
    public BindingRuntimeContext getRuntimeContext() {
        return context;
    }

    public Codec<YangInstanceIdentifier,InstanceIdentifier<?>> getInstanceIdentifierCodec() {
        return instanceIdentifierCodec;
    }

    public Entry<YangInstanceIdentifier, BindingStreamEventWriter> newWriter(final InstanceIdentifier<?> path,
            final NormalizedNodeStreamWriter domWriter) {
        LinkedList<YangInstanceIdentifier.PathArgument> yangArgs = new LinkedList<>();
        DataContainerNode<?> codecContext = getCodecContextNode(path, yangArgs);
        BindingStreamEventWriter writer = new BindingToNormalizedStreamWriter(codecContext, domWriter);
        return new SimpleEntry<>(YangInstanceIdentifier.create(yangArgs), writer);
    }

    public BindingStreamEventWriter newWriterWithoutIdentifier(final InstanceIdentifier<?> path,
            final NormalizedNodeStreamWriter domWriter) {
        return new BindingToNormalizedStreamWriter(getCodecContextNode(path, null), domWriter);
    }

    public DataContainerNode<?> getCodecContextNode(final InstanceIdentifier<?> binding,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        DataContainerNode<?> currentNode = root;
        for (InstanceIdentifier.PathArgument bindingArg : binding.getPathArguments()) {
            currentNode = currentNode.getIdentifierChild(bindingArg, builder);
        }
        return currentNode;
    }

    public CodecContextNode getCodecContextNode(final YangInstanceIdentifier dom,
            final List<InstanceIdentifier.PathArgument> builder) {
        CodecContextNode currentNode = root;
        CodecContextNode.ListNode currentList = null;
        for (YangInstanceIdentifier.PathArgument domArg : dom.getPathArguments()) {
            Preconditions.checkArgument(currentNode instanceof DataContainerNode<?>);
            DataContainerNode<?> previous = (DataContainerNode<?>) currentNode;
            CodecContextNode nextNode = previous.getYangIdentifierChild(domArg);
            /*
             * List representation in YANG Instance Identifier consists of two
             * arguments: first is list as a whole, second is list as an item so
             * if it is /list it means list as whole, if it is /list/list - it
             * is wildcarded and if it is /list/list[key] it is concrete item,
             * all this variations are expressed in Binding Aware Instance
             * Identifier as Item or IdentifiableItem
             */
            if (currentList != null) {

                if (currentList == nextNode) {

                    // We entered list, so now we have all information to emit
                    // list
                    // path using second list argument.
                    builder.add(currentList.getBindingPathArgument(domArg));
                    currentList = null;
                    currentNode = nextNode;
                } else {
                    throw new IllegalArgumentException(
                            "List should be referenced two times in YANG Instance Identifier");
                }
            } else if (nextNode instanceof CodecContextNode.ListNode) {
                // We enter list, we do not update current Node yet,
                // since we need to verify
                currentList = (CodecContextNode.ListNode) nextNode;
            } else if (nextNode instanceof CodecContextNode.ChoiceContextNode) {
                // We do not add path argument for choice, since
                // it is not supported by binding instance identifier.
                currentNode = nextNode;
            }else if (nextNode instanceof DataContainerNode<?>) {
                builder.add(((DataContainerNode<?>) nextNode).getBindingPathArgument(domArg));
                currentNode = nextNode;
            }
        }
        // Algorithm ended in list as whole representation
        // we sill need to emit identifier for list
        if (currentList != null) {
            builder.add(currentList.getBindingPathArgument(null));
            return currentList;
        }
        return currentNode;
    }

    @Override
    public ImmutableMap<String, LeafNode> getLeafNodes(final Class<?> parentClass, final DataNodeContainer childSchema) {
        HashMap<String, DataSchemaNode> getterToLeafSchema = new HashMap<>();
        for (DataSchemaNode leaf : childSchema.getChildNodes()) {
            if (leaf instanceof LeafSchemaNode || leaf instanceof LeafListSchemaNode) {
                String getterName = GETTER_PREFIX + BindingMapping.getClassName(leaf.getQName());
                getterToLeafSchema.put(getterName, leaf);
            }
        }
        return getLeafNodesUsingReflection(parentClass, getterToLeafSchema);
    }



    private ImmutableMap<String, LeafNode> getLeafNodesUsingReflection(final Class<?> parentClass,
            final Map<String, DataSchemaNode> getterToLeafSchema) {
        Map<String, LeafNode> leaves = new HashMap<>();
        for (Method method : parentClass.getMethods()) {
            if (method.getParameterTypes().length == 0) {
                DataSchemaNode schema = getterToLeafSchema.get(method.getName());
                final LeafNode leafNode;
                if (schema instanceof LeafSchemaNode) {
                    leafNode = leafNodeFrom(method.getReturnType(), schema);

                } else {
                    // FIXME: extract inner list value
                    leafNode = null;
                }
                if (leafNode != null) {
                    leaves.put(schema.getQName().getLocalName(), leafNode);
                }
            }
        }
        return ImmutableMap.copyOf(leaves);
    }


    private LeafNode leafNodeFrom(final Class<?> returnType, final DataSchemaNode schema) {
        return new LeafNode(schema, getCodec(returnType,schema));
    }

    private Codec<Object, Object> getCodec(final Class<?> returnType, final DataSchemaNode schema) {

        if(InstanceIdentifier.class.equals(returnType)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final Codec<Object,Object>casted = (Codec) instanceIdentifierCodec;
            return casted;
        } else if(BindingReflections.isBindingClass(returnType)) {
            final TypeDefinition<?> instantiatedType;
            if(schema instanceof LeafSchemaNode) {
                instantiatedType = ((LeafSchemaNode) schema).getType();
            } else if(schema instanceof LeafListSchemaNode) {
                instantiatedType = ((LeafListSchemaNode) schema).getType();
            } else {
                instantiatedType = null;
            }
            if(instantiatedType != null) {
                return getCodec(returnType,instantiatedType);
            }
        }
        return ValueTypeCodec.NOOP_CODEC;
    }

    private Codec<Object, Object> getCodec(final Class<?> returnType, final TypeDefinition<?> instantiatedType) {
        if(Enum.class.isAssignableFrom(returnType)) {
            throw new UnsupportedOperationException("Not implemented yet.");
        }
        return new ValueTypeCodec.EncapsulatedValueCodec(returnType);
    }

    private class InstanceIdentifierCodec implements Codec<YangInstanceIdentifier,InstanceIdentifier<?>> {

        @Override
        public YangInstanceIdentifier serialize(final InstanceIdentifier<?> input) {
            List<YangInstanceIdentifier.PathArgument> domArgs = new LinkedList<>();
            getCodecContextNode(input, domArgs);
            return YangInstanceIdentifier.create(domArgs);
        }

        @Override
        public InstanceIdentifier<?> deserialize(final YangInstanceIdentifier input) {
            List<InstanceIdentifier.PathArgument> builder = new LinkedList<>();
            getCodecContextNode(input, builder);
            return InstanceIdentifier.create(builder);
        }
    }

    private static class ValueContext {

        Method getter;
        Codec<Object,Object> codec;

        public ValueContext(final Class<?> identifier, final LeafNode leaf) {
            final String getterName = GETTER_PREFIX + BindingMapping.getClassName(leaf.getDomPathArgument().getNodeType());
            try {
                getter =identifier.getMethod(getterName);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new IllegalStateException(e);
            }
            codec = leaf.getValueCodec();
        }

        public Object getAndSerialize(final Object obj) {
            try {
                Object value = getter.invoke(obj);
                return codec.serialize(value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public Object deserialize(final Object obj) {
            return codec.deserialize(obj);
        }

    }

    private class IdentifiableItemCodec implements Codec<NodeIdentifierWithPredicates, IdentifiableItem<?, ?>> {

        private final Class<? extends Identifier<?>> keyClass;
        private final ImmutableMap<QName, ValueContext> keyValueContexts;
        private final QName name;
        private final Constructor<? extends Identifier<?>> constructor;
        private final Class<?> identifiable;

        public IdentifiableItemCodec(final QName name,final Class<? extends Identifier<?>> keyClass,final Class<?> identifiable,final Map<QName, ValueContext> keyValueContexts) {
            this.name = name;
            this.identifiable = identifiable;
            this.keyClass = keyClass;
            this.keyValueContexts = ImmutableMap.copyOf(keyValueContexts);
            this.constructor = getConstructor(keyClass);
        }

        @Override
        public IdentifiableItem<?,?> deserialize(final NodeIdentifierWithPredicates input) {
            ArrayList<Object> bindingValues = new ArrayList<>();
            for(Entry<QName, Object> yangEntry : input.getKeyValues().entrySet()) {
                QName yangName = yangEntry.getKey();
                Object yangValue = yangEntry.getValue();
                bindingValues.add(keyValueContexts.get(yangName).deserialize(yangValue));
            }
            try {
                Identifier<?> identifier = constructor.newInstance(bindingValues.toArray());
                return new IdentifiableItem(identifiable, identifier);
            } catch (InstantiationException | IllegalAccessException
                    | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public NodeIdentifierWithPredicates serialize(final IdentifiableItem<?, ?> input) {
            Object value = input.getKey();

            Map<QName,Object> values = new HashMap<>();
            for(Entry<QName, ValueContext> valueCtx : keyValueContexts.entrySet()) {
                values.put(valueCtx.getKey(), valueCtx.getValue().getAndSerialize(value));
            }
            return new NodeIdentifierWithPredicates(name, values);
        }

    }

    private static Constructor<? extends Identifier<?>> getConstructor(final Class<? extends Identifier<?>> clazz) {
        for(Constructor constr : clazz.getConstructors()) {
            Class<?>[] parameters = constr.getParameterTypes();
            if(!clazz.equals(parameters[0])) {
                // It is not copy constructor;
                return constr;
            }
        }
        throw new IllegalArgumentException("Supplied class " + clazz +"does not have required constructor.");
    }


    @Override
    public Codec<NodeIdentifierWithPredicates, IdentifiableItem<?, ?>> getPathArgumentCodec(final Class<?> listClz,
            final ListSchemaNode schema) {
        Class<? extends Identifier<?>> identifier =ClassLoaderUtils.findFirstGenericArgument(listClz, Identifiable.class);
        Map<QName, ValueContext> valueCtx = new HashMap<>();
        for(LeafNode leaf : getLeafNodes(identifier, schema).values()) {
            QName name = leaf.getDomPathArgument().getNodeType();
            valueCtx.put(name, new ValueContext(identifier,leaf));
        }
        return new IdentifiableItemCodec(schema.getQName(), identifier, listClz, valueCtx);
    }

}
