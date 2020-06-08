package io.gitlab.arturbosch.detekt.rules.bugs

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.bugs.ExpressionFunctionReturnsUnit.Companion.ALLOW_EXPLICIT_RETURN_TYPE
import io.gitlab.arturbosch.detekt.rules.setupKotlinEnvironment
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class ExpressionFunctionReturnsUnitSpec : Spek({
    setupKotlinEnvironment()

    val env: KotlinCoreEnvironment by memoized()
    val rule by memoized { ExpressionFunctionReturnsUnit(Config.empty) }

    describe("Functions returning Unit via expression statements") {

        it("reports implicit Unit return types") {
            val code = """
                fun errorProneUnit() = println("Hello Unit")
                fun errorProneUnitWithParam(param: String) = param.run { println(this) }
                fun String.errorProneUnitWithReceiver() = run { println(this) }
            """.trimIndent()

            val findings = rule.compileAndLintWithContext(env, code)

            assertThat(findings).hasSize(1)
        }

        it("reports explicit Unit return type if configured by default") {
            val code = """fun safeUnitReturn(): Unit = println("Hello Unit")"""

            val findings = rule.compileAndLintWithContext(env, code)

            assertThat(findings).hasSize(1)
        }

        it("does not report explicit Unit return type if configured") {
            val code = """fun safeButStillReported(): Unit = println("Hello Unit")"""

            val findings = ExpressionFunctionReturnsUnit(TestConfig(ALLOW_EXPLICIT_RETURN_TYPE to "true"))
                .compileAndLintWithContext(env, code)

            assertThat(findings).isEmpty()
        }

        it("does not report for block statements") {
            val code = """
                fun blockUnitReturn() { 
                    println("Hello Unit")
                }
            """.trimIndent()

            val findings = rule.compileAndLintWithContext(env, code)

            assertThat(findings).isEmpty()
        }
    }
})
