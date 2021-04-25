/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * {@link StatementSupport} trait for statements which create intermediate parent node for some of its child nodes.
 * An example of this is RFC6020/RFC7950 choice statement, which creates implicit case statements for child containers
 * and others.
 *
 * @author Robert Varga
 */
@Beta
public interface ImplicitParentAwareStatementSupport {
    /**
     * Returns implicit parent statement support for supplied statement definition, if it is defined. This allows
     * implementations of this interface add implicit parent to the build context hierarchy before a substatement
     * is created.
     *
     * @param stmtDef statement definition of substatement
     * @return optional of implicit parent statement support
     */
    Optional<StatementSupport<?, ?, ?>> getImplicitParentFor(StatementDefinition stmtDef);
}
