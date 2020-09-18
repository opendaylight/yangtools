/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import static org.opendaylight.yangtools.yang.common.YangVersion.VERSION_1;
import static org.opendaylight.yangtools.yang.common.YangVersion.VERSION_1_1;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;

@Beta
public final class IetfDataStructureSupport {
    private static final ImmutableSet<YangVersion> SUPPORTED_VERSIONS = Sets.immutableEnumSet(VERSION_1, VERSION_1_1);

    public static final @NonNull StatementSupportBundle BUNDLE = StatementSupportBundle.builder(SUPPORTED_VERSIONS)
            .addVersionSpecificSupport(VERSION_1, StructureStatementSupport.rfc6020())
            .addVersionSpecificSupport(VERSION_1_1, StructureStatementSupport.rfc7950())
            .addVersionSpecificSupport(VERSION_1, AugmentStructureStatementSupport.rfc6020())
            .addVersionSpecificSupport(VERSION_1_1, AugmentStructureStatementSupport.rfc7950())
            .build();

    private IetfDataStructureSupport() {
        // Hidden on purpose
    }
}
