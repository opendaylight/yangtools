/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Module-specific namespace for holding {@link StatementDefinition}s defined by extension statements. This namespace
 * is populated before full declaration phase.
 *
 * @author Robert Varga
 */
@Beta
public final class StatementDefinitionNamespace extends AbstractParserNamespace<QName, StatementSupport<?, ?, ?>> {
    public static final @NonNull StatementDefinitionNamespace INSTANCE = new StatementDefinitionNamespace();

    private StatementDefinitionNamespace() {
        super(ModelProcessingPhase.STATEMENT_DEFINITION, NamespaceBehaviour.global(StatementDefinitionNamespace.class));
    }
}
