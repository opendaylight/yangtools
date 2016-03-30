/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 * Namespace class for storing Maps of all modules with the same name. This namespace is
 * used only in case the semantic versioning is enabled, otherwise it is empty.
 */
public interface SemanticVersionModuleNamespace extends IdentifierNamespace<String, Map<ModuleIdentifier, StmtContext<?, ?, ?>>> {
}
