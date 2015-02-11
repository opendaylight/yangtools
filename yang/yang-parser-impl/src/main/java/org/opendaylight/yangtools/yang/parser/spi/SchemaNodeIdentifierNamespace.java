/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;

/**
 * Schema node identifier namespace
 *
 * All leafs, leaf-lists, lists, containers, choices, rpcs, notifications, and
 * anyxmls defined (directly or through a uses statement) within a parent node
 * or at the top level of the module or its submodules share the same identifier
 * namespace. This namespace is scoped to the parent node or module, unless the
 * parent node is a case node. In that case, the namespace is scoped to the
 * closest ancestor node that is not a case or choice node.
 */
public interface SchemaNodeIdentifierNamespace extends StatementNamespace.TreeBased<SchemaNodeIdentifier, DeclaredStatement<?>,EffectiveStatement<?,DeclaredStatement<?>>> {

}
