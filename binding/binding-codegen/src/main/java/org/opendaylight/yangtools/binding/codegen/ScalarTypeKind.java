/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;

/**
 * The code generation shape of a {@link ScalarTypeObjectArchetype} for the purposes of generation of a
 * {@link ScalarTypeObjectTemplate}'s output class.
 */
// FIXME: this enum should be absorbed into a class hierarchy in ScalarTypeObjectArchetype
@NonNullByDefault
enum ScalarTypeKind {
    /**
     * This template generates the root class in a {@link ScalarTypeObject} hierarchy.
     */
    ROOT(true, false),
    /**
     * This template generates the root class in a {@link ScalarTypeObject} hierarchy and defines restrictions.
     */
    ROOT_RESTRICTING(true, true),
    /**
     * This template generates a subclass class in a {@link ScalarTypeObject} hierarchy and it, or any of its
     * superclasses, defines restrictions.
     */
    SUBCLASS(false, false),
    /**
     * This template generates a subclass class in a {@link ScalarTypeObject} hierarchy and one of its superclasses
     * defines restrictions.
     */
    SUBCLASS_INHERITING(false, true),
    /**
     * This template generates a subclass class in a {@link ScalarTypeObject} hierarchy and it defines the first
     * restrictions. This implies its root is {@link #ROOT} and all intermediate superclasses are {@link #SUBCLASS}.
     */
    SUBCLASS_RESTRICTING(false, true);

    private final boolean isRoot;
    private final boolean hasRestrictions;

    ScalarTypeKind(final boolean isRoot, final boolean hasRestrictions) {
        this.isRoot = isRoot;
        this.hasRestrictions = hasRestrictions;
    }

    /**
     * {@return {@code true} if the class represents the root of a {@link ScalarTypeObject} hierarchy}
     */
    boolean isRoot() {
        return isRoot;
    }

    /**
     * {@return {@code true} if the class or any of its superclasses have restrictions}
     */
    boolean hasRestrictions() {
        return hasRestrictions;
    }

    private static boolean hasRestrictions(final ScalarTypeObjectArchetype archetype) {
        return archetype.restrictions() != null;
    }

    static ScalarTypeKind of(final ScalarTypeObjectArchetype archetype) {
        final var ret = recursiveOf(archetype);
        if (ret != null) {
            return ret;
        }
        throw new VerifyException("Unexpected archetype " + archetype);
    }

    private static @Nullable ScalarTypeKind recursiveOf(final ScalarTypeObjectArchetype archetype) {
        final var superType = archetype.getSuperType();
        if (superType != null) {
            final var superClass = recursiveOf(superType);
            return superClass == null ? null : ofSubclass(archetype, superClass.hasRestrictions);
        }
        return hasRestrictions(archetype) ? ROOT_RESTRICTING : ROOT;
    }

    private static ScalarTypeKind ofSubclass(final ScalarTypeObjectArchetype archetype, final boolean superRestricted) {
        if (superRestricted) {
            return SUBCLASS_INHERITING;
        }
        return hasRestrictions(archetype) ? SUBCLASS_RESTRICTING : SUBCLASS;
    }
}
