package com.example.calculatorapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception
import java.util.Stack

class MainActivity : AppCompatActivity() {

    private lateinit var expressionTextView: TextView
    private lateinit var resultTextView: TextView

    private val buttons by lazy {
        listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply, R.id.btnDivide,
            R.id.btnDecimal
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expressionTextView = findViewById(R.id.expressionTextView)
        resultTextView = findViewById(R.id.resultTextView)

        buttons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                val value = (it as Button).text.toString()
                appendToExpression(value)
            }
        }

        findViewById<Button>(R.id.btnClear).setOnClickListener {
            clearExpression()
        }

        findViewById<Button>(R.id.btnBackspace).setOnClickListener {
            backspace()
        }

        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            evaluateExpression()
        }
    }

    private fun appendToExpression(value: String) {
        val current = expressionTextView.text.toString()
        if (current.isNotEmpty() && isOperator(value) && isOperator(current.last().toString())) {
            // Prevent consecutive operators
            return
        }
        expressionTextView.text = current + value
        resultTextView.text = ""
    }

    private fun clearExpression() {
        expressionTextView.text = ""
        resultTextView.text = ""
    }

    private fun backspace() {
        val current = expressionTextView.text.toString()
        if (current.isNotEmpty()) {
            expressionTextView.text = current.substring(0, current.length - 1)
            resultTextView.text = ""
        }
    }

    private fun evaluateExpression() {
        val expression = expressionTextView.text.toString()
        if (expression.isEmpty()) return

        try {
            val result = evaluate(expression)
            resultTextView.text = result.toString()
        } catch (e: Exception) {
            resultTextView.text = "Error"
        }
    }

    private fun isOperator(c: String): Boolean {
        return c == "+" || c == "-" || c == "*" || c == "/"
    }

    // Simple expression evaluator supporting +, -, *, / and decimal numbers
    private fun evaluate(expr: String): Double {
        val tokens = tokenize(expr)
        val values = Stack<Double>()
        val ops = Stack<Char>()

        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            when {
                token.isDouble() -> values.push(token.toDouble())
                token.length == 1 && isOperator(token) -> {
                    while (ops.isNotEmpty() && hasPrecedence(token[0], ops.peek())) {
                        val val2 = values.pop()
                        val val1 = values.pop()
                        val op = ops.pop()
                        values.push(applyOp(op, val1, val2))
                    }
                    ops.push(token[0])
                }
                else -> throw IllegalArgumentException("Invalid token: $token")
            }
            i++
        }

        while (ops.isNotEmpty()) {
            val val2 = values.pop()
            val val1 = values.pop()
            val op = ops.pop()
            values.push(applyOp(op, val1, val2))
        }

        return values.pop()
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var number = ""
        for (c in expr) {
            if (c.isDigit() || c == '.') {
                number += c
            } else if (isOperator(c.toString())) {
                if (number.isNotEmpty()) {
                    tokens.add(number)
                    number = ""
                }
                tokens.add(c.toString())
            } else {
                throw IllegalArgumentException("Invalid character: $c")
            }
        }
        if (number.isNotEmpty()) {
            tokens.add(number)
        }
        return tokens
    }

    private fun hasPrecedence(op1: Char, op2: Char): Boolean {
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) {
            return false
        }
        return true
    }

    private fun applyOp(op: Char, a: Double, b: Double): Double {
        return when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> {
                if (b == 0.0) throw ArithmeticException("Division by zero")
                a / b
            }
            else -> 0.0
        }
    }

    private fun String.isDouble(): Boolean {
        return this.toDoubleOrNull() != null
    }
}
