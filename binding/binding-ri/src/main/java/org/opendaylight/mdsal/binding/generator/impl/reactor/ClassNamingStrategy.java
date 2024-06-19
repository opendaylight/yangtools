/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Enumeration of known strategies for translating a YANG node identifier into a Java package name segment or a Java
 * simple class name.
 */
abstract class ClassNamingStrategy implements Immutable {
    /**
     * Return the simple Java class name assigned by this naming strategy.
     *
     * @return Simple class name
     */
    abstract @NonNull String simpleClassName();

    /**
     * Return the fallback naming strategy. The fallback is used if this strategy ends up being insufficient in
     * assigning a unique name.
     *
     * @return Fallback strategy, {@code null} if there is no fallback.
     */
    abstract @Nullable ClassNamingStrategy fallback();

    // FIXME: document this method
    abstract @NonNull String rootName();

    /**
     * Return the {@code Java package name} allocated for any classes generated as children of this strategy.
     *
     * @return A package fragment name, for example {@code "foo.bar.baz"}
     */
    abstract @NonNull String childPackage();

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    abstract @NonNull ToStringHelper addToStringAttributes(@NonNull ToStringHelper helper);
}
