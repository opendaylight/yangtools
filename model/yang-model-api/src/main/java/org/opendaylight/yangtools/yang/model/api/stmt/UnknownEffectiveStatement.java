/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Effective counterpart to {@link UnknownStatement}. This interface exists only for the purposes of providing a bridge
 * from {@link UnknownSchemaNode} to {@link EffectiveStatement} via {@link UnknownSchemaNode#asEffectiveStatement()}.
 *
 * @param <A> Argument type ({@link Empty} if statement does not have argument.)
 * @param <D> Class representing declared version of this statement.
 */
// FIXME: remove this interface once UnknownSchemaNode is gone
public interface UnknownEffectiveStatement<A, D extends UnknownStatement<A>> extends EffectiveStatement<A, D> {

}
