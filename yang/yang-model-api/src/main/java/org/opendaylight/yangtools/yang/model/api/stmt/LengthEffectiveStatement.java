/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

@Beta
public interface LengthEffectiveStatement extends EffectiveStatement<List<ValueRange>, LengthStatement>,
    // FIXME: 6.0.0: reconsider this interface extension
    // FIXME: 6.0.0: if we are keeping it, consider a default implementation (shared with others effective statements)
    ConstraintMetaDefinition  {

}
