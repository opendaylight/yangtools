/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviation;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.DeviateDefinition;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class DeviationEffectiveStatementImpl
        extends DeclaredEffectiveStatementBase<SchemaNodeIdentifier, DeviationStatement>
        implements Deviation, DeviationEffectiveStatement, Immutable {
    private final SchemaPath targetPath;
    private final String description;
    private final String reference;
    private final List<UnknownSchemaNode> unknownSchemaNodes;
    private final List<DeviateDefinition> deviateDefinitions;

    DeviationEffectiveStatementImpl(final StmtContext<SchemaNodeIdentifier, DeviationStatement, ?> ctx) {
        super(ctx);
        this.targetPath = ctx.getStatementArgument().asSchemaPath();

        this.deviateDefinitions = ImmutableList.copyOf(allSubstatementsOfType(DeviateDefinition.class));

        DescriptionEffectiveStatement descriptionStmt = firstEffective(DescriptionEffectiveStatement.class);
        this.description = descriptionStmt == null ? null : descriptionStmt.argument();

        ReferenceEffectiveStatement referenceStmt = firstEffective(ReferenceEffectiveStatement.class);
        this.reference = referenceStmt == null ? null : referenceStmt.argument();

        List<UnknownSchemaNode> unknownSchemaNodesInit = new ArrayList<>();
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                unknownSchemaNodesInit.add((UnknownSchemaNode) effectiveStatement);
            }
        }
        unknownSchemaNodes = ImmutableList.copyOf(unknownSchemaNodesInit);
    }

    @Override
    public SchemaPath getTargetPath() {
        return targetPath;
    }

    @Override
    public List<DeviateDefinition> getDeviates() {
        return deviateDefinitions;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    @Override
    public Optional<String> getReference() {
        return Optional.ofNullable(reference);
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownSchemaNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(targetPath);
        result = prime * result + Objects.hashCode(deviateDefinitions);
        result = prime * result + Objects.hashCode(description);
        result = prime * result + Objects.hashCode(reference);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DeviationEffectiveStatementImpl other = (DeviationEffectiveStatementImpl) obj;
        if (!Objects.equals(targetPath, other.targetPath)) {
            return false;
        }
        if (!Objects.equals(deviateDefinitions, other.deviateDefinitions)) {
            return false;
        }
        if (!Objects.equals(description, other.description)) {
            return false;
        }
        if (!Objects.equals(reference, other.reference)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return DeviationEffectiveStatementImpl.class.getSimpleName() + "["
                + "targetPath=" + targetPath
                + ", deviates=" + deviateDefinitions
                + ", description=" + description
                + ", reference=" + reference
                + "]";
    }
}
