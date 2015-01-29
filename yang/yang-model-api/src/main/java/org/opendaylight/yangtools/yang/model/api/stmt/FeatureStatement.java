package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

public interface FeatureStatement extends DeclaredStatement<QName>, DocumentationGroup.WithStatus, ConditionalFeature {

    String getName();


    @Override
    public Collection<? extends IfFeatureStatement> getIfFeatures();

    @Override
    public @Nullable StatusStatement getStatus();

    @Override
    public @Nullable DescriptionStatement getDescription();

    @Override
    public @Nullable ReferenceStatement getReference();
}
