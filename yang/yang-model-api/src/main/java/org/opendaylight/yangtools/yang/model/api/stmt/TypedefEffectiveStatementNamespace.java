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
 * Namespace of available identities. According to RFC7950 section 6.2.1:
 * <pre>
 *     All derived type names defined within a parent node or at the top
 *     level of the module or its submodules share the same type
 *     identifier namespace.  This namespace is scoped to all descendant
 *     nodes of the parent node or module.  This means that any
 *     descendant node may use that typedef, and it MUST NOT define a
 *     typedef with the same name.
 * </pre>
 *
 * @author Robert Varga
 */
@Beta
public abstract class TypedefEffectiveStatementNamespace
        extends EffectiveStatementNamespace<TypedefEffectiveStatement> {
    private TypedefEffectiveStatementNamespace() {
        // Should never be instantiated
    }
}
