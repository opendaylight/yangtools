/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.ir;

import java.io.DataOutput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link YodlConstants#YODL_MEDIA_TYPE} version.
 */
@NonNullByDefault
enum YodlVersion {
    /**
     * Version 1 YODL file, as described by {@link IOConstantsV1}.
     */
    V1((byte) 1) {
        @Override
        StatementOutput newOutput(final DataOutput out) throws IOException {
            return new StatementOutputV1(out);
        }
    };

    private final byte versionByte;

    YodlVersion(final byte versionByte) {
        this.versionByte = versionByte;
    }

    byte versionByte() {
        return versionByte;
    }

    abstract StatementOutput newOutput(DataOutput out) throws IOException;
}
