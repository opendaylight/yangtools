/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.codec.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * A {@link Recv} receiving {@code rc:yang-data} as expressed by an {@link YangDataEffectiveStatement}.
 *
 * @since 16.2.0
 */
@NonNullByDefault
public final class RecvYangData extends ResultRecv {
    RecvYangData(final SchemaInferenceStack stack, final YangDataEffectiveStatement stmt) {
        super(stack);
    }
}
