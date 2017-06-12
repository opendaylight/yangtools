/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

public class SchemaNodeUtils {

    private SchemaNodeUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Optional<SchemaNode> getOriginalIfPossible(final SchemaNode node) {
        if (node instanceof DerivableSchemaNode) {
            @SuppressWarnings("unchecked")
            final Optional<SchemaNode> ret  = (Optional<SchemaNode>) (((DerivableSchemaNode) node).getOriginal());
            return ret;
        }
        return Optional.absent();
    }

    public static SchemaNode getRootOriginalIfPossible(final SchemaNode data) {
        Optional<SchemaNode> previous = Optional.absent();
        Optional<SchemaNode> next = getOriginalIfPossible(data);
        while (next.isPresent()) {
            previous = next;
            next = getOriginalIfPossible(next.get());
        }
        return previous.orNull();
    }

    /**
     * Returns RPC input or output schema based on supplied QName.
     *
     * @param rpc RPC Definition
     * @param qname input or output QName with namespace same as RPC
     * @return input or output schema. Returns null if RPC does not have input/output specified.
     */
    @Nullable public static ContainerSchemaNode getRpcDataSchema(@Nonnull final RpcDefinition rpc,
            @Nonnull final QName qname) {
        Preconditions.checkNotNull(rpc, "Rpc Schema must not be null");
        Preconditions.checkNotNull(qname,"QName must not be null");
        switch (qname.getLocalName()) {
            case "input":
                return rpc.getInput();
            case "output":
                return rpc.getOutput();
            default:
                throw new IllegalArgumentException("Supplied qname " + qname
                        + " does not represent rpc input or output.");
        }
    }
}
