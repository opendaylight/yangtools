/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.model.api;

import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

/**
 * Represents the effect of 'default-deny-all' extension, as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc6536">RFC6536</a>, being attached to a SchemaNode.
 */
public interface DefaultDenyAllSchemaNode extends UnknownSchemaNode {

    // RFC8341:
    //    The 'default-deny-write' extension MAY appear within a data
    //    definition statement.  It is ignored otherwise.";

    // RFC7951:
    //    o  data definition statement: A statement that defines new data
    //    nodes.  One of "container", "leaf", "leaf-list", "list", "choice",
    //    "case", "augment", "uses", "anydata", and "anyxml".

    // FIXME: audit implementation that the above is true

    @Override
    DefaultDenyAllEffectiveStatement asEffectiveStatement();
}
