/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;

/**
 * A single <a href="https://www.rfc-editor.org/rfc/rfc8525">RFC8525</a> {@code submodule} list entry.
 *
 * @param identifier {@link SourceIdentifier} of this submodule, e.g. the combination of {@code name} and
 *        {@code revision}
 * @param source A {@link SourceRepresentation} of the submodule
 */
public record YangLibSubmodule(@NonNull SourceIdentifier identifier, @NonNull SourceRepresentation source) {
    public YangLibSubmodule {
        requireNonNull(identifier);
        requireNonNull(source);
    }
}
