/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace.TreeScoped;

/**
 * Derived types namespace. All derived type names defined within a parent node or at the top level of the module
 * (or its submodules) share the same type identifier namespace.
 *
 * <p>
 * This namespace is scoped to all descendant nodes of the parent node or module. This means that any descendant node
 * may use that typedef, and it MUST NOT define a typedef with the same name.
 *
 * <p>
 * This namespace includes all type definitions implied by the language in which the current statement resides
 * (e.g. RFC6020/RFC7950 for YANG 1.0/1.1).
 */
public interface TypeNamespace extends TreeScoped<QName, TypedefStatement, TypedefEffectiveStatement> {

}
