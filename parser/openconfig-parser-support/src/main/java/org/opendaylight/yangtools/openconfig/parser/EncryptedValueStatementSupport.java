/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigConstants;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigHashedValueEffectiveStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigHashedValueStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@NonNullByDefault
public final class EncryptedValueStatementSupport extends AbstractHashedValueStatementSupport {
    /**
     * The definition of {@code oc-ext:openconfig-encrypted-value} statement.
     *
     * @since 15.0.0
     */
    public static final StatementDefinition DEF = StatementDefinition.of(
        OpenConfigHashedValueStatement.class, OpenConfigHashedValueEffectiveStatement.class,
        OpenConfigConstants.ENCRYPTED_VALUE_MODULE, "openconfig-encrypted-value");

    private static final SubstatementValidator VALIDATOR = SubstatementValidator.builder(DEF).build();

    public EncryptedValueStatementSupport(final YangParserConfiguration config) {
        super(DEF, config, VALIDATOR);
    }
}
