/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Represents YANG action statement.
 *
 * <p>
 * The "action" statement is used to define an operation connected to a
 * specific container or list data node.  It takes one argument, which
 * is an identifier, followed by a block of substatements that holds
 * detailed action information.  The argument is the name of the action.
 */
@Beta
public interface ActionStatement extends DeclaredStatement<QName>, OperationGroup {

}
