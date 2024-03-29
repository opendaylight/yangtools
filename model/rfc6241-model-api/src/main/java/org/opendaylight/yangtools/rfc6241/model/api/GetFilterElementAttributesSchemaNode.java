/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.model.api;

import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

/**
 * Represents the effect of 'get-filter-element-attributes' extension, as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc6241">RFC6241</a>.
 */
public interface GetFilterElementAttributesSchemaNode extends UnknownSchemaNode {
    /**
     * Attempt to find a {@link GetFilterElementAttributesSchemaNode} in a parent {@link AnyxmlSchemaNode}.
     *
     * @param parent Parent to search
     * @return {@link GetFilterElementAttributesSchemaNode} child, if present.
     */
    static Optional<GetFilterElementAttributesSchemaNode> findIn(final AnyxmlSchemaNode parent) {
        return parent.getUnknownSchemaNodes().stream().filter(GetFilterElementAttributesSchemaNode.class::isInstance)
                .findAny().map(GetFilterElementAttributesSchemaNode.class::cast);
    }

    @Override
    GetFilterElementAttributesEffectiveStatement asEffectiveStatement();
}
