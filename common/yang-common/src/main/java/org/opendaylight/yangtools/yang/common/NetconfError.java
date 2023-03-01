/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.HierarchicalIdentifier;

/**
 * Baseline interface for metadata associated with a NETCONF notion of an 'error'. Except what NETCONF regards as
 * an 'error', really means 'notable event' in that it can either be a warning or an error. No warnings were defined
 * at the time this distinction was made.
 */
@Beta
@NonNullByDefault
public interface NetconfError {
    /**
     * Return this error's severity.
     *
     * @return Error severity.
     */
    ErrorSeverity severity();

    /**
     * Return this error's type.
     *
     * @return Error type.
     */
    ErrorType type();

    /**
     * Return this error's tag.
     *
     * @return Error tag.
     */
    ErrorTag tag();

    /**
     * Return this errors's {@code error-message}, if available. This value is expected to be defined in a YANG model
     * through a {@code error-message} statement.
     *
     * @return Event message, or {@code null}.
     */
    @Nullable String message();

    /**
     * Return this error's {@code error-app-tag}, if available. This value is expected to be defined in a YANG model
     * through a {@code error-app-tag} statement.
     *
     * @return Application tag, or {@code null}.
     */
    @Nullable String appTag();

    /**
     * Return the path which triggered this error, if available.
     *
     * @return Triggering path, or {@code null}.
     */
    @Nullable HierarchicalIdentifier<?> path();

    /**
     * Return this error's additional info.
     *
     * @return Additional info.
     */
    List<? extends ErrorInfoRepresentation> info();
}
