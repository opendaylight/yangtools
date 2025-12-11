/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An entity that exposes a {@link StatementSourceReference} via {@link #sourceRef()}.
 */
@NonNullByDefault
public interface StatementSourceReferenceAware {
    /**
     * {@return the {@link StatementSourceReference}}
     */
    StatementSourceReference sourceRef();
}
