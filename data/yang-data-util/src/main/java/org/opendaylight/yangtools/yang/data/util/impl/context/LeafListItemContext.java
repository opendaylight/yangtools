/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.context;

import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.SimpleValue;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

public final class LeafListItemContext extends AbstractContext implements SimpleValue {
    LeafListItemContext(final LeafListSchemaNode schema) {
        super(null, schema);
    }

    @Override
    void pushToStack(final SchemaInferenceStack stack) {
        // No-op
    }
}
