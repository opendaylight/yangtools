package org.opendaylight.yangtools.yang.data.util.tree;

import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public class NormalizedNodeClassMappingFactory extends YangMetadataFactory.OrderingSensitive<Class<? extends NormalizedNode<?, ?>>>{

    @Override
    public Class<? extends NormalizedNode<?, ?>> fromAugmentation(final AugmentationSchema schema) {
        return AugmentationNode.class;
    }

    @Override
    protected Class<? extends NormalizedNode<?, ?>> fromAnyXml(final AnyXmlSchemaNode schemaNode) {
        return AnyXmlNode.class;
    }

    @Override
    protected Class<? extends NormalizedNode<?, ?>> fromChoice(final ChoiceNode schemaNode) {
        return org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode.class;
    }

    @Override
    protected Class<? extends NormalizedNode<?, ?>> fromContainer(final ContainerSchemaNode schemaNode) {
        return ContainerNode.class;
    }

    @Override
    protected Class<? extends NormalizedNode<?, ?>> fromLeaf(final LeafSchemaNode schemaNode) {
        return LeafNode.class;
    }

    @Override
    protected Class<? extends NormalizedNode<?, ?>> fromOrderedLeafSet(final LeafListSchemaNode schemaNode) {
        return OrderedLeafSetNode.class;
    }

    @Override
    protected Class<? extends NormalizedNode<?, ?>> fromOrderedMap(final ListSchemaNode schemaNode) {
        return OrderedMapNode.class;
    }

    @Override
    protected Class<? extends NormalizedNode<?, ?>> fromUnkeyedList(final ListSchemaNode schemaNode) {
        return UnkeyedListNode.class;
    }
    @Override
    protected Class<? extends NormalizedNode<?, ?>> fromUnorderedLeafSet(final LeafListSchemaNode schemaNode) {
        return LeafSetNode.class;
    }

    @Override
    protected Class<? extends NormalizedNode<?, ?>> fromUnorderedMap(final ListSchemaNode schemaNode) {
        return MapNode.class;
    }
}
