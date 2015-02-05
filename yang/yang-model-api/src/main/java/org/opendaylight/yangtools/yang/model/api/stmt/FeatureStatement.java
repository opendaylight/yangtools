package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.Statement;

public interface FeatureStatement extends
    Statement<FeatureStatement>,
    DocumentationGroup.WithStatus,
    ConditionalFeature {

    String getName();


    @Override
    public Iterable<? extends IfFeatureStatement> getIfFeatures();

    @Override
    public @Nullable StatusStatement getStatus();

    @Override
    public @Nullable DescriptionStatement getDescription();

    @Override
    public @Nullable ReferenceStatement getReference();
}
