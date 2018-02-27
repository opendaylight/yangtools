/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;

/**
 * Namespace of data-bearing child nodes. According to RFC7950 section 6.2.1:
 * <pre>
 *     All leafs, leaf-lists, lists, containers, choices, rpcs, actions,
 *     notifications, anydatas, and anyxmls defined (directly or through
 *     a "uses" statement) within a parent node or at the top level of
 *     the module or its submodules share the same identifier namespace.
 *     This namespace is scoped to the parent node or module, unless the
 *     parent node is a case node.  In that case, the namespace is scoped
 *     to the closest ancestor node that is not a case or choice node.
 * </pre>
 *
 * <p>
 * This corresponds to the {@code schema tree} view of a YANG-defined data.
 *
 * @param <T> Child statement type
 * @author Robert Varga
 */
@Beta
public abstract class SchemaTreeEffectiveStatementNamespace<T extends SchemaTreeEffectiveStatement<?>>
        extends EffectiveStatementNamespace<T> {
    private SchemaTreeEffectiveStatementNamespace() {
        // Should never be instantiated
    }
}
