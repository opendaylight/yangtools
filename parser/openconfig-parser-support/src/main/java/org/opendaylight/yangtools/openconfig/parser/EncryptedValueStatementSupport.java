/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.parser;

import org.opendaylight.yangtools.openconfig.model.api.OpenConfigStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class EncryptedValueStatementSupport extends AbstractHashedValueStatementSupport {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(OpenConfigStatements.OPENCONFIG_ENCRYPTED_VALUE).build();

    public EncryptedValueStatementSupport(final YangParserConfiguration config) {
        super(OpenConfigStatements.OPENCONFIG_ENCRYPTED_VALUE, config, VALIDATOR);
    }
}
