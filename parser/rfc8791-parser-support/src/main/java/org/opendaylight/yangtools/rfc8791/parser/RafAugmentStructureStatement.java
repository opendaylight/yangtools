/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractRefStatement;

public class RafAugmentStructureStatement
        extends AbstractRefStatement<SchemaNodeIdentifier.Absolute, AugmentStructureStatement>
        implements AugmentStructureStatement {
    protected RafAugmentStructureStatement(AugmentStructureStatement delegate, DeclarationReference ref) {
        super(delegate, ref);
    }
}
