/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;

/**
 * Intermediate-stage namespace equivalent to ModuleNamespace except it is keyed by module names. This namespace is
 * used to resolve inter-module references before actual linkage occurs.
 */
public interface PreLinkageModuleNamespace extends
        StatementNamespace<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> {
}
