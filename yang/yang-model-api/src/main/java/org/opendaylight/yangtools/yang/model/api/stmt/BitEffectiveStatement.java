/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import java.util.NoSuchElementException;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

@Beta
public interface BitEffectiveStatement extends EffectiveStatement<String, BitStatement> {
    default long position() {
        try {
            return findFirstEffectiveSubstatementArgument(PositionEffectiveStatement.class).get();
        } catch (NoSuchElementException e) {
            throw new VerifyException("No position found in " + this, e);
        }
    }
}
