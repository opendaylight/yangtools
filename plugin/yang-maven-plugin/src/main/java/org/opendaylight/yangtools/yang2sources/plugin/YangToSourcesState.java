/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.WritableObject;

/**
 * State of the result of a {@link YangToSourcesMojo} execution run.
 */
// FIXME: expand to capture:
//        - input YANG files
//        - code generators and their config
record YangToSourcesState(@NonNull FileStateSet outputFiles) implements WritableObject {
    YangToSourcesState {
        requireNonNull(outputFiles);
    }

    static @NonNull YangToSourcesState readFrom(final DataInput in) throws IOException {
        return new YangToSourcesState(FileStateSet.readFrom(in));
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        outputFiles.writeTo(out);
    }
}
