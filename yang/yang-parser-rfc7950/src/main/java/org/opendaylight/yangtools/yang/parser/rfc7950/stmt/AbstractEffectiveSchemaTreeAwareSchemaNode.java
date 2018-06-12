package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class AbstractEffectiveSchemaTreeAwareSchemaNode<D extends DeclaredStatement<QName>>
    extends AbstractEffectiveSchemaNode<D> implements SchemaTreeAwareEffectiveStatement<QName, D> {

    private final Map<QName, SchemaTreeEffectiveStatement<?>> schemaTreeChildren;

    protected AbstractEffectiveSchemaTreeAwareSchemaNode(final StmtContext<QName, D, ?> ctx) {
        super(ctx);
        schemaTreeChildren = streamEffectiveSubstatements(SchemaTreeEffectiveStatement.class)
                .collect(ImmutableMap.toImmutableMap(stmt -> (QName)stmt.argument(), Functions.identity()));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends Map<K, V>> getNamespaceContents(
            final Class<N> namespace) {
        if (SchemaTreeAwareEffectiveStatement.Namespace.class.equals(namespace)) {
            return Optional.of((Map<K, V>) schemaTreeChildren);
        }
        return super.getNamespaceContents(namespace);
    }
}
