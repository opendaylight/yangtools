/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.model.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

/**
 * Represents the effect of 'default-deny-all' extension, as defined in
 * <a href="https://tools.ietf.org/html/rfc6536">RFC6536</a>, being attached to a SchemaNode.
 */
@Beta
public interface DefaultDenyAllSchemaNode extends UnknownSchemaNode {
    /**
     * Attempt to find a {@link DefaultDenyAllSchemaNode} in a parent {@link DataSchemaNode}.
     *
     * @param parent Parent to search
     * @return {@link DefaultDenyAllSchemaNode} child, if present.
     */
    static Optional<DefaultDenyAllSchemaNode> findIn(final DataSchemaNode parent) {
        return parent.getUnknownSchemaNodes().stream().filter(DefaultDenyAllSchemaNode.class::isInstance).findAny()
                .map(DefaultDenyAllSchemaNode.class::cast);
    }
}
