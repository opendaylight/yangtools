/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;

/**
 * Module namespace. All modules known to the reactor are populated to this namespace. Each module is identified
 * by a {@link ModuleIdentifier}.
 */
public interface ModuleNamespace extends
        StatementNamespace<ModuleIdentifier, ModuleStatement, EffectiveStatement<String, ModuleStatement>> {
}
