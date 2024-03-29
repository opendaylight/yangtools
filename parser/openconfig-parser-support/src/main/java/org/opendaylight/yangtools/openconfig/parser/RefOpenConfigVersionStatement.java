/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.parser;

import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigVersionStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractRefStatement;

final class RefOpenConfigVersionStatement extends AbstractRefStatement<SemVer, OpenConfigVersionStatement>
        implements OpenConfigVersionStatement {
    RefOpenConfigVersionStatement(final OpenConfigVersionStatement delegate, final DeclarationReference ref) {
        super(delegate, ref);
    }
}
