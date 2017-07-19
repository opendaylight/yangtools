/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer;

import java.util.Collections;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.FromNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;

/**
 * Abstract(base) serializer for AnyXmlNodes, serializes elements of type E.
 *
 * @param <E> type of serialized elements
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public abstract class AnyXmlNodeBaseSerializer<E> implements
        FromNormalizedNodeSerializer<E, AnyXmlNode, AnyXmlSchemaNode> {

    @Override
    public final Iterable<E> serialize(final AnyXmlSchemaNode schema, final AnyXmlNode node) {
        return Collections.singletonList(serializeAnyXml(node));
    }

    /**
     * Serialize the inner value of a AnyXmlNode into element of type E.
     *
     * @param node to be serialized
     * @return serialized inner value as an Element
     */
    protected abstract E serializeAnyXml(AnyXmlNode node);
}
