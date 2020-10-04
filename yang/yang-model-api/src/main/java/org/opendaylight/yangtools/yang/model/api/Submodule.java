/*
 * Copyright (c) 2020 PANTHEO.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;

/**
 * This interface contains the methods for getting the data from the YANG submodule.
 */
public interface Submodule extends ModuleLike, EffectiveStatementEquivalent<SubmoduleEffectiveStatement> {

}
