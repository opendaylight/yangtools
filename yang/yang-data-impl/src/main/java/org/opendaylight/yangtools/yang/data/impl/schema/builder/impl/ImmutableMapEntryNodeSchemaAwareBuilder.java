/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Collection;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataNodeContainerValidator;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public final class ImmutableMapEntryNodeSchemaAwareBuilder extends ImmutableMapEntryNodeBuilder{

    private final ListSchemaNode schema;
    private final DataNodeContainerValidator validator;

    protected ImmutableMapEntryNodeSchemaAwareBuilder(ListSchemaNode schema) {
        this.schema = schema;
        this.validator = new DataNodeContainerValidator(schema);
    }

    @Override
    public ImmutableMapEntryNodeBuilder withNodeIdentifier(InstanceIdentifier.NodeIdentifierWithPredicates nodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }

    @Override
    public ImmutableMapEntryNodeSchemaAwareBuilder withChild(DataContainerChild<?, ?> child) {
        validator.validateChild(child.getIdentifier());
        super.withChild(child);
        return this;
    }

    @Override
    public ImmutableMapEntryNode build() {
        super.withNodeIdentifier(constructNodeIdentifier());
        return super.build();
    }

    /**
     * Build map entry node identifier from schema, and provided children
     */
    private InstanceIdentifier.NodeIdentifierWithPredicates constructNodeIdentifier() {
        Collection<QName> keys = schema.getKeyDefinition();

        // If no keys defined, add all child elements as key
        // FIXME should be all PRESENT child nodes, not all from schema
        if(keys.isEmpty()) {
            keys = childrenQNamesToPaths.keySet();
        }

        Map<QName, Object> keysToValues = Maps.newHashMap();
        for (QName key : keys) {
            // TODO two maps ? find better solution
            DataContainerChild<?, ?> valueForKey = value.get(childrenQNamesToPaths.get(key));
            Preconditions.checkState(valueForKey != null, "Key value: %s cannot be empty for: %s", key, schema.getQName());
            keysToValues.put(key, valueForKey.getValue());
        }

        return new InstanceIdentifier.NodeIdentifierWithPredicates(schema.getQName(), keysToValues);
    }

    public static ImmutableMapEntryNodeSchemaAwareBuilder create(ListSchemaNode schema) {
        return new ImmutableMapEntryNodeSchemaAwareBuilder(schema);
    }

}
