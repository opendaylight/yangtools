/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref;

import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractRefStatement;

public final class RefSubmoduleStatement extends AbstractRefStatement<UnqualifiedQName, SubmoduleStatement>
        implements SubmoduleStatement {
    public RefSubmoduleStatement(final SubmoduleStatement delegate, final DeclarationReference ref) {
        super(delegate, ref);
    }
}
