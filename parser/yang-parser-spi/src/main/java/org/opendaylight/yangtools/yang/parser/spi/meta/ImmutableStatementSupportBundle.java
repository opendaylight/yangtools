/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangVersion;

record ImmutableStatementSupportBundle(
        StatementSupportBundle parent,
        ImmutableSet<YangVersion> supportedVersions,
        @NonNull ImmutableMap<QName, StatementSupport<?, ?, ?>> commonDefinitions,
        @NonNull ImmutableMap<ParserNamespace<?, ?>, NamespaceBehaviour<?, ?>> namespaceDefinitions,
        @NonNull ImmutableTable<YangVersion, QName, StatementSupport<?, ?, ?>> versionSpecificDefinitions)
        implements StatementSupportBundle {
    static final ImmutableStatementSupportBundle EMPTY = new ImmutableStatementSupportBundle(null, null,
        ImmutableMap.of(), ImmutableMap.of(), ImmutableTable.of());

    ImmutableStatementSupportBundle {
        requireNonNull(commonDefinitions);
        requireNonNull(namespaceDefinitions);
        requireNonNull(versionSpecificDefinitions);
    }
}
