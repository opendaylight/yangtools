/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;

import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Internal shared helpers.
 * @author Robert Varga
 *
 */
final class ExportUtils {
    private ExportUtils() {
        // Hidden on purpose
    }

    static Optional<String> statementPrefix(final StatementPrefixResolver resolver, final QName stmtName,
            final DeclaredStatement<?> stmt) {
        final QNameModule namespace = stmtName.getModule();
        if (YangConstants.RFC6020_YIN_MODULE.equals(namespace)) {
            return Optional.empty();
        }

        // Non-default namespace, a prefix is needed
        @Nullable String prefix = resolver.findPrefix(stmt);
        if (prefix == null && !namespace.getRevision().isPresent()) {
            // FIXME: this is an artifact of commonly-bound statements in parser, which means a statement's name
            //        does not have a Revision. We'll need to find a solution to this which is acceptable. There
            //        are multiple ways of fixing this:
            //        - perhaps EffectiveModuleStatement should be giving us a statement-to-EffectiveModule map?
            //        - or DeclaredStatement should provide the prefix?
            //        The second one seems cleaner, as that means we would not have perform any lookup at all...
            Entry<QNameModule, @NonNull String> match = null;
            for (Entry<QNameModule, @NonNull String> entry : resolver.entrySet()) {
                final QNameModule ns = entry.getKey();
                if (namespace.equals(ns.withoutRevision()) && (match == null
                        || Revision.compare(match.getKey().getRevision(), ns.getRevision()) < 0)) {
                    match = entry;
                }
            }

            if (match != null) {
                prefix = match.getValue();
            }
        }

        checkArgument(prefix != null, "Failed to find prefix for statement %s", stmtName);
        verify(!prefix.isEmpty(), "Empty prefix for statement %s", stmtName);
        return Optional.of(prefix);
    }
}
