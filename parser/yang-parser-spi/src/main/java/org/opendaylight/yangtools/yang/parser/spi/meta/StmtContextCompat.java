/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Intermediate compatibility interface between {@link StmtContext} and {@link EffectiveStmtCtx.Current}.
 */
@Beta
public sealed interface StmtContextCompat extends CommonStmtCtx permits EffectiveStmtCtx, BoundStmtCtxCompat {
    /**
     * Return the executive summary of the copy process that has produced this context.
     *
     * @return A simplified summary of the copy process.
     */
    @NonNull CopyHistory history();
}
