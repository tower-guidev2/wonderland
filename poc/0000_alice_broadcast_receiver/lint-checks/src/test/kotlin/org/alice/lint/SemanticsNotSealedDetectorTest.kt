package org.alice.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

class SemanticsNotSealedDetectorTest : LintDetectorTest() {

    override fun getDetector(): Detector = SemanticsNotSealedDetector()

    override fun getIssues(): List<Issue> = listOf(SemanticsNotSealedDetector.ISSUE)

    private val modifierStub = kotlin(
        """
        package androidx.compose.ui

        class Modifier {
            companion object : Modifier()

            fun clearAndSetSemantics(
                properties: (Any.() -> Unit) = {}
            ): Modifier = this

            fun fillMaxSize(): Modifier = this
            fun padding(): Modifier = this
            fun background(): Modifier = this
        }
        """,
    ).indented()

    private val extensionStub = kotlin(
        """
        package org.alice.poc.airgap.composables.modifier

        import androidx.compose.ui.Modifier

        fun Modifier.semanticsSealed(): Modifier =
            this.clearAndSetSemantics { }
        """,
    ).indented()

    fun testSemanticsSealedAsFirstModifierIsClean() {
        lint().files(
            modifierStub,
            extensionStub,
            kotlin(
                """
                package org.alice.poc.airgap.composables

                import androidx.compose.ui.Modifier
                import org.alice.poc.airgap.composables.modifier.semanticsSealed

                fun example() {
                    val m = Modifier.semanticsSealed().fillMaxSize()
                }
                """,
            ).indented(),
        )
            .run()
            .expectClean()
    }

    fun testClearAndSetSemanticsAsFirstModifierIsClean() {
        lint().files(
            modifierStub,
            kotlin(
                """
                package org.alice.poc.airgap.composables

                import androidx.compose.ui.Modifier

                fun example() {
                    val m = Modifier.clearAndSetSemantics { }.fillMaxSize()
                }
                """,
            ).indented(),
        )
            .run()
            .expectClean()
    }

    fun testBareModifierWithNoChainIsClean() {
        lint().files(
            modifierStub,
            kotlin(
                """
                package org.alice.poc.airgap.composables

                import androidx.compose.ui.Modifier

                fun example() {
                    val m = Modifier
                }
                """,
            ).indented(),
        )
            .run()
            .expectClean()
    }

    fun testFillMaxSizeAsFirstModifierIsError() {
        lint().files(
            modifierStub,
            kotlin(
                """
                package org.alice.poc.airgap.composables

                import androidx.compose.ui.Modifier

                fun example() {
                    val m = Modifier.fillMaxSize().padding()
                }
                """,
            ).indented(),
        )
            .run()
            .expect(
                """
                src/org/alice/poc/airgap/composables/test.kt:6: Error: Modifier chain must begin with semanticsSealed(). Found fillMaxSize() as the first modifier instead. [SemanticsNotSealed]
                    val m = Modifier.fillMaxSize().padding()
                            ~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent(),
            )
    }

    fun testModifierParameterWithoutSealedIsError() {
        lint().files(
            modifierStub,
            kotlin(
                """
                package org.alice.poc.airgap.composables

                import androidx.compose.ui.Modifier

                fun example(modifier: Modifier) {
                    val m = modifier.fillMaxSize().padding()
                }
                """,
            ).indented(),
        )
            .run()
            .expect(
                """
                src/org/alice/poc/airgap/composables/test.kt:6: Error: Modifier chain must begin with semanticsSealed(). Found fillMaxSize() as the first modifier instead. [SemanticsNotSealed]
                    val m = modifier.fillMaxSize().padding()
                            ~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent(),
            )
    }

    fun testModifierParameterWithSealedIsClean() {
        lint().files(
            modifierStub,
            extensionStub,
            kotlin(
                """
                package org.alice.poc.airgap.composables

                import androidx.compose.ui.Modifier
                import org.alice.poc.airgap.composables.modifier.semanticsSealed

                fun example(modifier: Modifier) {
                    val m = modifier.semanticsSealed().fillMaxSize()
                }
                """,
            ).indented(),
        )
            .run()
            .expectClean()
    }

    fun testSemanticsSealedNotFirstIsError() {
        lint().files(
            modifierStub,
            extensionStub,
            kotlin(
                """
                package org.alice.poc.airgap.composables

                import androidx.compose.ui.Modifier
                import org.alice.poc.airgap.composables.modifier.semanticsSealed

                fun example() {
                    val m = Modifier.fillMaxSize().semanticsSealed()
                }
                """,
            ).indented(),
        )
            .run()
            .expect(
                """
                src/org/alice/poc/airgap/composables/test.kt:7: Error: Modifier chain must begin with semanticsSealed(). Found fillMaxSize() as the first modifier instead. [SemanticsNotSealed]
                    val m = Modifier.fillMaxSize().semanticsSealed()
                            ~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent(),
            )
    }
}
