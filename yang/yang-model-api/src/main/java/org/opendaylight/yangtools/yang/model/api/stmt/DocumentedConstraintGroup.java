package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nullable;

public interface DocumentedConstraintGroup extends DocumentationGroup {

    @Nullable ErrorAppTagStatement getErrorAppTagStatement();

    @Nullable ErrorMessageStatement getErrorMessageStatement();

}
