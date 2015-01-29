package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

public interface ExtensionStatement extends DeclaredStatement<QName>, DocumentationGroup.WithStatus {

    public @Nullable ArgumentStatement getArgument();

    @Override
    public @Nullable StatusStatement getStatus();

    @Override
    public @Nullable DescriptionStatement getDescription();

    @Override
    public @Nullable ReferenceStatement getReference();

}
