/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.TopologicalSort.NodeImpl;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionEffectiveStatement;

/**
 * Sort a set of {@link ModuleEffectiveStatement}s according to their interdependencies.
 */
@Beta
public final class ModuleDependencySort {
    private static final class ModuleEntry extends NodeImpl {
        private final ModuleEffectiveStatement module;
        private final Revision revision;

        ModuleEntry(final ModuleEffectiveStatement module) {
            this.module = requireNonNull(module);
            revision = module.streamEffectiveSubstatements(RevisionEffectiveStatement.class)
                .map(RevisionEffectiveStatement::argument)
                .max(Revision::compareTo)
                .orElse(null);
        }

        ModuleEffectiveStatement module() {
            return module;
        }

        Optional<Revision> revision() {
            return Optional.ofNullable(revision);
        }
    }

    private ModuleDependencySort() {
        // Hidden on purpose
    }

    public static @NonNull List<ModuleEffectiveStatement> sortModuleStatements(
            final Collection<ModuleEffectiveStatement> modules) {

        // First pass: index modules into a table, so we have a name -> revision -> stmt mapping. This is needed to
        //             accurately interpret revision-less imports.
        final Table<UnqualifiedQName, Optional<Revision>, ModuleEntry> table = HashBasedTable.create();
        for (ModuleEffectiveStatement module : modules) {
            final ModuleEntry entry = new ModuleEntry(module);
            final Map<Optional<Revision>, ModuleEntry> row = table.row(module.argument());
            final ModuleEntry prev = row.putIfAbsent(entry.revision(), entry);
            if (prev != null) {
                checkArgument(module.equals(prev.module()),
                    "Module %s overlaps on name/revision with %s", module, prev.module());
            }


        }




    }
}
