package org.opendaylight.yangtools.yang.model.api.stmt;


@Rfc6020AbnfRule("linkage-stms")
public interface LinkageGroup {

    Iterable<? extends ImportStatement> getImports();

    Iterable<? extends IncludeStatement> getIncludes();
}
