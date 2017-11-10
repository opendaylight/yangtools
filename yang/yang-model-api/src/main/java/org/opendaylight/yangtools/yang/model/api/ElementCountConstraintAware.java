/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import java.util.Optional;

/**
 * Common interface for list-like nodes, which can optionally have constraints on the number of direct children.
 *
 * @author Robert Varga
 */
@Beta
public interface ElementCountConstraintAware {
    /**
     * Return the constraint on the number of child nodes.
     *
     * @return the constraint on the number of child nodes, if applicable.
     */
    Optional<ElementCountConstraint> getElementCountConstraint();
}
