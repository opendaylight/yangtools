/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.ElementCountMatcher;

/**
 * Common interface for list-like nodes, which can optionally have constraints on the number of direct children.
 */
@Beta
public interface ElementCountConstraintAware {
    /**
     * {@return the {@code ElementCountMatcher}, or {@code null}}
     */
    @Nullable ElementCountMatcher elementCountMatcher();
}
