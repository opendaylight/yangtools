/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import com.google.common.annotations.Beta;
import com.google.common.collect.SetMultimap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.GlobalIdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

/**
 * Namespace used for storing information about modules that support deviation resolution.
 * Map key (QNameModule) denotes a module which can be deviated by the modules specified in the Map value.
 */
@Beta
public interface ModulesDeviatedByModules
        extends GlobalIdentifierNamespace<ModulesDeviatedByModules.SupportedModules,
                    SetMultimap<QNameModule, QNameModule>> {
    NamespaceBehaviour<SupportedModules, SetMultimap<QNameModule, QNameModule>, @NonNull ModulesDeviatedByModules>
        BEHAVIOUR = NamespaceBehaviour.globalOf(ModulesDeviatedByModules.class);

    enum SupportedModules {
        SUPPORTED_MODULES
    }
}
