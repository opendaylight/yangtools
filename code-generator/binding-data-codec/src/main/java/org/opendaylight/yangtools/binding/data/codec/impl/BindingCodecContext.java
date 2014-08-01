package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.binding.data.codec.api.NodeIdentifierWithPredicatesCodec;
import org.opendaylight.yangtools.binding.data.codec.impl.CodecContextNode.CodecContextFactory;
import org.opendaylight.yangtools.binding.data.codec.impl.CodecContextNode.DataContainerNode;
import org.opendaylight.yangtools.binding.data.codec.impl.CodecContextNode.LeafNode;
import org.opendaylight.yangtools.binding.data.codec.impl.CodecContextNode.NoopSerializeLeafNode;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.CodecRegistry;
import org.opendaylight.yangtools.yang.data.impl.codec.IdentifierCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.ValueWithQName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

public class BindingCodecContext implements CodecContextFactory, Immutable {

    private static final String GETTER_PREFIX = "get";
    private final CodecRegistry legacyRegistry;
    private final CodecContextNode.RootNode root;
    private final BindingRuntimeContext context;

    public BindingCodecContext(final BindingRuntimeContext context,final CodecRegistry legacyRegistry) {
        this.legacyRegistry = legacyRegistry;
        this.context = context;
        this.root = CodecContextNode.root(this);
    }


    public Entry<YangInstanceIdentifier,BindingStreamEventWriter> newWriter(final InstanceIdentifier<?> path, final NormalizedNodeStreamWriter domWriter) {
        LinkedList<YangInstanceIdentifier.PathArgument> yangArgs = new LinkedList<>();
        DataContainerNode<?> codecContext = getCodecContext(path, yangArgs);
        BindingStreamEventWriter writer = new BindingToNormalizedStreamWriter(codecContext, domWriter);
        return new SimpleEntry<>(YangInstanceIdentifier.create(yangArgs),writer);
    }

    public BindingStreamEventWriter newWriterWithoutIdentifier(final InstanceIdentifier<?> path, final NormalizedNodeStreamWriter domWriter) {
        return new BindingToNormalizedStreamWriter(getCodecContext(path,null), domWriter);
    }


    public DataContainerNode<?> getCodecContext(final InstanceIdentifier<?> binding,final List<YangInstanceIdentifier.PathArgument> builder) {
        DataContainerNode<?> currentNode = root;
        for(InstanceIdentifier.PathArgument bindingArg : binding.getPathArguments()) {
            currentNode = currentNode.getIdentifierChild(bindingArg,builder);
        }
        return currentNode;
    }


    @Override
    public NodeIdentifierWithPredicatesCodec getPathArgumentCodec(@SuppressWarnings("rawtypes") final Class identifiable) {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        final IdentifierCodec codec = legacyRegistry.getIdentifierCodecForIdentifiable(identifiable);
        return new NodeIdentifierWithPredicatesCodec() {

            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public YangInstanceIdentifier.NodeIdentifierWithPredicates serialize(final QName parentQName,
                    final Identifier key) {
                ValueWithQName codecInput = new ValueWithQName(parentQName, key);
                CompositeNode legacyVersion = codec.serialize(codecInput);
                Map<QName, Object> keyValues = new HashMap<>();
                for(Node<?> child : legacyVersion.getValue()) {
                    SimpleNode<?> casted = (SimpleNode<?>) child;
                    keyValues.put(casted.getNodeType(), casted.getValue());
                }
                return new NodeIdentifierWithPredicates(parentQName, keyValues);
            }
        };
    }


    @Override
    public BindingRuntimeContext getRuntimeContext() {
        return context;
    }

    @Override
    public ImmutableMap<String, LeafNode> getLeafNodes(final Class<?> parentClass, final DataNodeContainer childSchema) {
        HashMap<String, DataSchemaNode> getterToLeafSchema = new HashMap<>();
        for(DataSchemaNode leaf : childSchema.getChildNodes()) {
            if(leaf instanceof LeafSchemaNode || leaf instanceof LeafListSchemaNode) {
                String getterName = GETTER_PREFIX + BindingMapping.getClassName(leaf.getQName());
                getterToLeafSchema.put(getterName, leaf);
            }
        }
        return getLeafNodesUsingReflection(parentClass,getterToLeafSchema);
    }

    private ImmutableMap<String, LeafNode> getLeafNodesUsingReflection(final Class<?> parentClass,
            final Map<String, DataSchemaNode> getterToLeafSchema) {
        Map<String, LeafNode> leaves = new HashMap<>();
        for(Method method : parentClass.getMethods()) {
            if(method.getParameterTypes().length == 0) {
                DataSchemaNode schema = getterToLeafSchema.get(method.getName());
                final LeafNode leafNode;
                if(schema instanceof LeafSchemaNode) {
                    leafNode = leafNodeFrom(method.getReturnType(),schema);

                } else  {
                    // FIXME: extract inner list value
                    leafNode = null;
                }
                if(leafNode != null) {
                    leaves.put(schema.getQName().getLocalName(), leafNode);
                }
            }

        }
        return ImmutableMap.copyOf(leaves);
    }

    private LeafNode leafNodeFrom(final Class<?> returnType, final DataSchemaNode schema) {
        if(InstanceIdentifier.class.equals(returnType)) {
            return leafNodeWithInstanceIdentifierCodec(schema);
        }

        if(BindingReflections.isBindingClass(returnType)) {
            return leafNodeWithCodec(returnType,schema);
        }
        return new NoopSerializeLeafNode(schema);
    }

    private LeafNode leafNodeWithInstanceIdentifierCodec(final DataSchemaNode schema) {
        // FIXME Add proper loading of codec
        return new NoopSerializeLeafNode(schema);
    }

    private LeafNode leafNodeWithCodec(final Class<?> returnType, final DataSchemaNode schema) {
        // FIXME Add proper loading of codec
        return new NoopSerializeLeafNode(schema);
    }


}
