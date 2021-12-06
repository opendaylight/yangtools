/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;

public final class RegularChoiceEffectiveStatement extends AbstractChoiceEffectiveStatement {
    private final CaseSchemaNode defaultCase;
    private final @NonNull QName argument;

    public RegularChoiceEffectiveStatement(final ChoiceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final QName argument,
            final int flags, final @Nullable CaseSchemaNode defaultCase) {
        super(declared, substatements, flags);
        this.argument = requireNonNull(argument);
        this.defaultCase = defaultCase;
    }

    public RegularChoiceEffectiveStatement(final AbstractChoiceEffectiveStatement original, final QName argument,
            final int flags) {
        super(original, flags);
        this.argument = requireNonNull(argument);
        defaultCase = original.getDefaultCase().orElse(null);
    }

    @Override
    public QName argument() {
        return argument;
    }

    @Override
    public Optional<CaseSchemaNode> getDefaultCase() {
        return Optional.ofNullable(defaultCase);
    }
}
