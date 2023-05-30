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
     * Returns comment string associated with member.
     *
     * @return comment string associated with member.
     */
    @Nullable TypeMemberComment getComment();

    /**
     * Returns List of annotation definitions associated with generated type.
     *
     * @return List of annotation definitions associated with generated type.
     */
    List<AnnotationType> getAnnotations();

    /**
     * Returns the access modifier of member.
     *
     * @return the access modifier of member.
     */
    AccessModifier getAccessModifier();

    /**
     * Returns <code>true</code> if member is declared as static.
     *
     * @return <code>true</code> if member is declared as static.
     */
    boolean isStatic();

    /**
     * Returns <code>true</code> if member is declared as final.
     *
     * @return <code>true</code> if member is declared as final.
     */
    boolean isFinal();

    /**
     * Get the returning Type of member.
     *
     * @return the returning Type of member.
     */
    @NonNull Type getReturnType();

    /**
     * Returns the name of member.
     *
     * @return the name of member.
     */
    @NonNull String getName();
}
