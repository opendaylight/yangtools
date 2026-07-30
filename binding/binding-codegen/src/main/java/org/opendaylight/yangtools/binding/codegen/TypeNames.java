/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Well-known {@link JavaTypeName} constants which do not have a single user.
 */
@NonNullByDefault
final class TypeNames {

    static final JavaTypeName LISTENABLE_FUTURE = JavaTypeName.create(ListenableFuture.class);
    static final JavaTypeName RPC_RESULT = JavaTypeName.create(RpcResult.class);

    private TypeNames() {
        // hidden on purpose
    }
}
