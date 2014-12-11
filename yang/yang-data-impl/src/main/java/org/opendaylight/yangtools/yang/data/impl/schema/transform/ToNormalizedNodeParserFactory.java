/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform;

import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Factory for different normalized node parsers.
 *
 * @param <E>
 *            type of element to be parsed into NormalizedNode
 */
public interface ToNormalizedNodeParserFactory<E> {
	ToNormalizedNodeParser<E, AugmentationNode, AugmentationSchema> getAugmentationNodeParser();
	ToNormalizedNodeParser<E, ChoiceNode, org.opendaylight.yangtools.yang.model.api.ChoiceNode> getChoiceNodeParser();
	ToNormalizedNodeParser<E, ContainerNode, ContainerSchemaNode> getContainerNodeParser();
	ToNormalizedNodeParser<E, LeafNode<?>, LeafSchemaNode> getLeafNodeParser();
	ToNormalizedNodeParser<E, LeafSetEntryNode<?>, LeafListSchemaNode> getLeafSetEntryNodeParser();
	ToNormalizedNodeParser<E, LeafSetNode<?>, LeafListSchemaNode> getLeafSetNodeParser();
	ToNormalizedNodeParser<E, MapEntryNode, ListSchemaNode> getMapEntryNodeParser();
	ToNormalizedNodeParser<E, MapNode, ListSchemaNode> getMapNodeParser();
        ToNormalizedNodeParser<E, AnyXmlNode, AnyXmlSchemaNode> getAnyXmlNodeParser();
}
