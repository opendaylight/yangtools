/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.uses;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;

/**
 * An extension of {@link EmptyLocalUsesEffectiveStatement}, which adds substatements to the mix. Since this means we
 * can also have refine statements, we keep a lazily-populated map of those.
 */
final class RegularLocalUsesEffectiveStatement extends EmptyLocalUsesEffectiveStatement {
    private final Object substatements;

    private volatile Map<Descendant, SchemaNode> refines;

    RegularLocalUsesEffectiveStatement(final UsesStatement declared, final GroupingDefinition sourceGrouping,
            final int flags, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, sourceGrouping, flags);
        this.substatements = maskList(substatements);
    }

    @Override
    public ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return unmaskList(substatements);
    }

    @Override
    public Map<Descendant, SchemaNode> getRefines() {
        final Map<Descendant, SchemaNode> local;
        return (local = refines) != null ? local : loadRefines();
    }

    private synchronized @NonNull Map<Descendant, SchemaNode> loadRefines() {
        Map<Descendant, SchemaNode> local = refines;
        if (local == null) {
            refines = local = UsesStatementSupport.indexRefines(effectiveSubstatements());
        }
        return local;
    }
}
