package org.opendaylight.yangtools.yang.data.util.tree;

import java.util.List;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public abstract class YangMetadataFactory<M> implements MetadataFactory<M> {

    @Override
    public final @Nullable M from(final PathArgument arg,final DataSchemaNode schema) {
        if (schema instanceof ContainerSchemaNode) {
            return fromContainer((ContainerSchemaNode) schema);
        } else if (schema instanceof ListSchemaNode) {
            return fromList((ListSchemaNode) schema);
        } else if (schema instanceof LeafSchemaNode) {
            return fromLeaf((LeafSchemaNode) schema);
        } else if (schema instanceof ChoiceNode) {
            return fromChoice((ChoiceNode) schema);
        } else if (schema instanceof LeafListSchemaNode) {
            return fromLeafList((LeafListSchemaNode) schema);
        } else if (schema instanceof AnyXmlSchemaNode) {
            return fromAnyXml((AnyXmlSchemaNode) schema);
        }
        throw new IllegalArgumentException("Not supported schema node type for " + schema.getClass());
    }

    protected abstract @Nullable M fromAnyXml(AnyXmlSchemaNode schemaNode);

    protected abstract @Nullable M fromLeafList(LeafListSchemaNode schemaNode);

    protected abstract @Nullable M fromChoice(ChoiceNode schemaNode);

    protected abstract @Nullable M fromLeaf(LeafSchemaNode schemaNode);

    protected abstract @Nullable M fromList(ListSchemaNode schemaNode);

    protected abstract @Nullable M fromContainer(ContainerSchemaNode schemaNode);

    protected abstract @Nullable M from



    static abstract class OrderingSensitive<M> extends YangMetadataFactory<M> {

        @Override
        protected final @Nullable M fromList(final ListSchemaNode schemaNode) {
            final List<QName> keyDefinition = schemaNode.getKeyDefinition();
            if (keyDefinition == null || keyDefinition.isEmpty()) {
                return fromUnkeyedList(schemaNode);
            }
            if (schemaNode.isUserOrdered()) {
                return fromOrderedMap(schemaNode);
            }
            return fromUnorderedMap(schemaNode);
        }

        @Override
        protected final @Nullable M fromLeafList(final LeafListSchemaNode schemaNode) {
            if (schemaNode.isUserOrdered()) {
                return fromOrderedLeafSet(schemaNode);
            }
            return fromUnorderedLeafSet(schemaNode);
        }

        protected abstract @Nullable M fromUnkeyedList(ListSchemaNode schemaNode);

        protected abstract @Nullable M fromOrderedMap(ListSchemaNode schemaNode);

        protected abstract @Nullable M fromUnorderedMap(ListSchemaNode schemaNode);

        protected abstract @Nullable M fromOrderedLeafSet(LeafListSchemaNode schemaNode);

        protected abstract @Nullable M fromUnorderedLeafSet(LeafListSchemaNode schemaNode);

    }
}
