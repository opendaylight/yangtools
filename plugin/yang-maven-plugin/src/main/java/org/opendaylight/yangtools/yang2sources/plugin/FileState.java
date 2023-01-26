/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import java.nio.file.attribute.BasicFileAttributes;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Hash of a single file. {@link #size()} corresponds to {@link BasicFileAttributes#size()}.
 */
record FileState(@NonNull String path, long size, int crc32) {
    FileState {
        requireNonNull(path);
    }
}
