/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 *
 * Node which can have documentation assigned.
 *
 */
@Value.Immutable
public interface DocumentedNode {

    /**
     * Returns description of the instance of the type <code>SchemaNode</code>
     *
     * @return string with textual description the node which represents the
     *         argument of the YANG <code>description</code> substatement
     */
    @Nullable String getDescription();

    /**
     * Returns reference of the instance of the type <code>SchemaNode</code>
     *
     * The reference refers to external document that provides additional
     * information relevant for the instance of this type.
     *
     * @return string with the reference to some external document which
     *         represents the argument of the YANG <code>reference</code>
     *         substatement
     */
    @Nullable String getReference();

    /**
     * Returns status of the instance of the type <code>SchemaNode</code>
     *
     * @return status of this node which represents the argument of the YANG
     *         <code>status</code> substatement
     */
    @Nonnull Status getStatus();

    /**
     * Returns unknown schema nodes which belongs to this instance.
     *
     * @return list of unknown schema nodes defined under this node.
     */
    default @Nonnull List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return ImmutableList.of();
    }
}
