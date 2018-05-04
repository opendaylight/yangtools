package org.opendaylight.yangtools.yang.model.api.xpath;

public interface YangBinaryExpr extends YangExpr {

    YangExpr getLeftExpr();

    YangExpr getRightExpr();

    YangBinaryOperator getBinaryOperator();
}
