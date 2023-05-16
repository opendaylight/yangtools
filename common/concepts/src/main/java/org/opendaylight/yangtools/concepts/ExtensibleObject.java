/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface specifying access to extensions attached to a particular object. This functionality is loosely based on
 * <a href="https://docs.microsoft.com/en-us/dotnet/framework/wcf/extending/extensible-objects">Extensible Object</a>
 * pattern.
 *
 * @param <O> Type of extensible object
 * @param <E> Extension marker interface
 */
public interface ExtensibleObject<O extends ExtensibleObject<O, E>, E extends ObjectExtension<O, E>> {
    /**
     * Return an extension interface, if currently available.
     *
     * @implSpec
     *     Default implementation defers to linear search of {@link #supportedExtensions()}.
     *
     * @param <T> Extension type
     * @param type Extension type class
     * @return An extension instance, or {@code null}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    default <T extends E> @Nullable T extension(final Class<T> type) {
        final var nonnull = requireNonNull(type);
        return supportedExtensions().stream().filter(nonnull::isInstance).findFirst().map(nonnull::cast).orElse(null);
    }

    default <T extends E> Optional<T> findExtension(final Class<T> type) {
        return Optional.ofNullable(extension(type));
    }

    /**
     * Return currently-supported extensions. Note that the returned collection may change if this object is mutable.
     *
     * @implSpec
     *     Default implementations returns an empty List.
     *
     * @return Supported extensions
     */
    default @NonNull Collection<? extends E> supportedExtensions() {
        return List.of();
    }
}
