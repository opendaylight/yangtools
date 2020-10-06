/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

/**
 * {@link EffectiveStatement}-based result of YANG parser compilation. Unlike a SchemaContext, which it extends,
 * it gives access to individual {@link ModuleEffectiveStatement}s that comprise it.
 *
 * @author Robert Varga
 */
@Beta
// FIXME: 7.0.0: evaluate if we still need to extend SchemaContext here
public interface EffectiveModelContext extends SchemaContext {

    Map<QNameModule, ModuleEffectiveStatement> getModuleStatements();
}
