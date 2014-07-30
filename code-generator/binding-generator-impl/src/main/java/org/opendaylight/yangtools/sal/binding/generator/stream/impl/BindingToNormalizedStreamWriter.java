package org.opendaylight.yangtools.sal.binding.generator.stream.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.CodecRegistry;
import org.opendaylight.yangtools.yang.data.impl.codec.ValueWithQName;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;


class BindingToNormalizedStreamWriter implements BindingStreamEventWriter, Delegator<NormalizedNodeStreamWriter> {

    private final CodecRegistry registry;
    private final NormalizedNodeStreamWriter delegate;
    private final Deque<DataSchemaNode> schema;
    private final DataSchemaNode rootNodeSchema;

    public BindingToNormalizedStreamWriter(final DataSchemaNode schema, final NormalizedNodeStreamWriter delegate,final CodecRegistry registry) {
        this.delegate = Preconditions.checkNotNull(delegate, "Delegate must not be null");
        this.schema = new ArrayDeque<>();
        this.registry = registry;
        this.rootNodeSchema = Preconditions.checkNotNull(schema);

    }

    private DataSchemaNode getCurrentSchema() {
        return schema.peek();
    }

    private NodeIdentifier enterSchema(final DataSchemaNode next, final Class<?> schemaClass) {
        Preconditions.checkArgument(schemaClass.isInstance(next), "Emitted start event %s is not according to schema %s",schemaClass.getSimpleName(),next);
        this.schema.push(next);
        return new NodeIdentifier(next.getQName());
    }

    private void duplicateSchemaEnter() {
        this.schema.push(getCurrentSchema());
    }

    @Override
    public void endNode() {
        schema.pop();
        getDelegate().endNode();
    }

    private NodeIdentifier childLeaf(final String name) {
        DataSchemaNode current = getCurrentSchema();
        QName childName = QName.create(current.getQName(), name);
        DataSchemaNode next = ((DataNodeContainer) current).getDataChildByName(childName);
        return new NodeIdentifier(next.getQName());

    }

    private NodeIdentifier enterLeafSchema(final String localName, final Class<?> type) {
        DataSchemaNode current = getCurrentSchema();
        QName potential = QName.create(current.getQName(), localName);
        if(current instanceof DataNodeContainer) {
            DataSchemaNode next = ((DataNodeContainer) current).getDataChildByName(potential);
            return enterSchema(next,type);

        }
        throw new IllegalArgumentException("Node " + current + "is leaf node type.");
    }

    private NodeIdentifier enterSchema(final Class<?> name, final Class<?> schemaClass) {
        DataSchemaNode current = getCurrentSchema();

        if(current == null) {
            return enterSchema(rootNodeSchema,schemaClass);
        }

        QName potential = BindingReflections.findQName(name);
        if(current instanceof DataNodeContainer) {
            potential = QName.create(current.getQName(), potential.getLocalName());
            DataSchemaNode next = ((DataNodeContainer) current).getDataChildByName(potential);
            if(next == null) {
                throw new IllegalArgumentException(name + "is incorrect");
            }
            this.schema.push(next);
            return new NodeIdentifier(next.getQName());
        }
        throw new IllegalArgumentException(name + "is not present in current schema " + current);
    }



    @Override
    public NormalizedNodeStreamWriter getDelegate() {
        return delegate;
    }

    @Override
    public void anyxmlNode(final String name, final Object value) throws IllegalArgumentException {
        getDelegate().anyxmlNode(childLeaf(name), value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getContext(final Class<T> contextType) {
        if(SchemaNode.class.equals(contextType)) {
            return Optional.of((T) getCurrentSchema());
        }
        return Optional.absent();
    }

    @Override
    public void leafNode(final String localName, final Object value) throws IllegalArgumentException {
        getDelegate().leafNode(childLeaf(localName), value);
    };

    @Override
    public void leafSetEntryNode(final Object value) throws IllegalArgumentException {
        getDelegate().leafSetEntryNode(value);

    }


    @Override
    public void startAugmentationNode(final Class<? extends Augmentation<?>> augmentationType)
            throws IllegalArgumentException {
        // FIXME: Add proper augmentation identifier construction
        Set<QName> childNames = new HashSet<>();

        getDelegate().startAugmentationNode(new AugmentationIdentifier(childNames));
    }


    @Override
    public void startCase(final Class<? extends DataObject> caze, final int childSizeHint) throws IllegalArgumentException {
        // TODO Auto-generated method stub
//        Preconditions.checkArgument(getCurrentSchema() instanceof ChoiceCaseNode);
//        ChoiceCaseNode caze = ((ChoiceNode) getCurrentSchema()).getCaseNodeByName(name);
//        enterSchema(caze);
    };



    @Override
    public void startChoiceNode(final Class<? extends DataContainer> type,final int childSizeHint) throws IllegalArgumentException {
        getDelegate().startChoiceNode(enterSchema(type,ChoiceNode.class),childSizeHint);
    }

    @Override
    public void startContainerNode(final Class<? extends DataObject> object,final int childSizeHint) throws IllegalArgumentException {
        getDelegate().startContainerNode(enterSchema(object,ContainerSchemaNode.class),childSizeHint);
    }

    @Override
    public void startLeafSet(final String localName,final int childSizeHint) throws IllegalArgumentException {
        getDelegate().startLeafSet(enterLeafSchema(localName,LeafListSchemaNode.class),childSizeHint);
    };

    @Override
    public void startMapEntryNode(final Identifier<?> key,final int childSizeHint) throws IllegalArgumentException {
        duplicateSchemaEnter();
        getDelegate().startMapEntryNode(createNodeIdentifierWithPredicates(key),childSizeHint);
    };



    private NodeIdentifierWithPredicates createNodeIdentifierWithPredicates(final Identifier<?> key) {
        ValueWithQName<Identifier<?>> codecInput = new ValueWithQName<Identifier<?>>(getCurrentSchema().getQName(), key);
        CompositeNode identifier = registry.getCodecForIdentifier(key.getClass()).serialize(codecInput);

        Map<QName, Object> keyValues = new HashMap<>();
        for(Node<?> child : identifier.getValue()) {
            SimpleNode<?> casted = (SimpleNode<?>) child;
            keyValues.put(casted.getNodeType(), casted.getValue());
        }
        return new NodeIdentifierWithPredicates(identifier.getNodeType(), keyValues );
    }

    @Override
    public <T extends DataObject & Identifiable<?>> void startMapNode(final Class<T> mapEntryType, final int childSizeHint)
            throws IllegalArgumentException {
        getDelegate().startMapNode(enterSchema(mapEntryType,ListSchemaNode.class),childSizeHint);
    };

    @Override
    public void startUnkeyedList(final Class<? extends DataObject> obj,final int childSizeHint) throws IllegalArgumentException {
        getDelegate().startUnkeyedList(enterSchema(obj,ListSchemaNode.class),childSizeHint);
    };

    @Override
    public void startUnkeyedListItem(final int childSizeHint) throws IllegalStateException {
        duplicateSchemaEnter();
        getDelegate().startUnkeyedListItem(new NodeIdentifier(getCurrentSchema().getQName()),childSizeHint);
    }

}
