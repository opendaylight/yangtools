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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opendaylight.yangtools.binding.data.codec.impl.NodeCodecContext.CodecContextFactory;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
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
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BindingCodecContext implements CodecContextFactory, Immutable {
    private static final Logger LOG = LoggerFactory.getLogger(BindingCodecContext.class);
    private static final String GETTER_PREFIX = "get";

    private final SchemaRootCodecContext root;
    private final BindingRuntimeContext context;
    private final Codec<YangInstanceIdentifier, InstanceIdentifier<?>> instanceIdentifierCodec =
            new InstanceIdentifierCodec();
    private final Codec<QName, Class<?>> identityCodec = new IdentityCodec();

    public BindingCodecContext(final BindingRuntimeContext context) {
        this.context = Preconditions.checkNotNull(context, "Binding Runtime Context is required.");
        this.root = SchemaRootCodecContext.create(this);
    }

    @Override
    public BindingRuntimeContext getRuntimeContext() {
        return context;
    }

    Codec<YangInstanceIdentifier, InstanceIdentifier<?>> getInstanceIdentifierCodec() {
        return instanceIdentifierCodec;
    }

    public Codec<QName, Class<?>> getIdentityCodec() {
        return identityCodec;
    }

    public Entry<YangInstanceIdentifier, BindingStreamEventWriter> newWriter(final InstanceIdentifier<?> path,
            final NormalizedNodeStreamWriter domWriter) {
        LinkedList<YangInstanceIdentifier.PathArgument> yangArgs = new LinkedList<>();
        DataContainerCodecContext<?> codecContext = getCodecContextNode(path, yangArgs);
        BindingStreamEventWriter writer = new BindingToNormalizedStreamWriter(codecContext, domWriter);
        return new SimpleEntry<>(YangInstanceIdentifier.create(yangArgs), writer);
    }

    public BindingStreamEventWriter newWriterWithoutIdentifier(final InstanceIdentifier<?> path,
            final NormalizedNodeStreamWriter domWriter) {
        return new BindingToNormalizedStreamWriter(getCodecContextNode(path, null), domWriter);
    }

    public DataContainerCodecContext<?> getCodecContextNode(final InstanceIdentifier<?> binding,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        DataContainerCodecContext<?> currentNode = root;
        for (InstanceIdentifier.PathArgument bindingArg : binding.getPathArguments()) {
            currentNode = currentNode.getIdentifierChild(bindingArg, builder);
        }
        return currentNode;
    }

    /**
     * Multi-purpose utility function. Traverse the codec tree, looking for
     * the appropriate codec for the specified {@link YangInstanceIdentifier}.
     * As a side-effect, gather all traversed binding {@link InstanceIdentifier.PathArgument}s
     * into the supplied collection.
     *
     * @param dom {@link YangInstanceIdentifier} which is to be translated
     * @param bindingArguments Collection for traversed path arguments
     * @return Codec for target node, or @null if the node does not have a
     *         binding representation (choice, case, leaf).
     */
    @Nullable NodeCodecContext getCodecContextNode(final @Nonnull YangInstanceIdentifier dom,
            final @Nonnull Collection<InstanceIdentifier.PathArgument> bindingArguments) {
        NodeCodecContext currentNode = root;
        ListNodeCodecContext currentList = null;

        for (YangInstanceIdentifier.PathArgument domArg : dom.getPathArguments()) {
            Preconditions.checkArgument(currentNode instanceof DataContainerCodecContext<?>, "Unexpected child of non-container node %s", currentNode);
            final DataContainerCodecContext<?> previous = (DataContainerCodecContext<?>) currentNode;
            final NodeCodecContext nextNode = previous.getYangIdentifierChild(domArg);

            /*
             * List representation in YANG Instance Identifier consists of two
             * arguments: first is list as a whole, second is list as an item so
             * if it is /list it means list as whole, if it is /list/list - it
             * is wildcarded and if it is /list/list[key] it is concrete item,
             * all this variations are expressed in Binding Aware Instance
             * Identifier as Item or IdentifiableItem
             */
            if (currentList != null) {
                Preconditions.checkArgument(currentList == nextNode, "List should be referenced two times in YANG Instance Identifier %s", dom);

                // We entered list, so now we have all information to emit
                // list path using second list argument.
                bindingArguments.add(currentList.getBindingPathArgument(domArg));
                currentList = null;
                currentNode = nextNode;
            } else if (nextNode instanceof ListNodeCodecContext) {
                // We enter list, we do not update current Node yet,
                // since we need to verify
                currentList = (ListNodeCodecContext) nextNode;
            } else if (nextNode instanceof ChoiceNodeCodecContext) {
                // We do not add path argument for choice, since
                // it is not supported by binding instance identifier.
                currentNode = nextNode;
            } else if (nextNode instanceof DataContainerCodecContext<?>) {
                bindingArguments.add(((DataContainerCodecContext<?>) nextNode).getBindingPathArgument(domArg));
                currentNode = nextNode;
            } else if (nextNode instanceof LeafNodeCodecContext) {
                LOG.debug("Instance identifier referencing a leaf is not representable (%s)", dom);
                return null;
            }
        }

        // Algorithm ended in list as whole representation
        // we sill need to emit identifier for list
        if (currentNode instanceof ChoiceNodeCodecContext) {
            LOG.debug("Instance identifier targeting a choice is not representable (%s)", dom);
            return null;
        }
        if (currentNode instanceof CaseNodeCodecContext) {
            LOG.debug("Instance identifier targeting a case is not representable (%s)", dom);
            return null;
        }

        if (currentList != null) {
            bindingArguments.add(currentList.getBindingPathArgument(null));
            return currentList;
        }
        return currentNode;
    }

    @Override
    public ImmutableMap<String, LeafNodeCodecContext> getLeafNodes(final Class<?> parentClass,
            final DataNodeContainer childSchema) {
        HashMap<String, DataSchemaNode> getterToLeafSchema = new HashMap<>();
        for (DataSchemaNode leaf : childSchema.getChildNodes()) {
            final TypeDefinition<?> typeDef;
            if (leaf instanceof LeafSchemaNode) {
                typeDef = ((LeafSchemaNode) leaf).getType();
            } else if (leaf instanceof LeafListSchemaNode) {
                typeDef = ((LeafListSchemaNode) leaf).getType();
            } else {
                continue;
            }

            String getterName = getGetterName(leaf.getQName(), typeDef);
            getterToLeafSchema.put(getterName, leaf);
        }
        return getLeafNodesUsingReflection(parentClass, getterToLeafSchema);
    }

    private String getGetterName(final QName qName, TypeDefinition<?> typeDef) {
        String suffix = BindingMapping.getClassName(qName);

        while (typeDef.getBaseType() != null) {
            typeDef = typeDef.getBaseType();
        }
        if (typeDef instanceof BooleanTypeDefinition) {
            return "is" + suffix;
        }
        return GETTER_PREFIX + suffix;
    }

    private ImmutableMap<String, LeafNodeCodecContext> getLeafNodesUsingReflection(final Class<?> parentClass,
            final Map<String, DataSchemaNode> getterToLeafSchema) {
        Map<String, LeafNodeCodecContext> leaves = new HashMap<>();
        for (Method method : parentClass.getMethods()) {
            if (method.getParameterTypes().length == 0) {
                DataSchemaNode schema = getterToLeafSchema.get(method.getName());
                final Class<?> valueType;
                if (schema instanceof LeafSchemaNode) {
                    valueType = method.getReturnType();
                } else if (schema instanceof LeafListSchemaNode) {
                    Type genericType = ClassLoaderUtils.getFirstGenericParameter(method.getGenericReturnType());

                    if (genericType instanceof Class<?>) {
                        valueType = (Class<?>) genericType;
                    } else if (genericType instanceof ParameterizedType) {
                        valueType = (Class<?>) ((ParameterizedType) genericType).getRawType();
                    } else {
                        throw new IllegalStateException("Unexpected return type " + genericType);
                    }
                } else {
                    continue; // We do not have schema for leaf, so we will ignore it (eg. getClass, getImplementedInterface).
                }
                Codec<Object, Object> codec = getCodec(valueType, schema);
                final LeafNodeCodecContext leafNode = new LeafNodeCodecContext(schema, codec, method);
                leaves.put(schema.getQName().getLocalName(), leafNode);
            }
        }
        return ImmutableMap.copyOf(leaves);
    }



    private Codec<Object, Object> getCodec(final Class<?> valueType, final DataSchemaNode schema) {
        if (Class.class.equals(valueType)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final Codec<Object, Object> casted = (Codec) identityCodec;
            return casted;
        } else if (InstanceIdentifier.class.equals(valueType)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final Codec<Object, Object> casted = (Codec) instanceIdentifierCodec;
            return casted;
        } else if (BindingReflections.isBindingClass(valueType)) {
            final TypeDefinition<?> instantiatedType;
            if (schema instanceof LeafSchemaNode) {
                instantiatedType = ((LeafSchemaNode) schema).getType();
            } else if (schema instanceof LeafListSchemaNode) {
                instantiatedType = ((LeafListSchemaNode) schema).getType();
            } else {
                instantiatedType = null;
            }
            if (instantiatedType != null) {
                return getCodec(valueType, instantiatedType);
            }
        }
        return ValueTypeCodec.NOOP_CODEC;
    }

    private Codec<Object, Object> getCodec(final Class<?> valueType, final TypeDefinition<?> instantiatedType) {
        @SuppressWarnings("rawtypes")
        TypeDefinition rootType = instantiatedType;
        while (rootType.getBaseType() != null) {
            rootType = rootType.getBaseType();
        }
        if (rootType instanceof IdentityrefTypeDefinition) {
            return ValueTypeCodec.encapsulatedValueCodecFor(valueType, identityCodec);
        } else if (rootType instanceof InstanceIdentifierTypeDefinition) {
            return ValueTypeCodec.encapsulatedValueCodecFor(valueType, instanceIdentifierCodec);
        } else if (rootType instanceof UnionTypeDefinition) {
            Callable<UnionTypeCodec> loader = UnionTypeCodec.loader(valueType, (UnionTypeDefinition) rootType);
            try {
                return loader.call();
            } catch (Exception e) {
                throw new IllegalStateException("Unable to load codec for " + valueType, e);
            }
        }
        return ValueTypeCodec.getCodecFor(valueType, instantiatedType);
    }

    private class InstanceIdentifierCodec implements Codec<YangInstanceIdentifier, InstanceIdentifier<?>> {

        @Override
        public YangInstanceIdentifier serialize(final InstanceIdentifier<?> input) {
            List<YangInstanceIdentifier.PathArgument> domArgs = new ArrayList<>();
            getCodecContextNode(input, domArgs);
            return YangInstanceIdentifier.create(domArgs);
        }

        @Override
        public InstanceIdentifier<?> deserialize(final YangInstanceIdentifier input) {
            final List<InstanceIdentifier.PathArgument> builder = new ArrayList<>();
            final NodeCodecContext codec = getCodecContextNode(input, builder);
            return codec == null ? null : InstanceIdentifier.create(builder);
        }
    }

    private class IdentityCodec implements Codec<QName, Class<?>> {

        @Override
        public Class<?> deserialize(final QName input) {
            Preconditions.checkArgument(input != null, "Input must not be null.");
            return context.getIdentityClass(input);
        }

        @Override
        public QName serialize(final Class<?> input) {
            Preconditions.checkArgument(BaseIdentity.class.isAssignableFrom(input));
            return BindingReflections.findQName(input);
        }
    }

    private static class ValueContext {

        Method getter;
        Codec<Object, Object> codec;

        public ValueContext(final Class<?> identifier, final LeafNodeCodecContext leaf) {
            final String getterName = GETTER_PREFIX
                    + BindingMapping.getClassName(leaf.getDomPathArgument().getNodeType());
            try {
                getter = identifier.getMethod(getterName);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new IllegalStateException(e);
            }
            codec = leaf.getValueCodec();
        }

        public Object getAndSerialize(final Object obj) {
            try {
                Object value = getter.invoke(obj);
                Preconditions.checkArgument(value != null,
                        "All keys must be specified for %s. Missing key is %s. Supplied key is %s",
                        getter.getDeclaringClass(), getter.getName(), obj);
                return codec.serialize(value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public Object deserialize(final Object obj) {
            return codec.deserialize(obj);
        }

    }

    private static class IdentifiableItemCodec implements Codec<NodeIdentifierWithPredicates, IdentifiableItem<?, ?>> {

        private final Map<QName, ValueContext> keyValueContexts;
        private final ListSchemaNode schema;
        private final Constructor<? extends Identifier<?>> constructor;
        private final Class<?> identifiable;

        public IdentifiableItemCodec(final ListSchemaNode schema, final Class<? extends Identifier<?>> keyClass,
                final Class<?> identifiable, final Map<QName, ValueContext> keyValueContexts) {
            this.schema = schema;
            this.identifiable = identifiable;
            this.constructor = getConstructor(keyClass);

            /*
             * We need to re-index to make sure we instantiate nodes in the order in which
             * they are defined.
             */
            final Map<QName, ValueContext> keys = new LinkedHashMap<>();
            for (QName qname : schema.getKeyDefinition()) {
                keys.put(qname, keyValueContexts.get(qname));
            }
            this.keyValueContexts = ImmutableMap.copyOf(keys);
        }

        @Override
        public IdentifiableItem<?, ?> deserialize(final NodeIdentifierWithPredicates input) {
            final Collection<QName> keys = schema.getKeyDefinition();
            final ArrayList<Object> bindingValues = new ArrayList<>(keys.size());
            for (QName key : keys) {
                Object yangValue = input.getKeyValues().get(key);
                bindingValues.add(keyValueContexts.get(key).deserialize(yangValue));
            }

            final Identifier<?> identifier;
            try {
                identifier = constructor.newInstance(bindingValues.toArray());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(String.format("Failed to instantiate key class %s", constructor.getDeclaringClass()), e);
            }

            @SuppressWarnings({ "rawtypes", "unchecked" })
            final IdentifiableItem identifiableItem = new IdentifiableItem(identifiable, identifier);
            return identifiableItem;
        }

        @Override
        public NodeIdentifierWithPredicates serialize(final IdentifiableItem<?, ?> input) {
            Object value = input.getKey();

            Map<QName, Object> values = new LinkedHashMap<>();
            for (Entry<QName, ValueContext> valueCtx : keyValueContexts.entrySet()) {
                values.put(valueCtx.getKey(), valueCtx.getValue().getAndSerialize(value));
            }
            return new NodeIdentifierWithPredicates(schema.getQName(), values);
        }
    }

    @SuppressWarnings("unchecked")
    private static Constructor<? extends Identifier<?>> getConstructor(final Class<? extends Identifier<?>> clazz) {
        for (@SuppressWarnings("rawtypes") Constructor constr : clazz.getConstructors()) {
            Class<?>[] parameters = constr.getParameterTypes();
            if (!clazz.equals(parameters[0])) {
                // It is not copy constructor;
                return constr;
            }
        }
        throw new IllegalArgumentException("Supplied class " + clazz + "does not have required constructor.");
    }

    @Override
    public Codec<NodeIdentifierWithPredicates, IdentifiableItem<?, ?>> getPathArgumentCodec(final Class<?> listClz,
            final ListSchemaNode schema) {
        Class<? extends Identifier<?>> identifier = ClassLoaderUtils.findFirstGenericArgument(listClz,
                Identifiable.class);
        Map<QName, ValueContext> valueCtx = new HashMap<>();
        for (LeafNodeCodecContext leaf : getLeafNodes(identifier, schema).values()) {
            QName name = leaf.getDomPathArgument().getNodeType();
            valueCtx.put(name, new ValueContext(identifier, leaf));
        }
        return new IdentifiableItemCodec(schema, identifier, listClz, valueCtx);
    }

}
