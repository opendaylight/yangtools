/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Interface implemented by all {@link EffectiveStatement}s which can contain a {@code typedef} child.
 *
 * @param <A> Argument type
 * @param <D> Class representing declared version of this statement.
 */
public interface TypedefAwareEffectiveStatement<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
    /**
     * Mapping of {@code typedef}s defined within this node. It holds that statement's {@code typedef} substatements.
     * This constitutes the statament's contribution to the following in accordance with RFC7950 section 6.2.1:
     * <pre>
     *     All derived type names defined within a parent node or at the top
     *     level of the module or its submodules share the same type
     *     identifier namespace.  This namespace is scoped to all descendant
     *     nodes of the parent node or module.  This means that any
     *     descendant node may use that typedef, and it MUST NOT define a
     *     typedef with the same name.
     * </pre>
     *
     * @return All {@link TypedefEffectiveStatement}s defined in this module
     */
    default @NonNull Collection<TypedefEffectiveStatement> typedefs() {
        return collectEffectiveSubstatements(TypedefEffectiveStatement.class);
    }

    /**
     * Find a {@link TypedefEffectiveStatement} based on its {@link TypedefEffectiveStatement#argument()}.
     *
     * @param qname Typedef name
     * @return {@link TypedefEffectiveStatement}, or empty
     */
    default @NonNull Optional<TypedefEffectiveStatement> findTypedef(final @NonNull QName qname) {
        return streamEffectiveSubstatements(TypedefEffectiveStatement.class)
            .filter(typedef -> qname.equals(typedef.argument()))
            .findAny();
    }
}
