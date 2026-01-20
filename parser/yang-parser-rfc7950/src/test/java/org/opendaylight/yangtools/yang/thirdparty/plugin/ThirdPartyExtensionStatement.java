/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.thirdparty.plugin;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

/**
 * Public declared statement definition that this plugin supports. This interface should generally depend only on
 * concepts defined in yang-model-api.
 */
@NonNullByDefault
public interface ThirdPartyExtensionStatement extends UnknownStatement<String> {
    /**
     * The definition of {@code nc:get-filter-element-attributes} statement.
     *
     * @since 15.0.0
     */
    StatementDefinition DEFINITION = StatementDefinition.of(
        ThirdPartyExtensionStatement.class, ThirdPartyExtensionEffectiveStatement.class,
        QNameModule.of("urn:opendaylight:yang:extension:third-party", "2016-06-09"),
        "third-party-extension", "argument-name");

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }
}
