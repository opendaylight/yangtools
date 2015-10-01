package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.InstanceIdentifierSpecification;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RequireInstanceEffectiveStatementImpl;

final class DerivedInstanceIdentifierEffectiveStatement extends AbstractTypeEffectiveStatement<InstanceIdentifierSpecification, InstanceIdentifierTypeDefinition>
        implements InstanceIdentifierTypeDefinition {

    private boolean requireInstance;

    DerivedInstanceIdentifierEffectiveStatement(final EffectiveStatement<?, InstanceIdentifierSpecification> stmt,
            final SchemaPath path, final InstanceIdentifierTypeDefinition baseType) {
        super(stmt, path, baseType);

        RequireInstanceEffectiveStatementImpl maybeRequire = findEffective(stmt, RequireInstanceEffectiveStatementImpl.class);
        if (maybeRequire != null) {
            requireInstance = maybeRequire.argument();

            if (baseType.requireInstance()) {
                Preconditions.checkArgument(requireInstance, "Attempted to weaken require-instance from %s", baseType);
            }
        }
    }

    @Override
    public TypeEffectiveStatement<InstanceIdentifierSpecification> derive(
            final EffectiveStatement<?, InstanceIdentifierSpecification> stmt, final SchemaPath path) {
        return new DerivedInstanceIdentifierEffectiveStatement(stmt, path, this);
    }

    @Deprecated
    @Override
    public RevisionAwareXPath getPathStatement() {
        return getBaseType().getPathStatement();
    }

    @Override
    public boolean requireInstance() {
        return requireInstance;
    }
}
