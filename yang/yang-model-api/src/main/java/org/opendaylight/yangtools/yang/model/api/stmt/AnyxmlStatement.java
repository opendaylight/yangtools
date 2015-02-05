package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface AnyxmlStatement extends DataDefinitionStatement<AnyxmlStatement> {

    @Override
    public @Nonnull String getName();

    @Override
    public @Nullable WhenStatement getWhenStatement();

    @Override
    public Iterable<? extends IfFeatureStatement> getIfFeatures();

    public @Nullable Iterable<? extends MustStatement> getMusts();

    public @Nullable ConfigStatement getConfig();

    public @Nullable MandatoryStatement getMandatory();

    @Override
    public @Nullable StatusStatement getStatus();

    @Override
    public @Nullable DescriptionStatement getDescription();

    @Override
    public @Nullable ReferenceStatement getReference();

}
