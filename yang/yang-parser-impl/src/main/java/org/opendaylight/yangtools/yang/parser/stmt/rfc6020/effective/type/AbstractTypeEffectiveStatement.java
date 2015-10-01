package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.parser.util.TypeConstraints;

abstract class AbstractTypeEffectiveStatement<S extends TypeStatement, T extends TypeDefinition<T>>
        implements TypeEffectiveStatement<S>, TypeDefinition<T> {
    private final EffectiveStatement<?, S> stmt;
    private final SchemaPath path;
    private final String defaultValue;
    private final String description;
    private final String reference;
    private final Status status;
    private final String units;
    private final T baseType;

    AbstractTypeEffectiveStatement(final EffectiveStatement<?, S> stmt, final SchemaPath path, final T baseType) {
        this.stmt = Preconditions.checkNotNull(stmt);
        this.path = Preconditions.checkNotNull(path);
        this.baseType = Preconditions.checkNotNull(baseType);

        defaultValue = findOrDefault(stmt, DefaultStatement.class, String.class, baseType.getDefaultValue());
        description = findOrDefault(stmt, DescriptionStatement.class, String.class, baseType.getDescription());
        reference = findOrDefault(stmt, ReferenceStatement.class, String.class, baseType.getReference());
        status = findOrDefault(stmt, StatusStatement.class, Status.class, baseType.getStatus());
        units = findOrDefault(stmt, UnitsStatement.class, String.class, baseType.getUnits());
    }

    static final <T> T findEffective(final EffectiveStatement<?, ?> stmt, final Class<T> statement) {
        for (EffectiveStatement<?, ?> s : stmt.effectiveSubstatements()) {
            if (statement.isInstance(s)) {
                return statement.cast(s);
            }
        }

        return null;
     }

    private static final <T> T findOrDefault(final EffectiveStatement<?, ?> stmt, final Class<?> statement, final Class<T> valueClass, final Object value) {
        for (EffectiveStatement<?, ?> s : stmt.effectiveSubstatements()) {
            if (statement.isInstance(s.getDeclared())) {
                return valueClass.cast(s.argument());
            }
        }
        return valueClass.cast(value);
    }


    static List<LengthConstraint> calculateLengths(final EffectiveStatement<?, ?> stmt,
            final List<LengthConstraint> base) {
        final LengthEffectiveStatementImpl s = findEffective(stmt, LengthEffectiveStatementImpl.class);
        if (s != null) {
            final List<LengthConstraint> dr = s.argument();
            if (!dr.isEmpty()) {
                // FIXME: get source reference
                final TypeConstraints constraints = new TypeConstraints("foo", 4);
                constraints.addLengths(base);
                constraints.addLengths(dr);
                constraints.validateConstraints();
                return constraints.getLength();
            }
        }

        return base;
    }

    static List<RangeConstraint> calculateRanges(final EffectiveStatement<?, ?> stmt, final List<RangeConstraint> base) {
        final RangeEffectiveStatementImpl s = findEffective(stmt, RangeEffectiveStatementImpl.class);
        if (s != null) {
            final List<RangeConstraint> dr = s.argument();
            if (!dr.isEmpty()) {
                // FIXME: get source reference
                final TypeConstraints constraints = new TypeConstraints("foo", 4);
                constraints.addRanges(base);
                constraints.addRanges(dr);
                constraints.validateConstraints();
                return constraints.getRange();
            }
        }

        return base;
    }

    @Override
    public final S getDeclared() {
        return stmt.getDeclared();
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> V get(final Class<N> namespace, final K identifier) {
        return stmt.get(namespace, identifier);
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        return stmt.getAll(namespace);
    }

    @Override
    public final Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return stmt.effectiveSubstatements();
    }

    @Override
    public final StatementDefinition statementDefinition() {
        return stmt.statementDefinition();
    }

    @Override
    public final String argument() {
        return getQName().getLocalName();
    }

    @Override
    public final StatementSource getStatementSource() {
        return stmt.getStatementSource();
    }

    @Override
    public final String getUnits() {
        return units;
    }

    @Override
    public final Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public final QName getQName() {
        return path.getLastComponent();
    }

    @Override
    public final SchemaPath getPath() {
        return path;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final String getDescription() {
        return description;
    }

    @Override
    public final String getReference() {
        return reference;
    }

    @Override
    public final Status getStatus() {
        return status;
    }

    @Override
    public final T getBaseType() {
        return baseType;
    }
}
