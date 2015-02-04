package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.Statement;

public interface IdentityStatement extends Statement<IdentityStatement>, DocumentationGroup.WithStatus {

    @Nonnull String getName();

    @Nullable BaseStatement getBase();

    @Override
    public @Nullable StatusStatement getStatus();

    @Override
    public @Nullable DescriptionStatement getDescription();

    @Override
    public @Nullable ReferenceStatement getReference();

}
