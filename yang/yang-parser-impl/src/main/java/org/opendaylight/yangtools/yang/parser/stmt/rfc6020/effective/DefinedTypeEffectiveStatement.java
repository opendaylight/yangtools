package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

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
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.DefinitionAwareTypeEffectiveStatement;

final class DefinedTypeEffectiveStatement<T extends TypeDefinition<T>> implements DefinitionAwareTypeEffectiveStatement<TypeStatement, T> {
    private final DefinitionAwareTypeEffectiveStatement<TypeStatement, T> base;
    private final SchemaPath path;
    private final String defaultValue;
    private final String description;
    private final String reference;
    private final Status status;
    private final String units;
    private final String argument;

    DefinedTypeEffectiveStatement(final TypeDefEffectiveStatementImpl stmt, final SchemaPath path, final String argument,
            final DefinitionAwareTypeEffectiveStatement<TypeStatement, T> base) {
        this.path = Preconditions.checkNotNull(path);
        this.argument = Preconditions.checkNotNull(argument);
        this.base = Preconditions.checkNotNull(base);

        defaultValue = findOrDefault(stmt, DefaultStatement.class, String.class, base.getDefaultValue());
        description = findOrDefault(stmt, DescriptionStatement.class, String.class, base.getDescription());
        reference = findOrDefault(stmt, ReferenceStatement.class, String.class, base.getReference());
        status = findOrDefault(stmt, StatusStatement.class, Status.class, base.getStatus());
        units = findOrDefault(stmt, UnitsStatement.class, String.class, base.getUnits());
    }

    private static final <T> T findOrDefault(final EffectiveStatement<?, ?> stmt, final Class<?> statement, final Class<T> valueClass, final Object value) {
        for (EffectiveStatement<?, ?> s : stmt.effectiveSubstatements()) {
            if (statement.isInstance(s.getDeclared())) {
                return valueClass.cast(s.argument());
            }
        }
        return valueClass.cast(value);
    }

    @Override
    public TypeStatement getDeclared() {
        return base.getDeclared();
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V get(final Class<N> namespace, final K identifier) {
        return base.get(namespace, identifier);
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        return base.getAll(namespace);
    }

    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return base.effectiveSubstatements();
    }

    @Override
    public StatementDefinition statementDefinition() {
        return base.statementDefinition();
    }

    @Override
    public String argument() {
        return argument;
    }

    @Override
    public StatementSource getStatementSource() {
        return base.getStatementSource();
    }

    @Override
    public TypeEffectiveStatement<TypeStatement> derive(final EffectiveStatement<?, TypeStatement> stmt, final SchemaPath path) {
        throw new UnsupportedOperationException("Should never be derived?");
    }

    @Override
    public T getBaseType() {
        return base.getBaseType();
    }

    @Override
    public String getUnits() {
        return units;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public QName getQName() {
        return getPath().getLastComponent();
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return base.getUnknownSchemaNodes();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public T getTypeSpecificDefinition() {
        return base.getTypeSpecificDefinition();
    }
}
