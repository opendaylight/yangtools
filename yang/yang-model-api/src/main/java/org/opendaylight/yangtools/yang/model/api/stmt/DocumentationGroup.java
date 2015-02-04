package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nullable;

public interface DocumentationGroup {

    @Nullable DescriptionStatement getDescription();

    @Nullable ReferenceStatement getReference();



    public interface WithStatus extends DocumentationGroup {

        @Nullable StatusStatement getStatus();

    }

}
