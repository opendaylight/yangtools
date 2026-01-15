/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;

public final class EmptyChoiceEffectiveStatement extends AbstractChoiceEffectiveStatement {
    public EmptyChoiceEffectiveStatement(final ChoiceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final int flags) {
        super(declared, substatements, flags);
    }

    public EmptyChoiceEffectiveStatement(final AbstractChoiceEffectiveStatement original, final int flags) {
        super(original, flags);
    }

    @Override
    public QName argument() {
        return declared().argument();
    }

    @Override
    public Optional<CaseSchemaNode> getDefaultCase() {
        return Optional.empty();
    }
}
