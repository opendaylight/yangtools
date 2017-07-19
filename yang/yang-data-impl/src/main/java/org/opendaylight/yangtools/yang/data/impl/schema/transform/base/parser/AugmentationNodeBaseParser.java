/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Abstract(base) parser for AugmentationNode, parses elements of type E.
 *
 * @param <E> type of elements to be parsed
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public abstract class AugmentationNodeBaseParser<E> extends
        BaseDispatcherParser<E, YangInstanceIdentifier.AugmentationIdentifier, AugmentationNode, AugmentationSchema> {

    public AugmentationNodeBaseParser(final BuildingStrategy<YangInstanceIdentifier.AugmentationIdentifier, AugmentationNode> buildingStrategy) {
        super(buildingStrategy);
    }

    public AugmentationNodeBaseParser() {}

    @Override
    protected final DataContainerNodeBuilder<YangInstanceIdentifier.AugmentationIdentifier, AugmentationNode> getBuilder(final AugmentationSchema schema) {
        return Builders.augmentationBuilder(schema);
    }

    @Override
    protected final Set<DataSchemaNode> getRealSchemasForAugment(final AugmentationSchema schema, final AugmentationSchema augmentSchema) {
        return SchemaUtils.getRealSchemasForAugment(schema, augmentSchema);
    }


    @Override
    protected final DataSchemaNode getSchemaForChild(final AugmentationSchema schema, final QName childQName) {
        return SchemaUtils.findSchemaForChild(schema, childQName, strictParsing());
    }

    @Override
    protected final Map<QName, ChoiceSchemaNode> mapChildElementsFromChoices(final AugmentationSchema schema) {
        return SchemaUtils.mapChildElementsFromChoices(schema);
    }

    @Override
    protected final Map<QName, AugmentationSchema> mapChildElementsFromAugments(final AugmentationSchema schema) {
        return Collections.emptyMap();
    }
}
