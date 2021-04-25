/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Namespace of available {@code typedef}s inside of an {@link EffectiveStatement}. It holds that statement's
 * {@code typedef} substatements. This constitutes the statament's contribution to the following in accordance with
 * RFC7950 section 6.2.1:
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
// FIXME: 7.0.0: add indexing of this namespace to yang-model-spi
public abstract class TypedefNamespace extends EffectiveStatementNamespace<TypedefEffectiveStatement> {
    private TypedefNamespace() {
        // Should never be instantiated
    }
}
