package ro.redeul.google.go.findUsages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import ro.redeul.google.go.inspection.InspectionResult;
import ro.redeul.google.go.inspection.fix.ConvertToAssignmentFix;
import ro.redeul.google.go.inspection.fix.DeleteStmtFix;
import ro.redeul.google.go.inspection.fix.RemoveVariableFix;
import ro.redeul.google.go.lang.parser.GoElementTypes;
import ro.redeul.google.go.lang.psi.GoFile;
import ro.redeul.google.go.lang.psi.GoPsiElement;
import ro.redeul.google.go.lang.psi.declarations.GoConstDeclaration;
import ro.redeul.google.go.lang.psi.declarations.GoVarDeclaration;
import ro.redeul.google.go.lang.psi.expressions.GoExpr;
import ro.redeul.google.go.lang.psi.expressions.literals.GoLiteral;
import ro.redeul.google.go.lang.psi.expressions.literals.GoLiteralIdentifier;
import ro.redeul.google.go.lang.psi.expressions.primary.GoLiteralExpression;
import ro.redeul.google.go.lang.psi.impl.GoPsiElementBase;
import ro.redeul.google.go.lang.psi.statements.GoShortVarDeclaration;
import ro.redeul.google.go.lang.psi.toplevel.GoFunctionDeclaration;
import ro.redeul.google.go.lang.psi.toplevel.GoFunctionParameter;
import ro.redeul.google.go.lang.psi.toplevel.GoMethodDeclaration;
import ro.redeul.google.go.lang.psi.toplevel.GoMethodReceiver;
import ro.redeul.google.go.lang.psi.toplevel.GoTypeSpec;
import ro.redeul.google.go.lang.psi.types.GoPsiType;
import ro.redeul.google.go.lang.psi.types.GoPsiTypeInterface;
import ro.redeul.google.go.lang.psi.types.GoPsiTypeName;
import ro.redeul.google.go.lang.psi.types.struct.GoTypeStructField;
import ro.redeul.google.go.lang.psi.utils.GoFileUtils;
import ro.redeul.google.go.lang.psi.visitors.GoRecursiveElementVisitor;
import static com.intellij.patterns.PlatformPatterns.psiElement;
import static ro.redeul.google.go.lang.psi.utils.GoPsiUtils.isNodeOfType;
import static ro.redeul.google.go.lang.psi.utils.GoPsiUtils.resolveSafely;

public class GoVariableUsageStatVisitor2 extends GoRecursiveElementVisitor {

    private static final TokenSet NEW_SCOPE_STATEMENT = TokenSet.create(
        GoElementTypes.BLOCK_STATEMENT,
        GoElementTypes.IF_STATEMENT,
        GoElementTypes.FOR_WITH_CLAUSES_STATEMENT,
        GoElementTypes.FOR_WITH_CONDITION_STATEMENT,
        GoElementTypes.FOR_WITH_RANGE_STATEMENT,
        GoElementTypes.SWITCH_EXPR_STATEMENT,
        GoElementTypes.SWITCH_TYPE_STATEMENT,
        GoElementTypes.SWITCH_EXPR_CASE,
        GoElementTypes.SELECT_STATEMENT,
        GoElementTypes.SELECT_CASE
    );

    private InspectionResult result;
    private Context ctx;

    public GoVariableUsageStatVisitor2(InspectionResult result) {
        this.result = result;
    }

    @Override
    public void visitFile(GoFile file) {
        visitElement(file);

        for (GoPsiElement usage : usages) {
            declarations.remove(usage);
        }

        for (GoPsiElement declaration : declarations) {
            if (psiElement(GoLiteralIdentifier.class)
                    .withText("_").accepts(declaration))
                continue;

            if ( psiElement(GoLiteralIdentifier.class)
                .withParent(GoFunctionParameter.class).accepts(declaration)) {
                result.addProblem(declaration, "Unused parameter",
                                  ProblemHighlightType.LIKE_UNUSED_SYMBOL);
            } else {
                result.addProblem(declaration, "Unused variable",
                                  ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                  new RemoveVariableFix());
            }
        }
        int a = 10;
//        HashMap<String, VariableUsage> global = new HashMap<String, VariableUsage>();
//        ctx = new Context(result, global);
//        getGlobalVariables(file, global);
//
//        for (GoFunctionDeclaration fd : file.getFunctions()) {
//            visitFunctionDeclaration(fd);
//        }
//
//        // A global variable could be used in different files even if it's not exported.
//        // We cannot reliably check problems on global variables, so we don't check anymore.
//        ctx.popLastScopeLevel();
    }

//    @Override
//    public void visitElement(GoPsiElement element) {
//        if (!couldOpenNewScope(element)) {
//            super.visitElement(element);
//            return;
//        }
//
//        ctx.addNewScopeLevel();
//
//        super.visitElement(element);
//
//        for (VariableUsage v : ctx.popLastScopeLevel().values()) {
//            if (!v.isUsed()) {
//                ctx.unusedVariable(v);
//            }
//        }
//    }

    Set<GoPsiElement> declarations = new HashSet<GoPsiElement>();
    Set<GoPsiElement> usages = new HashSet<GoPsiElement>();

    @Override
    public void visitConstDeclaration(GoConstDeclaration declaration) {
        Collections.addAll(declarations, declaration.getIdentifiers());
        for (GoExpr goExpr : declaration.getExpressions()) {
            goExpr.accept(this);
        }
    }

    @Override
    public void visitVarDeclaration(GoVarDeclaration declaration) {
        Collections.addAll(declarations, declaration.getIdentifiers());
        for (GoExpr goExpr : declaration.getExpressions()) {
            goExpr.accept(this);
        }
    }

    @Override
    public void visitShortVarDeclaration(GoShortVarDeclaration declaration) {
        Collections.addAll(declarations, declaration.getIdentifiers());
        for (GoExpr goExpr : declaration.getExpressions()) {
            goExpr.accept(this);
        }
    }

    @Override
    public void visitFunctionParameter(GoFunctionParameter parameter) {
        Collections.addAll(declarations, parameter.getIdentifiers());
    }

    @Override
    public void visitLiteralExpression(GoLiteralExpression expression) {
        GoPsiElement psiElement = resolveSafely(expression, GoPsiElement.class);
        if ( psiElement != null ) {
            usages.add(psiElement);
        } else {
            visitElement(expression);
        }
    }

    @Override
    public void visitLiteralIdentifier(GoLiteralIdentifier identifier) {
        GoPsiElement psiElement = resolveSafely(identifier, GoPsiElement.class);
        if ( psiElement != null ) {
            usages.add(psiElement);
        }
    }

    @Override
    public void visitFunctionDeclaration(GoFunctionDeclaration declaration) {
        for (GoFunctionParameter parameter : declaration.getParameters()) {
            parameter.accept(this);
        }

        if ( declaration.getBlock() != null)
           declaration.getBlock().accept(this);
    }

    @Override
    public void visitMethodDeclaration(GoMethodDeclaration declaration) {
        declaration.getMethodReceiver().accept(this);

        visitFunctionDeclaration(declaration);
    }

    @Override
    public void visitInterfaceType(GoPsiTypeInterface type) {
        // dont :)
    }

    private void visitIdentifiersAndExpressions(GoLiteralIdentifier[] identifiers, GoExpr[] exprs,
                                                boolean mayRedeclareVariable) {
        if (identifiers.length == 0) {
            return;
        }

        int nonBlankIdCount = 0;
        List<GoLiteralIdentifier> redeclaredIds = new ArrayList<GoLiteralIdentifier>();
        for (GoLiteralIdentifier id : identifiers) {
            if (!id.isBlank()) {
                nonBlankIdCount++;
                if (ctx.isDefinedInCurrentScope(id)) {
                    redeclaredIds.add(id);
                }
            }
        }

        if (mayRedeclareVariable) {
            String msg = "No new variables declared";
            if (nonBlankIdCount == 0) {
                PsiElement start = identifiers[0].getParent();
                ctx.addProblem(start, start, msg,
                               ProblemHighlightType.GENERIC_ERROR,
                               new DeleteStmtFix());
            } else if (redeclaredIds.size() == nonBlankIdCount) {
                PsiElement start = identifiers[0];
                PsiElement end = identifiers[identifiers.length - 1];
                ctx.addProblem(start, end, msg,
                               ProblemHighlightType.GENERIC_ERROR,
                               new ConvertToAssignmentFix());
            }
        } else {
            for (GoLiteralIdentifier redeclaredId : redeclaredIds) {
                String msg = redeclaredId.getText() + " redeclared in this block";
                ctx.addProblem(redeclaredId, redeclaredId, msg,
                               ProblemHighlightType.GENERIC_ERROR);
            }
        }

        for (GoLiteralIdentifier id : identifiers) {
            ctx.addDefinition(id);
        }

        for (GoExpr expr : exprs) {
            visitElement(expr);
        }
    }

    private static boolean couldOpenNewScope(PsiElement element) {
        if (!(element instanceof GoPsiElementBase)) {
            return false;
        }

        return isNodeOfType(element, NEW_SCOPE_STATEMENT);
    }

    private void visitExpressionAsIdentifier(GoExpr expr, boolean declaration) {
        if (!(expr instanceof GoLiteralExpression)) {
            return;
        }

        GoLiteral literal = ((GoLiteralExpression) expr).getLiteral();
        if (literal.getType() == GoLiteral.Type.Identifier)
            if (needToCollectUsage((GoLiteralIdentifier) literal)) {
                if (declaration) {
                    ctx.addDefinition(literal);
                } else {
                    ctx.addUsage(literal);
                }
            }
    }

    private boolean needToCollectUsage(GoLiteralIdentifier id) {
        return id != null && !isFunctionOrMethodCall(id) && !isTypeField(
            id) && !isType(id) &&
            // if there is any dots in the identifier, it could be from other packages.
            // usage collection of other package variables is not implemented yet.
            !id.getText().contains(".");
    }

    private boolean isType(GoLiteralIdentifier id) {
        PsiElement parent = id.getParent();
        return isNodeOfType(parent, GoElementTypes.BASE_TYPE_NAME) ||
            isNodeOfType(parent,
                         GoElementTypes.REFERENCE_BASE_TYPE_NAME) || parent instanceof GoPsiTypeName;
    }

    private boolean isTypeField(GoLiteralIdentifier id) {
        return id.getParent() instanceof GoTypeStructField || isTypeFieldInitializer(
            id);
    }

    /**
     * Check whether id is a field name in composite literals
     *
     * @param id
     * @return
     */
    private boolean isTypeFieldInitializer(GoLiteralIdentifier id) {
        if (!(id.getParent() instanceof GoLiteral)) {
            return false;
        }

        PsiElement parent = id.getParent().getParent();
        if (parent == null || parent.getNode() == null ||
            parent.getNode()
                  .getElementType() != GoElementTypes.COMPOSITE_LITERAL_ELEMENT_KEY) {
            return false;
        }

        PsiElement sibling = parent.getNextSibling();
        return sibling != null && ":".equals(sibling.getText());

    }

    private boolean isFunctionOrMethodCall(GoLiteralIdentifier id) {
        if (!(id.getParent() instanceof GoLiteralExpression)) {
            return false;
        }

        PsiElement grandpa = id.getParent().getParent();
        return grandpa.getNode()
                      .getElementType() == GoElementTypes.CALL_OR_CONVERSION_EXPRESSION &&
            id.getParent().isEquivalentTo(grandpa.getFirstChild());
    }

    private void addFunctionParametersToMap(GoFunctionParameter[] parameters,
                                            Map<String, VariableUsage> variables,
                                            boolean ignoreProblem) {
        for (GoFunctionParameter p : parameters) {
            for (GoLiteralIdentifier id : p.getIdentifiers()) {
                variables.put(id.getName(),
                              new VariableUsage(id, ignoreProblem));
            }
        }
    }

    private GoLiteralIdentifier getMethodReceiverIdentifier(
        GoMethodDeclaration md) {
        GoMethodReceiver methodReceiver = md.getMethodReceiver();
        if (methodReceiver == null) {
            return null;
        }

        return methodReceiver.getIdentifier();
    }

    private void getGlobalVariables(GoFile file, HashMap<String, VariableUsage> variables) {
        for (GoConstDeclaration cd : GoFileUtils.getConstDeclarations(file)) {
            visitConstDeclaration(cd);
        }

        for (GoVarDeclaration vd : GoFileUtils.getVarDeclarations(file)) {
            visitVarDeclaration(vd);
        }

        for (GoMethodDeclaration md : file.getMethods()) {
            variables.put(md.getFunctionName(), new VariableUsage(md));
        }

        for (GoFunctionDeclaration fd : file.getFunctions()) {
            variables.put(fd.getFunctionName(), new VariableUsage(fd));
        }

        for (GoTypeSpec spec : GoFileUtils.getTypeSpecs(file)) {
            GoPsiType type = spec.getType();
            if (type != null) {
                variables.put(type.getName(), new VariableUsage(type));
            }
        }
    }


    private Map<String, VariableUsage> getFunctionParameters(GoFunctionDeclaration fd) {
        Map<String, VariableUsage> variables = createFunctionParametersMap(
            fd.getParameters(), fd.getResults());

        if (fd instanceof GoMethodDeclaration) {
            // Add method receiver to parameter list
            GoLiteralIdentifier receiver = getMethodReceiverIdentifier(
                (GoMethodDeclaration) fd);
            if (receiver != null) {
                variables.put(receiver.getName(), new VariableUsage(receiver));
            }
        }
        return variables;
    }

    private Map<String, VariableUsage> createFunctionParametersMap(GoFunctionParameter[] parameters,
                                                                   GoFunctionParameter[] results) {
        Map<String, VariableUsage> variables = ctx.addNewScopeLevel();
        addFunctionParametersToMap(parameters, variables, false);

        // Don't detect usage problem on function result
        addFunctionParametersToMap(results, variables, true);
        return variables;
    }


    public void afterVisitGoFunctionDeclaration() {
        for (VariableUsage v : ctx.popLastScopeLevel().values()) {
            if (!v.isUsed()) {
                ctx.unusedParameter(v);
            }
        }
    }

    private static class Context {
        public final InspectionResult result;
        public final List<Map<String, VariableUsage>> variables = new ArrayList<Map<String, VariableUsage>>();

        Context(InspectionResult result, Map<String, VariableUsage> global) {
            this.result = result;
            this.variables.add(global);
        }

        public Map<String, VariableUsage> addNewScopeLevel() {
            Map<String, VariableUsage> variables = new HashMap<String, VariableUsage>();
            this.variables.add(variables);
            return variables;
        }

        public Map<String, VariableUsage> popLastScopeLevel() {
            return variables.remove(variables.size() - 1);
        }

        public void unusedVariable(VariableUsage variableUsage) {
            if (variableUsage.isBlank()) {
                return;
            }

            addProblem(variableUsage, "Unused variable",
                       ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                       new RemoveVariableFix());
        }

        public void unusedParameter(VariableUsage variableUsage) {
            if (!variableUsage.isBlank()) {
                addProblem(variableUsage, "Unused parameter",
                           ProblemHighlightType.LIKE_UNUSED_SYMBOL);
            }
        }

        public void unusedGlobalVariable(VariableUsage variableUsage) {
            if (variableUsage.element instanceof GoFunctionDeclaration ||
                variableUsage.element instanceof GoPsiType) {
                return;
            }

            addProblem(variableUsage, "Unused global",
                       ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                       new RemoveVariableFix());
        }

        public void addProblem(VariableUsage variableUsage, String desc,
                               ProblemHighlightType highlightType,
                               LocalQuickFix... fixes) {
            if (!variableUsage.ignoreAnyProblem) {
                result.addProblem(variableUsage.element, desc, highlightType,
                                  fixes);
            }
        }

        public void addProblem(PsiElement start, PsiElement end, String desc, ProblemHighlightType type, LocalQuickFix... fixes) {
            result.addProblem(start, end, desc, type, fixes);
        }

        public boolean isDefinedInCurrentScope(GoPsiElement element) {
            return variables.get(variables.size() - 1)
                            .containsKey(element.getText());
        }

        public void addDefinition(GoPsiElement element) {
            Map<String, VariableUsage> map = variables.get(
                variables.size() - 1);
            VariableUsage variableUsage = map.get(element.getText());
            if (variableUsage != null) {
                variableUsage.addUsage(element);
            } else {
                map.put(element.getText(), new VariableUsage(element));
            }
        }

        public void addUsage(GoPsiElement element) {
            for (int i = variables.size() - 1; i >= 0; i--) {
                VariableUsage variableUsage = variables.get(i)
                                                       .get(element.getText());
                if (variableUsage != null) {
                    variableUsage.addUsage(element);
                    return;
                }
            }
        }
    }
}