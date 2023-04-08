/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataNodeContainerValidator;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.data.util.NormalizedNodeSchemaUtils;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;

final class SchemaAwareImmutableChoiceNodeBuilder extends ImmutableChoiceNodeBuilder {
    private final ChoiceSchemaNode schema;
    private DataNodeContainerValidator validator;

    SchemaAwareImmutableChoiceNodeBuilder(final ChoiceSchemaNode schema) {
        this.schema = requireNonNull(schema, "Schema was null");
        super.withNodeIdentifier(NodeIdentifier.create(schema.getQName()));
    }

    @Override
    public DataContainerNodeBuilder<NodeIdentifier, ChoiceNode> withNodeIdentifier(
            final NodeIdentifier withNodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }

    @Override
    public DataContainerNodeBuilder<NodeIdentifier, ChoiceNode> withChild(final DataContainerChild child) {
        if (validator == null) {
            Optional<CaseSchemaNode> detectedCaseOpt = NormalizedNodeSchemaUtils.detectCase(schema, child);
            DataValidationException.checkLegalChild(detectedCaseOpt.isPresent(), child.getIdentifier(), schema);
            validator = new DataNodeContainerValidator(detectedCaseOpt.orElseThrow());
        }

        return super.withChild(validator.validateChild(child));
    }

    @Override
    public ChoiceNode build() {
        // TODO validate when statement
        return super.build();
    }
}
