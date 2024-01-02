/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.api;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.spi.source.SourceIdentifier;

/**
 * A single <a href="https://www.rfc-editor.org/rfc/rfc8525">RFC8525</a> {@code module-set}.
 */
public record YangLibModuleSet(@NonNull String name,
        @NonNull ImmutableMap<Unqualified, YangLibModule> modules,
        @NonNull ImmutableMap<SourceIdentifier, YangLibModule> importOnlyModules) {
    public YangLibModuleSet {
        requireNonNull(name);
        requireNonNull(modules);
        requireNonNull(importOnlyModules);
    }
}
