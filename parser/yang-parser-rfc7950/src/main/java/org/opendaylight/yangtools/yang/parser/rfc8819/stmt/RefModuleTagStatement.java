/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc8819.stmt;

import org.opendaylight.yangtools.concepts.Tag;
import org.opendaylight.yangtools.rfc8819.model.api.ModuleTagStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractRefStatement;

public class RefModuleTagStatement extends AbstractRefStatement<Tag, ModuleTagStatement>
        implements ModuleTagStatement {
    protected RefModuleTagStatement(ModuleTagStatement delegate, DeclarationReference ref) {
        super(delegate, ref);
    }
}
