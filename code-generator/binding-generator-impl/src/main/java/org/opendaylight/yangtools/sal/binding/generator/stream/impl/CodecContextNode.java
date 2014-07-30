package org.opendaylight.yangtools.sal.binding.generator.stream.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingSchemaContextUtils;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.binding.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.AugmentationSchemaProxy;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;

abstract class CodecContextNode {

    public abstract Optional<YangInstanceIdentifier.PathArgument> getDomPathArgument();

    public static CodecContextNode root(final SchemaContext schemaCtx, final BindingMetadataLoader loader) {
        return new RootNode(schemaCtx, loader);
    }

    public interface BindingMetadataLoader {

        AugmentationSchema getAugmentationDefinition(Class<?> childClass);

        DataSchemaNode getSchemaDefinition(Class<?> childClass);

        NodeIdentifierWithPredicatesCodec getPathArgumentCodec(Class<?> identifiable);

    }

    static class LeafNode extends CodecContextNode {

        private final Optional<YangInstanceIdentifier.PathArgument> yangIdentifier;

        LeafNode(final DataSchemaNode node) {
            yangIdentifier = Optional
                    .<YangInstanceIdentifier.PathArgument> of(new YangInstanceIdentifier.NodeIdentifier(node.getQName()));
        }

        @Override
        public Optional<PathArgument> getDomPathArgument() {
            return yangIdentifier;
        }

        public Object serializeValue(final Object value) {
            return value;
        }

    }

    public abstract static class DataContainerNode<T> extends CodecContextNode {

        protected final T schema;
        protected final QNameModule namespace;
        protected final BindingMetadataLoader metadataLoader;
        protected final Class<?> bindingClass;


        protected final LoadingCache<Class<?>, CodecContextNode> containerChild;

        public DataContainerNode(final Class<?> cls, final QNameModule namespace, final T nodeSchema,
                final BindingMetadataLoader loader) {
            super();
            this.schema = nodeSchema;
            this.metadataLoader = loader;
            this.namespace = namespace;
            this.bindingClass = cls;


            this.containerChild = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, CodecContextNode>() {
                @Override
                public CodecContextNode load(final Class<?> key) throws Exception {
                    return loadChild(key);
                }
            });
        }

        public T getSchema() {
            return schema;
        }



        public final CodecContextNode getChild(final InstanceIdentifier.PathArgument arg) {
            return getChild(arg.getType());
        }

        public CodecContextNode getChild(final Class<?> childClass) {
            return containerChild.getUnchecked(childClass);
        }

        abstract CodecContextNode loadChild(final Class<?> childClass);

        @Override
        public String toString() {
            return getClass().getSimpleName() + " [" + bindingClass + "]";
        }

    }

    private static CodecContextNode from(final Class<?> cls, final DataSchemaNode schema,
            final BindingMetadataLoader loader) {
        if (schema instanceof ContainerSchemaNode) {
            return new ContainerNode(cls, (ContainerSchemaNode) schema, loader);
        } else if (schema instanceof ListSchemaNode) {
            return new ListNode(cls, (ListSchemaNode) schema, loader);
        } else if (schema instanceof ChoiceNode) {
            return new ChoiceContextNode(cls, (ChoiceNode) schema, loader);
        }
        throw new IllegalArgumentException("Not supported type " + cls + " " + schema);
    }

    public abstract static class DataChildContainerNode<T extends DataNodeContainer> extends DataContainerNode<T> {

        protected final T schema;
        protected final LoadingCache<String, LeafNode> leafChild;

        public DataChildContainerNode(final Class<?> cls, final QNameModule namespace, final T nodeSchema,
                final BindingMetadataLoader loader) {
            super(cls, namespace, nodeSchema, loader);
            this.schema = nodeSchema;

            this.leafChild = CacheBuilder.newBuilder().build(new CacheLoader<String, LeafNode>() {
                @Override
                public LeafNode load(final String key) throws Exception {
                    return loadLeaf(key);
                }
            });

        }

        @Override
        public T getSchema() {
            return schema;
        }


        public final LeafNode getLeafChild(final String name) {
            return leafChild.getUnchecked(name);
        }

        public LeafNode loadLeaf(final String localName) {
            QName childName = QName.create(namespace, localName);
            DataSchemaNode childSchema = schema.getDataChildByName(childName);
            Preconditions.checkArgument(childSchema != null, "Node %s does not have leaf child named %s", schema,
                    localName);
            return new LeafNode(childSchema);
        }

        @Override
        CodecContextNode loadChild(final Class<?> childClass) {
            if (Augmentation.class.isAssignableFrom(childClass)) {
                return loadAugmentation(childClass);
            } else {

                DataSchemaNode origDef = metadataLoader.getSchemaDefinition(childClass);
                // Direct instantiation or use in same module in which grouping
                // was defined.
                DataSchemaNode sameName = schema.getDataChildByName(origDef.getQName());
                final DataSchemaNode childSchema;
                if (sameName != null) {
                    // Exactly same schema node
                    if (origDef.equals(sameName)) {
                        childSchema = sameName;
                        // We check if instantiated node was added via uses
                        // statement
                        // and is instatiation of same grouping
                    } else if (origDef.equals(SchemaNodeUtils.getRootOriginalIfPossible(sameName))) {
                        childSchema = sameName;
                    } else {
                        // Node has same name, but clearly is different
                        childSchema = null;
                    }
                } else {
                    // We are looking for instantiation via uses in other module
                    QName instantiedName = QName.create(namespace, origDef.getQName().getLocalName());
                    DataSchemaNode potential = schema.getDataChildByName(instantiedName);
                    // We check if it is really instantiated from same
                    // definition
                    // as class was derived
                    if (potential != null && origDef.equals(SchemaNodeUtils.getRootOriginalIfPossible(potential))) {
                        childSchema = potential;
                    } else {
                        childSchema = null;
                    }
                }
                Preconditions.checkArgument(childSchema != null, "Node %s does not have child named %s", schema,
                        childClass);
                return CodecContextNode.from(childClass, childSchema, metadataLoader);
            }
        }

        private CodecContextNode loadAugmentation(final Class<?> childClass) {
            Preconditions.checkArgument(schema instanceof AugmentationTarget);
            AugmentationSchema schemaDefinition = metadataLoader.getAugmentationDefinition(childClass);

            Set<QName> childNames = new HashSet<>();
            Set<DataSchemaNode> childNodes = new HashSet<>();
            for (DataSchemaNode child : schemaDefinition.getChildNodes()) {
                QName name = child.getQName();
                childNames.add(name);
                childNodes.add(schema.getDataChildByName(name));
            }
            AugmentationIdentifier identifier = new AugmentationIdentifier(childNames);
            QNameModule namespace = Iterables.getFirst(childNames, null).getModule();
            AugmentationSchemaProxy currentAugSchema = new AugmentationSchemaProxy(schemaDefinition, childNodes);
            return new AugmentationNode(childClass, namespace, identifier, currentAugSchema, metadataLoader);
        }

    }

    public static class AugmentationNode extends DataChildContainerNode<AugmentationSchema> {

        private final Optional<PathArgument> yangIdentifier;

        public AugmentationNode(final Class<?> cls, final QNameModule namespace,
                final AugmentationIdentifier identifier, final AugmentationSchema nodeSchema,
                final BindingMetadataLoader loader) {
            super(cls, namespace, nodeSchema, loader);
            this.yangIdentifier = Optional.<YangInstanceIdentifier.PathArgument> of(identifier);
        }

        @Override
        public Optional<PathArgument> getDomPathArgument() {
            return yangIdentifier;
        }
    }

    public static class ContainerNode extends DataChildContainerNode<ContainerSchemaNode> {

        private final Optional<PathArgument> yangIdentifier;

        public ContainerNode(final Class<?> cls, final ContainerSchemaNode nodeSchema,
                final BindingMetadataLoader loader) {
            super(cls, nodeSchema.getQName().getModule(), nodeSchema, loader);
            this.yangIdentifier = Optional
                    .<YangInstanceIdentifier.PathArgument> of(new YangInstanceIdentifier.NodeIdentifier(nodeSchema
                            .getQName()));
        }

        @Override
        public Optional<PathArgument> getDomPathArgument() {
            return yangIdentifier;
        }

    }

    public static class CaseContextNode extends DataChildContainerNode<ChoiceCaseNode> {

        private final Optional<PathArgument> yangIdentifier;

        public CaseContextNode(final Class<?> cls, final ChoiceCaseNode nodeSchema, final BindingMetadataLoader loader) {
            super(cls, nodeSchema.getQName().getModule(), nodeSchema, loader);
            this.yangIdentifier = Optional
                    .<YangInstanceIdentifier.PathArgument> of(new YangInstanceIdentifier.NodeIdentifier(nodeSchema
                            .getQName()));
        }

        @Override
        public Optional<PathArgument> getDomPathArgument() {
            return yangIdentifier;
        }

    }

    public static class ChoiceContextNode extends DataContainerNode<ChoiceNode> {

        private final Optional<PathArgument> yangArgument;

        public ChoiceContextNode(final Class<?> cls, final ChoiceNode nodeSchema, final BindingMetadataLoader loader) {
            super(cls, nodeSchema.getQName().getModule(), nodeSchema, loader);
            yangArgument = Optional.<PathArgument> of(new YangInstanceIdentifier.NodeIdentifier(nodeSchema.getQName()));
        }

        @Override
        public Optional<PathArgument> getDomPathArgument() {
            return yangArgument;
        }

        @Override
        CodecContextNode loadChild(final Class<?> childClass) {

            DataSchemaNode origSchema = metadataLoader.getSchemaDefinition(childClass);
            Preconditions.checkArgument(origSchema instanceof ChoiceCaseNode);

            ChoiceCaseNode sameName = schema.getCaseNodeByName(origSchema.getQName());
            final ChoiceCaseNode found = BindingSchemaContextUtils.findInstantiatedCase(schema,
                    (ChoiceCaseNode) origSchema).get();
            return new CaseContextNode(childClass, found, metadataLoader);
        }

    }

    public static class ListNode extends DataChildContainerNode<ListSchemaNode> {

        private final Optional<PathArgument> yangIdentifier;
        private final NodeIdentifierWithPredicatesCodec codec;

        public ListNode(final Class<?> cls, final ListSchemaNode nodeSchema, final BindingMetadataLoader loader) {
            super(cls, nodeSchema.getQName().getModule(), nodeSchema, loader);
            this.yangIdentifier = Optional
                    .<YangInstanceIdentifier.PathArgument> of(new YangInstanceIdentifier.NodeIdentifier(nodeSchema
                            .getQName()));

            if (Identifiable.class.isAssignableFrom(cls)) {
                this.codec = loader.getPathArgumentCodec(cls);
            } else {
                this.codec = null;
            }
        }

        @Override
        public Optional<PathArgument> getDomPathArgument() {
            return yangIdentifier;
        }

        public NodeIdentifierWithPredicates serialize(final Identifier<?> binding) {
            return codec.serialize(schema.getQName(), binding);
        }
    }

    public static class RootNode extends DataContainerNode<SchemaContext> {

        public RootNode(final SchemaContext nodeSchema, final BindingMetadataLoader loader) {
            super(RootNode.class, null, nodeSchema, loader);
        }

        @Override
        CodecContextNode loadChild(final Class<?> childClass) {
            Class<Object> parent = ClassLoaderUtils.findFirstGenericArgument(childClass, ChildOf.class);
            Preconditions.checkArgument(DataRoot.class.isAssignableFrom(parent));

            QName qname = BindingReflections.findQName(childClass);
            DataSchemaNode childSchema = getSchema().getDataChildByName(qname);
            return CodecContextNode.from(childClass, childSchema, metadataLoader);
        }

        @Override
        public Optional<PathArgument> getDomPathArgument() {
            return Optional.absent();
        }
    }
}
