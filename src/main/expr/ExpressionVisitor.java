package main.expr;

public interface ExpressionVisitor<R> {
    R visitAssignmentExpression(AssignmentExpression expr);
    R visitVariableLookupExpression(VariableLookupExpression expr);
    R visitBinaryExpression(BinaryExpression expr);
    R visitUnaryExpression(UnaryExpression expr);
    R visitFunctionCallExpression(FunctionCallExpression expr);
    R visitMemberExpression(MemberExpression expr);
    R visitThisExpression(ThisExpression expr);
    R visitLiteralExpression(LiteralExpression expr);
    R visitVectorExpression(VectorExpression expr);
    R visitLoopExpression(LoopExpression expr);
    R visitFunctionDeclarationExpression(FunctionDeclarationExpression expr);
    R visitClassDeclarationExpression(ClassDeclarationExpression expr);
    R visitBlockExpression(BlockExpression expr);
    R visitReturnExpression(ReturnExpression expr);
    R visitNativeFunctionCallExpression(NativeFunctionCallExpression expr);
    R visitListExpression(ListExpression expr);
    R visitIndexExpression(IndexExpression expr);
    R visitTernaryExpression(TernaryExpression expr);
    R visitCatchExpression(CatchExpression expr);
    R visitEnumDeclarationExpression(EnumDeclarationExpression expr);
    R visitMatchExpression(MatchExpression expr);
    R visitRangeExpression(RangeExpression expr);
    R visitDictionaryExpression(DictionaryExpression expr);
    R visitSetExpression(SetExpression expr);
    R visitForExpression(ForExpression expr);
}
