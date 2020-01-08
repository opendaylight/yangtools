package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement;

abstract class AbstractLeafEffectiveStatement extends AbstractDeclaredEffectiveStatement.Default<QName, LeafStatement>
        implements LeafEffectiveStatement, LeafSchemaNode, DerivableSchemaNode {
    // Variable: either a single substatement or an ImmutableList
    private final Object substatements;

    AbstractLeafEffectiveStatement(final LeafStatement declared, final EffectiveStatement<?, ?> substatement) {
        super(declared);
        this.substatements = requireNonNull(substatement);
    }

    AbstractLeafEffectiveStatement(final LeafStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared);
        this.substatements = requireNonNull(substatements);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        if (substatements instanceof ImmutableList) {
            return (ImmutableList<? extends EffectiveStatement<?, ?>>) substatements;
        }
        verify(substatements instanceof EffectiveStatement, "Unexpected substatement %s", substatements);
        return ImmutableList.of((EffectiveStatement<?, ?>) substatements);
    }

    @Override
    public final Optional<String> getDescription() {
        return findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class);
    }

    @Override
    public final Optional<String> getReference() {
        return findFirstEffectiveSubstatementArgument(ReferenceEffectiveStatement.class);
    }

    @Override
    public TypeDefinition<? extends TypeDefinition<?>> getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isConfiguration() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAugmenting() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAddedByUses() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isMandatory() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public @NonNull QName getQName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NonNull SchemaPath getPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<? extends SchemaNode> getOriginal() {
        // TODO Auto-generated method stub
        return null;
    }
}
