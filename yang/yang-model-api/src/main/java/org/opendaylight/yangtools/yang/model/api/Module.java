/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

/**
 * This interface contains the methods for getting the data from the YANG module.
 */
public interface Module extends ModuleLike, EffectiveStatementEquivalent<ModuleEffectiveStatement> {

}
