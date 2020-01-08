package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;

final class EmptyLeafEffectiveStatement extends AbstractLeafEffectiveStatement {
    EmptyLeafEffectiveStatement(final LeafStatement declared, final EffectiveStatement<?, ?> substatement) {
        super(declared, substatement);
    }

    EmptyLeafEffectiveStatement(final LeafStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, substatements);
    }

    @Override
    public ImmutableList<MustDefinition> getMustConstraints() {
        return ImmutableList.of();
    }

    @Override
    public Optional<RevisionAwareXPath> getWhenCondition() {
        return Optional.empty();
    }

    @Override
    public Status getStatus() {
        return Status.CURRENT;
    }
}
