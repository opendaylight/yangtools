package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nullable;


@Rfc6020AbnfRule("meta-stmts")
public interface MetaGroup extends DocumentationGroup {


    @Nullable OrganizationStatement getOrganization();

    @Nullable ContactStatement getContact();

}
