/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.thirdparty.plugin;

import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownEffectiveStatement;

/**
 * Public interface exposed to the effective world.
 *
 * @author Robert Varga
 */
public interface ThirdPartyExtensionEffectiveStatement
        extends UnknownEffectiveStatement<String, ThirdPartyExtensionStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return ThirdPartyExtensionsMapping.THIRD_PARTY_EXTENSION;
    }

    String getValueFromNamespace();
}
