/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DefaultEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DescriptionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ReferenceEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.StatusEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnitsEffectiveStatementImpl;

abstract class AbstractTypeDefinitionBuilder<T extends TypeDefinition<T>> implements TypeDefinitionBuilder<T> {
    private final Collection<UnknownSchemaNode> unknownSchemaNodes = new ArrayList<>();
    private TypeDefinition<?> baseType;
    private SchemaPath path;
    private String description;
    private String reference;
    private Status status = Status.CURRENT;
    private String defaultValue;
    private String units;

    @Nonnull protected final SchemaPath getPath() {
        Preconditions.checkState(path != null, "Path has not been set");
        return path;
    }

    @Nonnull protected final TypeDefinition<?> getBaseType() {
        Preconditions.checkState(baseType != null, "Base type has not been set");
        return baseType;
    }

    @Nullable protected final String getDefaultValue() {
        return defaultValue;
    }

    @Nullable protected final String getDescription() {
        return description;
    }

    @Nullable protected final String getReference() {
        return reference;
    }

    @Nullable protected final Status getStatus() {
        return status;
    }

    @Nullable protected final String getUnits() {
        return units;
    }

    @Nullable protected final Collection<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownSchemaNodes;
    }

    @Override
    public final AbstractTypeDefinitionBuilder<T> setPath(final SchemaPath path) {
        this.path = Preconditions.checkNotNull(path);
        return this;
    }

    @Override
    public final AbstractTypeDefinitionBuilder<T> setBaseType(final TypeDefinition<?> baseType) {
        this.baseType = Preconditions.checkNotNull(baseType);
        return this;
    }

    @Override
    public final AbstractTypeDefinitionBuilder<T> addEffectiveStatements(
            final Collection<? extends EffectiveStatement<?, ?>> statements) {

        for (EffectiveStatement<?, ?> stmt : statements) {
            Preconditions.checkNotNull(stmt, "Statements {} includes a null element", statements);

            if (stmt instanceof DefaultEffectiveStatementImpl) {
                defaultValue = ((DefaultEffectiveStatementImpl)stmt).argument();
            } else if (stmt instanceof DescriptionEffectiveStatementImpl) {
                description = ((DescriptionEffectiveStatementImpl) stmt).argument();
            } else if (stmt instanceof ReferenceEffectiveStatementImpl) {
                reference = ((ReferenceEffectiveStatementImpl) stmt).argument();
            } else if (stmt instanceof StatusEffectiveStatementImpl) {
                status = ((StatusEffectiveStatementImpl) stmt).argument();
            } else if (stmt instanceof UnitsEffectiveStatementImpl) {
                units = ((UnitsEffectiveStatementImpl) stmt).argument();
            } else if (stmt instanceof UnknownSchemaNode) {
                unknownSchemaNodes.add((UnknownSchemaNode) stmt);
            } else {
                addEffectiveStatement(Preconditions.checkNotNull(stmt));
            }
        }
        return this;
    }

    /**
     * Add the effects of a single statement into the resulting type definition. The contract is the same as
     * {@link #addEffectiveStatements(Collection)}, but any statements consumed by this class will not be reported.
     *
     * @param stmt An effective statement
     */
    protected void addEffectiveStatement(@Nonnull final EffectiveStatement<?, ?> stmt) {
        // Default is a no-op
    }
}
