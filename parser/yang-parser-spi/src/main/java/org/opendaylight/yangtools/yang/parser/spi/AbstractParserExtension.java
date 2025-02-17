/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Abstract base class for {@link ParserExtension} implementations.
 */
@NonNullByDefault
public abstract class AbstractParserExtension implements ParserExtension {
    private final ImmutableSet<StatementDefinition> supportedStatements;

    /**
     * Construct an instance supporting a single statement.
     *
     * @param supportedStatement the supported statement
     */
    protected AbstractParserExtension(final StatementDefinition supportedStatement) {
        this(ImmutableSet.of(supportedStatement));
    }

    /**
     * Construct an instance supporting multiple statements.
     *
     * @param supportedStatement the supported statement
     * @throws IllegalArgumentException if {@code supportedStatements} is empty
     */
    protected AbstractParserExtension(final StatementDefinition... supportedStatements) {
        this(ImmutableSet.copyOf(supportedStatements));
    }

    /**
     * Construct an instance supporting multiple statements.
     *
     * @param supportedStatements the supported statements
     * @throws IllegalArgumentException if {@code supportedStatements} is empty
     */
    protected AbstractParserExtension(final ImmutableSet<StatementDefinition> supportedStatements) {
        if (supportedStatements.isEmpty()) {
            throw new IllegalArgumentException("supportedStatements must not be empty");
        }
        this.supportedStatements = requireNonNull(supportedStatements);
    }

    @Override
    public final ImmutableSet<StatementDefinition> supportedStatements() {
        return supportedStatements;
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return super.equals(obj);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    /**
     * Add any extension-specific attributes. The default implementation adds {@link #supportedStatements()}.
     *
     * @param helper the {@link ToStringHelper}, ignoring {@code null} values
     * @return the helper
     */
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("supportedStatements", supportedStatements);
    }
}
