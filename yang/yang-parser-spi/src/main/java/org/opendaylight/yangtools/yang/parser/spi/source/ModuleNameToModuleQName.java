/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

/**
 * Source-specific mapping of prefixes to namespaces.
 */
public final class ModuleNameToModuleQName extends AbstractParserNamespace<String, QNameModule> {
    public static final @NonNull ModuleNameToModuleQName INSTANCE = new ModuleNameToModuleQName();

    private ModuleNameToModuleQName() {
        super(ModelProcessingPhase.SOURCE_LINKAGE, NamespaceBehaviour.sourceLocal(ModuleNameToModuleQName.class));
    }
}
