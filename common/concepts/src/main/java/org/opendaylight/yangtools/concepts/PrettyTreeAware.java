/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility interface to bridge structures which can be formatted via {@link PrettyTree}.
 */
@NonNullByDefault
public interface PrettyTreeAware {
    /**
     * Return a {@link PrettyTree} view of this object.
     *
     * @return A {@link PrettyTree}.
     */
    PrettyTree prettyTree();
}
