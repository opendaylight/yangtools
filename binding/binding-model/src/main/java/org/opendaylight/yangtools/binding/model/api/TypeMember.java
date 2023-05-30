/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Common interface for variables and methods in class.
 */
public interface TypeMember {
    /**
     * {@return comment string associated with member}
     */
    @Nullable TypeMemberComment getComment();

    /**
     * {@return List of annotation definitions associated with generated type}
     */
    @NonNull List<AnnotationType> getAnnotations();

    /**
     * {@return the access modifier of member}
     */
    AccessModifier getAccessModifier();

    /**
     * {@return {@code true} if member is declared as static}
     */
    boolean isStatic();

    /**
     * {@return {@code true} if member is declared as final}
     */
    boolean isFinal();

    /**
     * {@return the returning {@link Type} of member}
     */
    @NonNull Type getReturnType();

    /**
     * {@return the name of member}
     */
    @NonNull String getName();
}
