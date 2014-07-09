package org.opendaylight.yangtools.yang.parser.builder.util;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

public abstract class AbstractDocumentedDataNodeContainer extends AbstractDocumentedNode implements DataNodeContainer {

    private final Set<DataSchemaNode> childNodes;
    private final Set<GroupingDefinition> groupings;
    private final Set<UsesNode> uses;
    private final Set<TypeDefinition<?>> typeDefinitions;

    protected AbstractDocumentedDataNodeContainer(final AbstractDocumentedDataNodeContainerBuilder data) {
        super(data);
        childNodes = ImmutableSet.copyOf(data.getChildNodes());
        groupings = ImmutableSet.copyOf(data.getGroupings());
        uses = ImmutableSet.copyOf(data.getUsesNodes());
        typeDefinitions = ImmutableSet.copyOf(data.getTypeDefinitions());
    }

    @Override
    public final Set<TypeDefinition<?>> getTypeDefinitions() {
        return typeDefinitions;
    }

    @Override
    public final Set<DataSchemaNode> getChildNodes() {
        return childNodes;
    }

    @Override
    public final Set<GroupingDefinition> getGroupings() {
        return groupings;
    }

    @Override
    public final DataSchemaNode getDataChildByName(final QName name) {
        for (DataSchemaNode node : childNodes) {
            if (node.getQName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public final DataSchemaNode getDataChildByName(final String name) {
        for (DataSchemaNode node : childNodes) {
            if (node.getQName().getLocalName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public Set<UsesNode> getUses() {
        return uses;
    }

}
