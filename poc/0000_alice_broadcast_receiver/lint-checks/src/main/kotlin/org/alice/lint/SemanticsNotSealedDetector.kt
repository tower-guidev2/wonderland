package org.alice.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UParenthesizedExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.USimpleNameReferenceExpression

class SemanticsNotSealedDetector : Detector(), SourceCodeScanner {

    // Lint API requires companion object for Issue registration — framework constraint.
    companion object {
        private val IMPLEMENTATION = Implementation(
            SemanticsNotSealedDetector::class.java,
            Scope.JAVA_FILE_SCOPE,
        )

        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "SemanticsNotSealed",
            briefDescription = "Modifier chain must begin with semanticsSealed()",
            explanation = """
                Every Modifier chain in the Alice application must start with \
                `.semanticsSealed()` (or `.clearAndSetSemantics { }`) as its \
                first modifier to prevent accessibility-service data exfiltration \
                on the air-gapped device.

                Move `semanticsSealed()` to the front of the chain:
                ```
                Modifier.semanticsSealed().fillMaxSize()
                ```
            """.trimIndent(),
            category = Category.SECURITY,
            priority = 10,
            severity = Severity.ERROR,
            implementation = IMPLEMENTATION,
        )

        private val SEALED_NAMES = setOf(
            "semanticsSealed",
            "clearAndSetSemantics",
        )
    }

    override fun getApplicableUastTypes() =
        listOf(UQualifiedReferenceExpression::class.java)

    override fun createUastHandler(context: JavaContext) =
        object : com.android.tools.lint.client.api.UElementHandler() {

            override fun visitQualifiedReferenceExpression(
                node: UQualifiedReferenceExpression,
            ) {
                val parent = node.uastParent
                if (parent is UQualifiedReferenceExpression && parent.receiver === node) return
                if (parent is UParenthesizedExpression) return

                val root = findChainRoot(node) ?: return
                if (isModifierExpression(unwrapParentheses(root)).not()) return

                val firstCall = findFirstMethodCall(node, root) ?: return
                val methodName = firstCall.methodName ?: return

                if (methodName !in SEALED_NAMES) {
                    context.report(
                        ISSUE,
                        firstCall,
                        context.getLocation(firstCall),
                        "Modifier chain must begin with `semanticsSealed()`. " +
                            "Found `$methodName()` as the first modifier instead.",
                    )
                }
            }
        }

    private fun unwrapParentheses(expr: UExpression): UExpression {
        var current = expr
        while (current is UParenthesizedExpression) {
            current = current.expression
        }
        return current
    }

    private fun findChainRoot(expr: UQualifiedReferenceExpression): UExpression? {
        var current: UExpression = expr
        while (true) {
            current = when (current) {
                is UQualifiedReferenceExpression -> current.receiver
                is UParenthesizedExpression -> current.expression
                else -> return current
            }
        }
    }

    private fun isModifierExpression(expr: UExpression): Boolean {
        if (expr is USimpleNameReferenceExpression && expr.identifier == "Modifier") return true
        val canonical = expr.getExpressionType()?.canonicalText ?: return false
        return canonical == "androidx.compose.ui.Modifier"
            || canonical == "androidx.compose.ui.Modifier.Companion"
    }

    private fun findFirstMethodCall(
        topLevel: UQualifiedReferenceExpression,
        root: UExpression,
    ): UCallExpression? =
        findQualifiedWithReceiver(topLevel, root)?.let { innermost ->
            innermost.selector as? UCallExpression
        }

    private fun findQualifiedWithReceiver(
        node: UQualifiedReferenceExpression,
        target: UExpression,
    ): UQualifiedReferenceExpression? {
        if (node.receiver === target) return node
        val inner = when (val receiver = node.receiver) {
            is UQualifiedReferenceExpression -> receiver
            is UParenthesizedExpression -> {
                val unwrapped = unwrapParentheses(receiver)
                if (unwrapped === target) return node
                unwrapped as? UQualifiedReferenceExpression ?: return null
            }
            else -> return null
        }
        return findQualifiedWithReceiver(inner, target)
    }
}
