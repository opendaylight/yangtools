/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.YangModeledAnyXmlNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangModeledAnyXmlSchemaNode;

/**
 * Abstract(base) parser for YangModeledAnyXmlNodes, parses elements of type E.
 *
 * @param <E> type of elements to be parsed
 */
public abstract class YangModeledAnyXmlNodeBaseParser<E> extends
        BaseDispatcherParser<E, YangInstanceIdentifier.NodeIdentifier, YangModeledAnyXmlNode, YangModeledAnyXmlSchemaNode> {

    public YangModeledAnyXmlNodeBaseParser() {}

    public YangModeledAnyXmlNodeBaseParser(final BuildingStrategy<YangInstanceIdentifier.NodeIdentifier, YangModeledAnyXmlNode> buildingStrategy) {
        super(buildingStrategy);
    }

    @Override
    protected final DataYangModeledAnyXmlNodeBuilder<YangInstanceIdentifier.NodeIdentifier, YangModeledAnyXmlNode> getBuilder(
            final YangModeledAnyXmlSchemaNode schema) {
        return Builders.containerBuilder(schema);
    }

    @Override
    public final YangModeledAnyXmlNode parse(final Iterable<E> elements, final YangModeledAnyXmlSchemaNode schema) {
        checkOnlyOneNode(schema, elements);
        return super.parse(elements, schema);
    }

    @Override
    protected final Set<DataSchemaNode> getRealSchemasForAugment(final YangModeledAnyXmlSchemaNode schema, final AugmentationSchema augmentSchema) {
        return SchemaUtils.getRealSchemasForAugment((AugmentationTarget) schema, augmentSchema);
    }

    @Override
    protected final DataSchemaNode getSchemaForChild(final YangModeledAnyXmlSchemaNode schema, final QName childQName) {
        return SchemaUtils.findSchemaForChild(schema, childQName, strictParsing());
    }

    @Override
    protected final Map<QName, ChoiceSchemaNode> mapChildElementsFromChoices(final YangModeledAnyXmlSchemaNode schema) {
        return SchemaUtils.mapChildElementsFromChoices(schema);
    }

    @Override
    protected final Map<QName, AugmentationSchema> mapChildElementsFromAugments(final YangModeledAnyXmlSchemaNode schema) {
        return SchemaUtils.mapChildElementsFromAugments(schema);
    }

    @Override
    protected abstract Map<QName, String> getAttributes(E e);

}
