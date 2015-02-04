package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nullable;



public interface ChoiceStatement extends DataDefinitionStatement<ChoiceStatement> {


    @Override
    public String getName();

    @Override
    public @Nullable WhenStatement getWhenStatement();

    @Override
    public Iterable<? extends IfFeatureStatement> getIfFeatures();

    public @Nullable DefaultStatement getDefault();

    public @Nullable ConfigStatement getConfig();

    public @Nullable MandatoryStatement getMandatory();

    @Override
    public @Nullable StatusStatement getStatus();

    @Override
    public @Nullable DescriptionStatement getDescription();

    @Override
    public @Nullable ReferenceStatement getReference();

    public Iterable<? extends CaseStatement> getCases();
}
