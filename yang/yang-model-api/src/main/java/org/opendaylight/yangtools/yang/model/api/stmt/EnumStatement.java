package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

public interface EnumStatement extends DeclaredStatement<String>, DocumentationGroup.WithStatus {

    @Nonnull String getName();

    @Nullable ValueStatement getValue();

    @Override
    public @Nullable StatusStatement getStatus();

    @Override
    public @Nullable DescriptionStatement getDescription();

    @Override
    public @Nullable ReferenceStatement getReference();
}

