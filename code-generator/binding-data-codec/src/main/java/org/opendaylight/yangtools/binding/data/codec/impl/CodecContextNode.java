package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Map.Entry;
import org.opendaylight.yangtools.binding.data.codec.api.NodeIdentifierWithPredicatesCodec;
import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl;
import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.binding.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
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

    public abstract YangInstanceIdentifier.PathArgument getDomPathArgument();

    public static RootNode root(final CodecContextFactory schemaCtx) {
        return new RootNode(schemaCtx);
    }

    public interface CodecContextFactory {

        BindingRuntimeContext getRuntimeContext();

        ImmutableMap<String, LeafNode> getLeafNodes(Class<?> parentClass, DataNodeContainer childSchema);

        NodeIdentifierWithPredicatesCodec getPathArgumentCodec(Class<?> identifiable);

    }

    abstract static class LeafNode extends CodecContextNode {

        private final YangInstanceIdentifier.PathArgument yangIdentifier;

        LeafNode(final DataSchemaNode node) {
            yangIdentifier = new YangInstanceIdentifier.NodeIdentifier(node.getQName());
        }

        @Override
        public YangInstanceIdentifier.PathArgument getDomPathArgument() {
            return (yangIdentifier);
        }

        public abstract Object serializeValue(final Object value);

    }

    static class NoopSerializeLeafNode extends LeafNode {
        public NoopSerializeLeafNode(final DataSchemaNode node) {
            super(node);
        }

        @Override
        public Object serializeValue(final Object value) {
            return value;
        }

    }

    public abstract static class DataContainerNode<T> extends CodecContextNode {

        protected final T schema;
        protected final QNameModule namespace;
        protected final CodecContextFactory factory;
        protected final Class<?> bindingClass;

        protected final LoadingCache<Class<?>, DataContainerNode<?>> containerChild;

        public DataContainerNode(final Class<?> cls, final QNameModule namespace, final T nodeSchema,
                final CodecContextFactory factory) {
            super();
            this.schema = nodeSchema;
            this.factory = factory;
            this.namespace = namespace;
            this.bindingClass = cls;

            this.containerChild = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, DataContainerNode<?>>() {
                @Override
                public DataContainerNode<?> load(final Class<?> key) throws Exception {
                    return loadChild(key);
                }
            });
        }

        public T getSchema() {
            return schema;
        }

        public DataContainerNode<?> getIdentifierChild(final InstanceIdentifier.PathArgument arg,
                final List<YangInstanceIdentifier.PathArgument> builder) {
            final DataContainerNode<?> child = getStreamChild(arg.getType());
            if (builder != null) {
                child.addYangPathArgument(arg, builder);
            }
            return child;
        }

        /**
         *
         * Returns child context as if it was walked by
         * {@link BindingStreamEventWriter}. This means that to enter case, one
         * must issue getChild(ChoiceClass).getChild(CaseClass).
         *
         * @param childClass
         * @return
         */
        public DataContainerNode<?> getStreamChild(final Class<?> childClass) {
            return containerChild.getUnchecked(childClass);
        }

        abstract DataContainerNode<?> loadChild(final Class<?> childClass);

        @Override
        public String toString() {
            return getClass().getSimpleName() + " [" + bindingClass + "]";
        }

    }

    private static DataContainerNode<?> from(final Class<?> cls, final DataSchemaNode schema,
            final CodecContextFactory loader) {
        if (schema instanceof ContainerSchemaNode) {
            return new ContainerNode(cls, (ContainerSchemaNode) schema, loader);
        } else if (schema instanceof ListSchemaNode) {
            return new ListNode(cls, (ListSchemaNode) schema, loader);
        } else if (schema instanceof ChoiceNode) {
            return new ChoiceContextNode(cls, (ChoiceNode) schema, loader);
        }
        throw new IllegalArgumentException("Not supported type " + cls + " " + schema);
    }

    public void addYangPathArgument(final InstanceIdentifier.PathArgument arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        if (builder != null) {
            builder.add(getDomPathArgument());
        }
    }

    public abstract static class DataChildContainerNode<T extends DataNodeContainer> extends DataContainerNode<T> {

        protected final T schema;
        protected final ImmutableMap<String, LeafNode> leafChild;
        protected final ImmutableMap<Type, Entry<Type, Type>> choiceCaseChildren;

        public DataChildContainerNode(final Class<?> cls, final QNameModule namespace, final T nodeSchema,
                final CodecContextFactory loader) {
            super(cls, namespace, nodeSchema, loader);
            this.schema = nodeSchema;
            this.choiceCaseChildren = factory.getRuntimeContext().getChoiceCaseChildren(schema);
            this.leafChild = loader.getLeafNodes(cls, nodeSchema);
        }

        @Override
        public T getSchema() {
            return schema;
        }

        @Override
        public DataContainerNode<?> getIdentifierChild(final InstanceIdentifier.PathArgument arg,
                final List<YangInstanceIdentifier.PathArgument> builder) {
            if (choiceCaseChildren.isEmpty()) {
                return super.getIdentifierChild(arg, builder);
            }
            // Lookup in choiceCase
            Class<? extends DataObject> argument = arg.getType();
            ReferencedTypeImpl ref = new ReferencedTypeImpl(argument.getPackage().getName(), argument.getSimpleName());
            Entry<Type, Type> cazeId = choiceCaseChildren.get(ref);
            if (cazeId == null) {
                return super.getIdentifierChild(arg, builder);
            }
            ClassLoadingStrategy loader = factory.getRuntimeContext().getStrategy();
            try {
                Class<?> choice = loader.loadClass(cazeId.getKey());
                Class<?> caze = loader.loadClass(cazeId.getValue());
                ChoiceContextNode choiceNode = (ChoiceContextNode) getStreamChild(choice);
                choiceNode.addYangPathArgument(arg, builder);
                CaseContextNode cazeNode = (CaseContextNode) choiceNode.getStreamChild(caze);
                cazeNode.addYangPathArgument(arg, builder);
                return cazeNode.getIdentifierChild(arg, builder);

            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Required class not found.", e);
            }

        }

        public final LeafNode getLeafChild(final String name) {
            final LeafNode value = leafChild.get(name);
            Preconditions.checkArgument(value != null);
            return value;
        }

        @Override
        DataContainerNode<?> loadChild(final Class<?> childClass) {
            if (Augmentation.class.isAssignableFrom(childClass)) {
                return loadAugmentation(childClass);
            } else {

                DataSchemaNode origDef = factory.getRuntimeContext().getSchemaDefinition(childClass);
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
                return CodecContextNode.from(childClass, childSchema, factory);
            }
        }

        @SuppressWarnings("rawtypes")
        private AugmentationNode loadAugmentation(final Class childClass) {
            Preconditions.checkArgument(schema instanceof AugmentationTarget);
            Entry<AugmentationIdentifier, AugmentationSchema> augSchema = factory.getRuntimeContext()
                    .getAugmentationSchema(schema, childClass);
            QNameModule namespace = Iterables.getFirst(augSchema.getKey().getPossibleChildNames(), null).getModule();
            return new AugmentationNode(childClass, namespace, augSchema.getKey(), augSchema.getValue(), factory);
        }

    }

    public static class AugmentationNode extends DataChildContainerNode<AugmentationSchema> {

        private final YangInstanceIdentifier.PathArgument yangIdentifier;

        public AugmentationNode(final Class<?> cls, final QNameModule namespace,
                final AugmentationIdentifier identifier, final AugmentationSchema nodeSchema,
                final CodecContextFactory loader) {
            super(cls, namespace, nodeSchema, loader);
            this.yangIdentifier = identifier;
        }

        @Override
        public YangInstanceIdentifier.PathArgument getDomPathArgument() {
            return yangIdentifier;
        }
    }

    public static class ContainerNode extends DataChildContainerNode<ContainerSchemaNode> {

        private final YangInstanceIdentifier.PathArgument yangIdentifier;

        public ContainerNode(final Class<?> cls, final ContainerSchemaNode nodeSchema, final CodecContextFactory loader) {
            super(cls, nodeSchema.getQName().getModule(), nodeSchema, loader);
            this.yangIdentifier = (new YangInstanceIdentifier.NodeIdentifier(nodeSchema.getQName()));
        }

        @Override
        public YangInstanceIdentifier.PathArgument getDomPathArgument() {
            return yangIdentifier;
        }

    }

    public static class CaseContextNode extends DataChildContainerNode<ChoiceCaseNode> {

        private final YangInstanceIdentifier.PathArgument yangIdentifier;

        public CaseContextNode(final Class<?> cls, final ChoiceCaseNode nodeSchema,
                final CodecContextFactory runtimeContext) {
            super(cls, nodeSchema.getQName().getModule(), nodeSchema, runtimeContext);
            this.yangIdentifier = (new YangInstanceIdentifier.NodeIdentifier(nodeSchema.getQName()));
        }

        @Override
        public YangInstanceIdentifier.PathArgument getDomPathArgument() {
            return yangIdentifier;
        }

        @Override
        public void addYangPathArgument(final PathArgument arg,
                final List<org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument> builder) {
            // NOOP
        }

    }

    public static class ChoiceContextNode extends DataContainerNode<ChoiceNode> {

        private final YangInstanceIdentifier.PathArgument yangArgument;

        public ChoiceContextNode(final Class<?> cls, final ChoiceNode nodeSchema, final CodecContextFactory context) {
            super(cls, nodeSchema.getQName().getModule(), nodeSchema, context);
            yangArgument = new YangInstanceIdentifier.NodeIdentifier(nodeSchema.getQName());
        }

        @Override
        public YangInstanceIdentifier.PathArgument getDomPathArgument() {
            return yangArgument;
        }

        @Override
        DataContainerNode<?> loadChild(final Class<?> childClass) {

            ChoiceCaseNode childSchema = factory.getRuntimeContext().getCaseSchemaDefinition(schema, childClass);
            return new CaseContextNode(childClass, childSchema, factory);
        }

    }

    public static class ListNode extends DataChildContainerNode<ListSchemaNode> {

        private final YangInstanceIdentifier.PathArgument yangIdentifier;
        private final NodeIdentifierWithPredicatesCodec codec;

        public ListNode(final Class<?> cls, final ListSchemaNode nodeSchema, final CodecContextFactory loader) {
            super(cls, nodeSchema.getQName().getModule(), nodeSchema, loader);
            this.yangIdentifier = new YangInstanceIdentifier.NodeIdentifier(nodeSchema.getQName());
            if (Identifiable.class.isAssignableFrom(cls)) {
                this.codec = loader.getPathArgumentCodec(cls);
            } else {
                this.codec = null;
            }
        }

        @Override
        public YangInstanceIdentifier.PathArgument getDomPathArgument() {
            return yangIdentifier;
        }

        @Override
        public void addYangPathArgument(final PathArgument arg, final List<YangInstanceIdentifier.PathArgument> builder) {

            /**
             * DOM Instance Identifier for list is always represent by
             * two entries one for map and one for children.
             * This is also true for wildcarded instance identifiers
             *
             */
            if (builder == null) {
                return;
            }
            super.addYangPathArgument(arg, builder);
            if(arg instanceof IdentifiableItem<?, ?>) {
                builder.add(serialize(((IdentifiableItem) arg).getKey()));
            } else {
                // Adding wildarded
                super.addYangPathArgument(arg, builder);
            }
        }

        public NodeIdentifierWithPredicates serialize(final Identifier<?> binding) {
            return codec.serialize(schema.getQName(), binding);
        }
    }

    public static class RootNode extends DataContainerNode<SchemaContext> {

        public RootNode(final CodecContextFactory factory) {
            super(RootNode.class, null, factory.getRuntimeContext().getSchemaContext(), factory);
        }

        @Override
        DataContainerNode<?> loadChild(final Class<?> childClass) {
            Class<Object> parent = ClassLoaderUtils.findFirstGenericArgument(childClass, ChildOf.class);
            Preconditions.checkArgument(DataRoot.class.isAssignableFrom(parent));

            QName qname = BindingReflections.findQName(childClass);
            DataSchemaNode childSchema = getSchema().getDataChildByName(qname);
            return CodecContextNode.from(childClass, childSchema, factory);
        }

        @Override
        public YangInstanceIdentifier.PathArgument getDomPathArgument() {
            throw new UnsupportedOperationException();
        }
    }
}
