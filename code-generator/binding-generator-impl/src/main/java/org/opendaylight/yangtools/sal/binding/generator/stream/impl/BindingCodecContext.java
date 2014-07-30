package org.opendaylight.yangtools.sal.binding.generator.stream.impl;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleContext;
import org.opendaylight.yangtools.sal.binding.generator.stream.impl.CodecContextNode.BindingMetadataLoader;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
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
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.AugmentationSchemaProxy;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class BindingCodecContext implements BindingMetadataLoader {

    private final Map<Type,AugmentationSchema> augmentationToSchema = new HashMap<>();
    private final CodecRegistry legacyRegistry;
    private final CodecContextNode root;
    private final Map<Type, Object> typeToDefiningSchema = new HashMap<>();


    public BindingCodecContext(final SchemaContext schemaCtx,final Iterable<ModuleContext> bindingCtx,final CodecRegistry legacyRegistry) {
        this.legacyRegistry = legacyRegistry;
        for(ModuleContext ctx : bindingCtx) {
            augmentationToSchema.putAll(ctx.getTypeToAugmentation());
            typeToDefiningSchema.putAll(ctx.getTypeToSchema());
        }
        this.root = CodecContextNode.root(schemaCtx,this);
    }

    public AugmentationSchema getAugmentationSchema(final DataNodeContainer target,final Class<? extends Augmentation<?>> aug) {
        ReferencedTypeImpl reference = new ReferencedTypeImpl(aug.getPackage().getName(), aug.getSimpleName());
        AugmentationSchema origSchema = augmentationToSchema.get(reference);
        Preconditions.checkArgument(origSchema != null, "Supplied augmentation %s in not valid in current schema context",aug);
        // FIXME: Add real lookup to find schema

        Set<DataSchemaNode> realChilds = new HashSet<>();
        for(DataSchemaNode child : origSchema.getChildNodes()) {
            realChilds.add(target.getDataChildByName(child.getQName()));
        }
        return new AugmentationSchemaProxy(origSchema, realChilds );
    }

    public BindingStreamEventWriter newWriter(final InstanceIdentifier<?> path, final NormalizedNodeStreamWriter domWriter) {
        return new BindingToNormalizedStreamWriter(getCodecContext(path), domWriter);
    }

    CodecContextNode getCodecContext(final InstanceIdentifier<?> path) {
        CodecContextNode current = root;
        for(InstanceIdentifier.PathArgument arg : path.getPathArguments()) {
            final CodecContextNode next = ((CodecContextNode.DataContainerNode) current).getChild(arg);
            Preconditions.checkArgument(next != null,"Node %s does not have child %s",current,arg);
            current = next;
        }
        return current;
    }

    @Override
    public AugmentationSchema getAugmentationDefinition(final Class<?> childClass) {
        ReferencedTypeImpl ref = new ReferencedTypeImpl(childClass.getPackage().getName(), childClass.getSimpleName());
        return augmentationToSchema.get(ref);
    }

    @Override
    public DataSchemaNode getSchemaDefinition(final Class<?> childClass) {
        ReferencedTypeImpl ref = new ReferencedTypeImpl(childClass.getPackage().getName(), childClass.getSimpleName());
        return (DataSchemaNode) typeToDefiningSchema.get(ref);
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

}
