/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Common interface for variables and methods in class.
 */
// FIXME: seal this class
public interface TypeMember {
    /**
     * {@return the {@link TypeMemberComment} associated with member}
     * @implSpec Default implementation returns {@code null}
     */
    default @Nullable TypeMemberComment getComment() {
        return null;
    }

    /**
     * {@return List of annotation definitions attached to this member}
     * @implSpec Default implementation returns an empty list
     */
    @NonNullByDefault
    default List<AttachedAnnotation> getAnnotations() {
        return List.of();
    }

    /**
     * {@return the access modifier of member}
     * @implSpec Default implementation returns {@link AccessModifier#PUBLIC}
     */
    @NonNullByDefault
    default AccessModifier getAccessModifier() {
        return AccessModifier.PUBLIC;
    }

    /**
     * {@return the returning {@link Type} of member}
     */
    @NonNullByDefault
    Type getReturnType();

    /**
     * {@return the name of member}
     */
    @NonNullByDefault
    String getName();
}
