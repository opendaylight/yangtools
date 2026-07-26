/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Common interface for variables and methods in class.
 */
public sealed interface TypeMember permits MethodSignature {
    /**
     * {@return comment string associated with member}
     */
    @Nullable TypeMemberComment getComment();

    /**
     * {@return the returning {@link Type} of member}
     */
    @NonNull Type getReturnType();

    /**
     * {@return the name of member}
     */
    @NonNull String getName();
}
