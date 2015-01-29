package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nullable;


public interface ContainerStatement extends DataDefinitionStatement<ContainerStatement>, DataDefinitionContainer.WithReusableDefinitions {

    @Override
    public String getName();

    @Override
    public @Nullable WhenStatement getWhenStatement();

    @Override
    public Iterable<? extends IfFeatureStatement> getIfFeatures();

    public @Nullable Iterable<? extends MustStatement> getMusts();

    public @Nullable PresenceStatement getPresence();

    public @Nullable ConfigStatement getConfig();

    @Override
    public @Nullable StatusStatement getStatus();

    @Override
    public @Nullable DescriptionStatement getDescription();

    @Override
    public @Nullable ReferenceStatement getReference();

    @Override
    public Iterable<? extends TypedefStatement> getTypedefs();

    @Override
    public Iterable<? extends GroupingStatement> getGroupings();

    @Override
    public Iterable<? extends DataDefinitionStatement<?>> getDataDefinitions();

}
