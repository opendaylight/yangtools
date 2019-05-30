/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.ImmutableMapTemplate;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataNodeContainerValidator;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException.IllegalListKeyException;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public final class ImmutableMapEntryNodeSchemaAwareBuilder extends ImmutableMapEntryNodeBuilder {

    private final ListSchemaNode schema;
    private final DataNodeContainerValidator validator;

    ImmutableMapEntryNodeSchemaAwareBuilder(final ListSchemaNode schema) {
        this.schema = requireNonNull(schema);
        this.validator = new DataNodeContainerValidator(schema);
    }

    @Override
    public ImmutableMapEntryNodeBuilder withNodeIdentifier(final NodeIdentifierWithPredicates withNodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }

    @Override
    public DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> withChild(
            final DataContainerChild<?, ?> child) {
        validator.validateChild(child.getIdentifier());
        return super.withChild(child);
    }

    @Override
    public MapEntryNode build() {
        super.withNodeIdentifier(constructNodeIdentifier());
        return super.build();
    }

    public static @NonNull DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> create(
            final ListSchemaNode schema) {
        return new ImmutableMapEntryNodeSchemaAwareBuilder(schema);
    }

    /**
     * Build map entry node identifier from schema and provided children.
     */
    private NodeIdentifierWithPredicates constructNodeIdentifier() {
        final Map<QName, Object> predicates;
        final Collection<QName> keys = schema.getKeyDefinition();
        if (!keys.isEmpty()) {
            predicates = keyDefToPredicates(keys);
        } else if (!childrenQNamesToPaths.isEmpty()) {
            predicates = childrenToPredicates();
        } else {
            predicates = ImmutableMap.of();
        }
        return NodeIdentifierWithPredicates.of(schema.getQName(), predicates);
    }

    private Map<QName, Object> childrenToPredicates() {
        final Object[] values = new Object[childrenQNamesToPaths.size()];
        int offset = 0;
        for (Entry<QName, PathArgument> entry : childrenQNamesToPaths.entrySet()) {
            values[offset++] = nonnullKeyValue(entry.getKey(), getChild(entry.getValue())).getValue();
        }
        return ImmutableMapTemplate.ordered(childrenQNamesToPaths.keySet()).instantiateWithValues(values);
    }

    private Map<QName, Object> keyDefToPredicates(final Collection<QName> keys) {
        final Object[] values = new Object[keys.size()];
        int offset = 0;
        for (QName key : keys) {
            values[offset++] = nonnullKeyValue(key, getChild(childrenQNamesToPaths.get(key))).getValue();
        }
        return ImmutableMapTemplate.ordered(keys).instantiateWithValues(values);
    }

    private DataContainerChild<?, ?> nonnullKeyValue(final QName key, final DataContainerChild<?, ?> value) {
        if (value != null) {
            return value;
        }
        throw new IllegalListKeyException("Key value not present for key: %s, in: %s values %s", key, schema.getQName(),
            buildValue());
    }
}
