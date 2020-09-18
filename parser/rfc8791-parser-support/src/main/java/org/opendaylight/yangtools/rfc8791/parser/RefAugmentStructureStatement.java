/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureArgument;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractRefStatement;

final class RefAugmentStructureStatement
        extends AbstractRefStatement<AugmentStructureArgument, AugmentStructureStatement>
        implements AugmentStructureStatement {
    RefAugmentStructureStatement(final AugmentStructureStatement delegate, final DeclarationReference ref) {
        super(delegate, ref);
    }
}
