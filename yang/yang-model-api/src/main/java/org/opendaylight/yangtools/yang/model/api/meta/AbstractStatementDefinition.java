/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Abstract utility class to handle StatementArgumentDefinition aspect of {@link StatementDefinition}. Most users should
 * use this class or {@link DefaultStatementDefinition}.
 */
@Beta
@NonNullByDefault
public abstract class AbstractStatementDefinition implements StatementDefinition {
    private final QName statementName;
    private final @Nullable QName argumentName;
    private final boolean yinElement;

    protected AbstractStatementDefinition(final QName statementName) {
        this(statementName, false, null);
    }

    protected AbstractStatementDefinition(final QName statementName, final boolean yinElement,
        final @Nullable QName argumentName) {
        this.statementName = requireNonNull(statementName);
        this.yinElement = yinElement;
        this.argumentName = argumentName;
    }

    @Override
    public final QName getStatementName() {
        return statementName;
    }

    @Override
    public final Optional<ArgumentDefinition> getArgumentDefinition() {
        return ArgumentDefinition.ofNullable(argumentName, yinElement);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        helper.add("name", statementName);
        if (argumentName != null) {
            helper.add("argument", argumentName).add("yin-element", yinElement);
        }
        return helper;
    }
}
