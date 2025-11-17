/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.model.api;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;

/**
 * Argument to an {@link AugmentStructureStatement}. This is similar to an {@link SchemaNodeIdentifier.Absolute}, except
 * the first {@link QName} is not a step in {@code schema node} tree, but rather matches a {@link StructureStatement}.
 *
 * @param structure the {@link QName} of the structure
 * @param descendant the structure descendant
 * @since 14.0.21
 */
@NonNullByDefault
public record AugmentStructureArgument(QName structure, SchemaNodeIdentifier.@Nullable Descendant descendant) {
    /**
     * Default constructor.
     *
     * @param structure the {@link QName} of the structure
     * @param descendant the structure descendant
     */
    public AugmentStructureArgument {
        requireNonNull(structure);
    }

    /**
     * Convenience constructor.
     *
     * @param structure the {@link QName} of the structure
     * @param descendant the {@link SchemaNodeIdentifier.Descendant} steps
     */
    public AugmentStructureArgument(final QName structure, final QName... descendant) {
        this(structure, descendant.length == 0 ? null : SchemaNodeIdentifier.Descendant.of(descendant));
    }

    @Override
    public String toString() {
        return "AugmentStructureArgument [qnames=" + qnames() + "]";
    }

    private List<?> qnames() {
        final var local = descendant;
        return local == null ? List.of(structure) : qnames(structure, local);
    }

    private static List<?> qnames(final QName structure, final SchemaNodeIdentifier.Descendant descandant) {
        final var qnames = descandant.getNodeIdentifiers();
        final var ret = new ArrayList<>(qnames.size() + 1);
        ret.add(structure);

        var prev = structure.getModule();
        for (var qname : qnames) {
            final var module = qname.getModule();
            ret.add(module.equals(prev) ? qname.getLocalName() : qname);
            prev = module;
        }

        return ret;
    }
}
