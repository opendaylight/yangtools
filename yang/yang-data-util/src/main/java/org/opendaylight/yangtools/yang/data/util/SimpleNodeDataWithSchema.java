/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Utility class used for tracking parser state as needed by a StAX-like parser.
 * This class is to be used only by respective XML and JSON parsers in yang-data-codec-xml and yang-data-codec-gson.
 *
 * <p>
 * Represents a simple node with value (anyxml, leaf, leaf-list entry).
 */
public abstract class SimpleNodeDataWithSchema extends AbstractNodeDataWithSchema {

    private Object value;

    public SimpleNodeDataWithSchema(final DataSchemaNode dataSchemaNode) {
        super(dataSchemaNode);
    }

    public void setValue(final Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

}
