/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigHashedValueStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@NonNullByDefault
public final class EncryptedValueStatementSupport extends AbstractHashedValueStatementSupport {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(OpenConfigHashedValueStatement.ENCRYPTED_DEFINITION).build();

    public EncryptedValueStatementSupport(final YangParserConfiguration config) {
        super(OpenConfigHashedValueStatement.ENCRYPTED_DEFINITION, config, VALIDATOR);
    }
}
