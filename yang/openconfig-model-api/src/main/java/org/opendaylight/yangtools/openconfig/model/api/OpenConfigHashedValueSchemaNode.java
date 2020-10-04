/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.model.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementEquivalent;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

/**
 * SchemaNode presence in SchemaNode API space.
 *
 * @author Robert Varga
 */
@Beta
public interface OpenConfigHashedValueSchemaNode
        extends UnknownSchemaNode, EffectiveStatementEquivalent<OpenConfigHashedValueEffectiveStatement> {
    /**
     * Determine if specified SchemaNode is marked to report its value in hashed form.
     *
     * @param schemaNode schema node to examine
     * @return True if specified node is marked, false otherwise
     * @throws NullPointerException if schemaNode is null
     */
    static boolean isPresentIn(final SchemaNode schemaNode) {
        return schemaNode.getUnknownSchemaNodes().stream().anyMatch(OpenConfigHashedValueSchemaNode.class::isInstance);
    }
}
