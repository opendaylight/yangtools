/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.HierarchicalIdentifier;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Baseline interface for metadata associated with a NETCONF notion of an 'error'. Except what NETCONF regards as
 * an 'error', really means 'notable event' in that it can either be a warning or an error. No warnings were defined
 * at the time this distinction was made.
 */
@Beta
public interface NetconfError<T extends NetconfError<T>> extends Immutable {
    /**
     * Return this error's severity.
     *
     * @return Error severity.
     */
    @NonNull ErrorSeverity severity();

    /**
     * Return this error's type.
     *
     * @return Error type.
     */
    @NonNull ErrorType type();

    /**
     * Return this error's tag.
     *
     * @return Error tag.
     */
    @NonNull ErrorTag tag();

    /**
     * Return this error's {@code error-app-tag}, if available. This value is expected to be defined in a YANG model
     * through a {@code error-app-tag} statement.
     *
     * @return Application tag, or null.
     */
    @Nullable String appTag();

    /**
     * Return this errors's {@code error-message}, if available. This value is expected to be defined in a YANG model
     * through a {@code error-message} statement.
     *
     * @return Event message, or null.
     */
    @Nullable String message();

    /**
     * Return the path which triggered this error, if available.
     *
     * @return Triggering path, or null.
     */
    @Nullable HierarchicalIdentifier<?> path();

    /**
     * Return this error's additional info, if available.
     *
     * @return Additional info, or null.
     */
    @Nullable ErrorInfo<?> info();

    /**
     * Return this object's representation class.
     *
     * @return This object's representation class.
     */
    @NonNull Class<T> representation();
}
