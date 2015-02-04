package org.opendaylight.yangtools.yang.model.api.stmt;


public interface DataDefinitionContainer {

    Iterable<? extends DataDefinitionStatement<?>> getDataDefinitions();


    public interface WithReusableDefinitions extends DataDefinitionContainer {

        Iterable<? extends TypedefStatement> getTypedefs();

        Iterable<? extends GroupingStatement> getGroupings();

    }

}
