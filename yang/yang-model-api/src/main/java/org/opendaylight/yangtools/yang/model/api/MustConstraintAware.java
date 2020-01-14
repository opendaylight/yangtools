/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Collection;

/**
 * Mix-in interface for nodes which can define must constraints.
 *
 * @author Robert Varga
 */
public interface MustConstraintAware {
    /**
     * Specifies the rules which the node which contains <code>must</code> YANG substatement has to match.
     *
     * @return collection of <code>MustDefinition</code> (XPath) instances which represents the concrete data
     *         constraints
     */
    Collection<? extends MustDefinition> getMustConstraints();
}
