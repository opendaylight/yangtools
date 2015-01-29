package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nullable;


public interface UsesStatement extends DataDefinitionStatement {

    @Override
    String getName();

    @Override
    public @Nullable WhenStatement getWhenStatement();

    @Override
    public Iterable<? extends IfFeatureStatement> getIfFeatures();

    @Override
    public @Nullable StatusStatement getStatus();

    @Override
    public @Nullable DescriptionStatement getDescription();

    @Override
    public @Nullable ReferenceStatement getReference();

    public Iterable<? extends RefineStatement> getRefines();

    public Iterable<? extends AugmentStatement> getAugments();

}
