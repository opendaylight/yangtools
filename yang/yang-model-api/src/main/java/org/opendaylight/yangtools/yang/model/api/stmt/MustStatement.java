package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

public interface MustStatement extends DeclaredStatement<RevisionAwareXPath>, DocumentedConstraintGroup {

    public @Nonnull String getCondition();

    @Override
    public @Nullable ErrorMessageStatement getErrorMessageStatement();

    @Override
    public @Nullable ErrorAppTagStatement getErrorAppTagStatement();

    @Override
    public @Nullable DescriptionStatement getDescription();

    @Override
    public @Nullable ReferenceStatement getReference();

}
