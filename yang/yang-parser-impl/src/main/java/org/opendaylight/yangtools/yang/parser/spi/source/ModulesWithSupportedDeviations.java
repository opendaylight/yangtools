/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.spi.source;

import com.google.common.annotations.Beta;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 * Namespace used for storing information about modules that support deviation resolution
 */
@Beta
public interface ModulesWithSupportedDeviations
        extends IdentifierNamespace<ModulesWithSupportedDeviations.SupportedModules, Set<QNameModule>> {

    enum SupportedModules {
        SUPPORTED_MODULES
    }
}
