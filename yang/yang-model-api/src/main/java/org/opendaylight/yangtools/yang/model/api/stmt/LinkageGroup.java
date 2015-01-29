package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;

@Rfc6020AbnfRule("linkage-stms")
public interface LinkageGroup {

    Collection<? extends ImportStatement> getImports();

    Collection<? extends IncludeStatement> getIncludes();
}
