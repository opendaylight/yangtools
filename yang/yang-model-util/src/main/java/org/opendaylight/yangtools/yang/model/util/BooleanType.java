/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;

/**
 * The <code>default</code> implementation of Boolean Type Definition interface.
 *
 * @see BooleanTypeDefinition
 */
public final class BooleanType extends AbstractTypeDefinition<BooleanTypeDefinition> implements BooleanTypeDefinition {
    private static final SchemaPath PATH = SchemaPath.create(true, BaseTypes.BOOLEAN_QNAME);
    private static final String DESCRIPTION = "The boolean built-in type represents a boolean value.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.5";
    private static final String UNITS = "";

    private static final BooleanType INSTANCE = new BooleanType(PATH, Status.CURRENT, DESCRIPTION, REFERENCE, null,
        null, UNITS, Boolean.FALSE);

    BooleanType(final SchemaPath path, final Status status, final String description, final String reference,
            final List<UnknownSchemaNode> unknownSchemaNodes, final BooleanTypeDefinition baseType, final String units,
            final Object defaultValue) {
        super(path, description, reference, status, unknownSchemaNodes, baseType, units, defaultValue);
    }

    /**
     * Returns default instance of boolean built-in type.
     * @return default instance of boolean built-in type.
     */
    public static BooleanType getInstance() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BooleanType [name=");
        builder.append(getQName());
        builder.append(", path=");
        builder.append(getPath());
        builder.append("]");
        return builder.toString();
    }
}
