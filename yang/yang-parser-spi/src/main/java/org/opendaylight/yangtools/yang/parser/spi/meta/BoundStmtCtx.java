/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link CommonStmtCtx} which has additionally been bound to a {@link StatementSupport}. It provides
 * {@link #argument()} as interpreted by that support.
 *
 * @param <A> Argument type
 */
@Beta
public interface BoundStmtCtx<A> extends CommonStmtCtx {
    /**
     * Return the statement argument.
     *
     * @return statement argument, or null if this statement does not have an argument
     */
    @Nullable A argument();

    /**
     * Return the statement argument in literal format.
     *
     * @return raw statement argument string
     * @throws VerifyException if this statement does not have an argument
     */
    default @NonNull A getArgument() {
        return verifyNotNull(argument(), "Attempted to use non-existent argument of %s", this);
    }
}
