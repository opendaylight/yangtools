/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifiable;

/**
 * The Type interface defines the base type for all types defined in java. Each
 * Type defined in java MUST contain name and package name, except of primitive
 * types like int, byte etc. In case of mapping of primitive type the package
 * name MUST be left as empty string.
 */
public interface Type extends Identifiable<JavaTypeName> {
    /**
     * Returns name of the package that interface belongs to.
     *
     * @return name of the package that interface belongs to
     */
    default String getPackageName() {
        return getIdentifier().packageName();
    }

    /**
     * Returns name of the interface.
     *
     * @return name of the interface.
     */
    default String getName() {
        return getIdentifier().simpleName();
    }

    /**
     * Returns fully qualified name of Type. <br>
     * The fully qualified name of Type MUST be returned in following format:
     * <ul>
     * <li>If does not contains package name: [type name] (e.g. int, byte,
     * byte[],...)</li>
     * <li>If Type contains package name: [package name].[type name] (e.g
     * java.lang.Byte, org.opendaylight.controller.gen.GenType)</li>
     * </ul>
     *
     * @return fully qualified name of Type.
     */
    default String getFullyQualifiedName() {
        return getIdentifier().toString();
    }

    static @NonNull Type of(final JavaTypeName identifier) {
        return new DefaultType(identifier);
    }

    static @NonNull Type of(final Identifiable<JavaTypeName> type) {
        return of(type.getIdentifier());
    }

    static @NonNull Type of(final Class<?> type) {
        return of(JavaTypeName.create(type));
    }
}
