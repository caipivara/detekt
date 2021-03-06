package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

/**
 * @author Artur Bosch
 */
class ExpressionBodySyntaxSpec : Spek({
    val subject by memoized { ExpressionBodySyntax(Config.empty) }

    describe("ExpressionBodySyntax rule") {

        context("several return statements") {

            it("reports constant return") {
                assertThat(subject.lint("""
				fun stuff(): Int {
					return 5
				}
			"""
                )).hasSize(1)
            }

            it("reports return statement with method chain") {
                assertThat(subject.lint("""
				fun stuff(): Int {
					return moreStuff().getStuff().stuffStuff()
				}
			"""
                )).hasSize(1)
            }

            it("reports return statements with conditionals") {
                assertThat(subject.lint("""
				fun stuff(): Int {
					return if (true) return 5 else return 3
				}
				fun stuff(): Int {
					return try { return 5 } catch (e: Exception) { return 3 }
				}
			""")).hasSize(2)
            }

            it("does not report multiple if statements") {
                assertThat(subject.lint("""
				fun stuff(): Int {
					if (true) return true
					return false
				}
			""")).isEmpty()
            }
        }

        context("several return statements with multiline method chain") {

            val code = """
			fun stuff(): Int {
				return moreStuff()
					.getStuff()
					.stuffStuff()
			}"""

            it("does not report with the default configuration") {
                assertThat(subject.lint(code)).isEmpty()
            }

            it("reports with includeLineWrapping = true configuration") {
                val config = TestConfig(mapOf(ExpressionBodySyntax.INCLUDE_LINE_WRAPPING to "true"))
                assertThat(ExpressionBodySyntax(config).lint(code)).hasSize(1)
            }
        }

        context("several return statements with multiline when expression") {

            val code = """
			fun stuff(val arg: Int): Int {
				return when(arg) {
					0 -> 0
					else -> 1
				}
			}"""

            it("does not report with the default configuration") {
                assertThat(subject.lint(code)).isEmpty()
            }

            it("reports with includeLineWrapping = true configuration") {
                val config = TestConfig(mapOf(ExpressionBodySyntax.INCLUDE_LINE_WRAPPING to "true"))
                assertThat(ExpressionBodySyntax(config).lint(code)).hasSize(1)
            }
        }

        context("several return statements with multiline if expression") {

            val code = """
			fun stuff(val arg: Int): Int {
				return if (arg == 0) 0
				else 1
			}"""

            it("does not report with the default configuration") {
                assertThat(subject.lint(code)).isEmpty()
            }

            it("reports with includeLineWrapping = true configuration") {
                val config = TestConfig(mapOf(ExpressionBodySyntax.INCLUDE_LINE_WRAPPING to "true"))
                assertThat(ExpressionBodySyntax(config).lint(code)).hasSize(1)
            }
        }
    }
})
