/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataNodeContainerValidator;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public final class ImmutableMapEntryNodeSchemaAwareBuilder extends ImmutableMapEntryNodeBuilder {

    private final ListSchemaNode schema;
    private final DataNodeContainerValidator validator;

    ImmutableMapEntryNodeSchemaAwareBuilder(final ListSchemaNode schema) {
        this.schema = Preconditions.checkNotNull(schema);
        this.validator = new DataNodeContainerValidator(schema);
    }

    @Override
    public ImmutableMapEntryNodeBuilder withNodeIdentifier(final NodeIdentifierWithPredicates nodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }

    @Override
    public DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> withChild(
            final DataContainerChild<?, ?> child) {
        validator.validateChild(child.getIdentifier());
        return super.withChild(child);
    }

    @Override
    public MapEntryNode build() {
        super.withNodeIdentifier(constructNodeIdentifier());
        return super.build();
    }

    /**
     * Build map entry node identifier from schema and provided children.
     */
    private NodeIdentifierWithPredicates constructNodeIdentifier() {
        Collection<QName> keys = schema.getKeyDefinition();

        if (keys.isEmpty()) {
            keys = childrenQNamesToPaths.keySet();
        }

        final Map<QName, Object> keysToValues = new LinkedHashMap<>();
        for (QName key : keys) {
            final DataContainerChild<?, ?> valueForKey = getChild(childrenQNamesToPaths.get(key));
            DataValidationException.checkListKey(valueForKey, key, new NodeIdentifierWithPredicates(
                schema.getQName(), keysToValues));
            keysToValues.put(key, valueForKey.getValue());
        }

        return new NodeIdentifierWithPredicates(schema.getQName(), keysToValues);
    }

    public static DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> create(
            final ListSchemaNode schema) {
        return new ImmutableMapEntryNodeSchemaAwareBuilder(schema);
    }
}
