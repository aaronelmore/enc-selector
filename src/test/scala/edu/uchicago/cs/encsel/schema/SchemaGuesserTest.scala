package edu.uchicago.cs.encsel.schema

import org.junit.Test
import org.junit.Assert._
import java.io.File
import edu.uchicago.cs.encsel.model.DataType

class SchemaGuesserTest {

  @Test
  def testTestType(): Unit = {
    var guess = new SchemaGuesser

    assertEquals(DataType.INTEGER, guess.testType("22", DataType.BOOLEAN))
    assertEquals(DataType.BOOLEAN, guess.testType("1", DataType.BOOLEAN))
    assertEquals(DataType.BOOLEAN, guess.testType("0", DataType.BOOLEAN))
    assertEquals(DataType.BOOLEAN, guess.testType("true", DataType.BOOLEAN))
    assertEquals(DataType.BOOLEAN, guess.testType("False", DataType.BOOLEAN))
    assertEquals(DataType.BOOLEAN, guess.testType("Yes", DataType.BOOLEAN))
    assertEquals(DataType.BOOLEAN, guess.testType("nO", DataType.BOOLEAN))

    assertEquals(DataType.INTEGER, guess.testType("22", DataType.INTEGER))
    assertEquals(DataType.DOUBLE, guess.testType("22.54", DataType.INTEGER))
    assertEquals(DataType.STRING, guess.testType("Goodman", DataType.INTEGER))

    assertEquals(DataType.DOUBLE, guess.testType("22.5", DataType.DOUBLE))
    assertEquals(DataType.DOUBLE, guess.testType("3234", DataType.DOUBLE))
    assertEquals(DataType.DOUBLE, guess.testType("5", DataType.DOUBLE))
    assertEquals(DataType.STRING, guess.testType("Goews", DataType.DOUBLE))

    assertEquals(DataType.STRING, guess.testType("Goews", DataType.STRING))
    assertEquals(DataType.STRING, guess.testType("32", DataType.STRING))
    assertEquals(DataType.STRING, guess.testType("32.323", DataType.STRING))
  }

  @Test
  def testGuessSchema(): Unit = {
    var guess = new SchemaGuesser()

    var csvSchema = guess.guessSchema(new File("src/test/resource/test_guess_schema.csv").toURI())

    assertEquals(5, csvSchema.columns.size)
    assertEquals(DataType.DOUBLE, csvSchema.columns(0)._1)
    assertEquals(DataType.STRING, csvSchema.columns(1)._1)
    assertEquals(DataType.LONG, csvSchema.columns(2)._1)
    assertEquals(DataType.STRING, csvSchema.columns(3)._1)
    assertEquals(DataType.INTEGER, csvSchema.columns(4)._1)

    var xlsxSchema = guess.guessSchema(new File("src/test/resource/test_guess_schema.xlsx").toURI())

    assertEquals(4, xlsxSchema.columns.size)

    assertEquals(DataType.DOUBLE, xlsxSchema.columns(0)._1)
    assertEquals(DataType.STRING, xlsxSchema.columns(1)._1)
    assertEquals(DataType.LONG, xlsxSchema.columns(2)._1)
    assertEquals(DataType.STRING, xlsxSchema.columns(3)._1)

    var jsonSchema = guess.guessSchema(new File("src/test/resource/test_guess_schema.json").toURI())

    assertEquals(4, jsonSchema.columns.size)

    assertEquals(DataType.DOUBLE, jsonSchema.columns(0)._1)
    assertEquals(DataType.STRING, jsonSchema.columns(1)._1)
    assertEquals(DataType.INTEGER, jsonSchema.columns(2)._1)
    assertEquals(DataType.STRING, jsonSchema.columns(3)._1)

    var tsvSchema = guess.guessSchema(new File("src/test/resource/test_guess_schema.tsv").toURI())

    assertEquals(5, tsvSchema.columns.size)

    assertEquals(DataType.DOUBLE, tsvSchema.columns(0)._1)
    assertEquals(DataType.STRING, tsvSchema.columns(1)._1)
    assertEquals(DataType.LONG, tsvSchema.columns(2)._1)
    assertEquals(DataType.INTEGER, tsvSchema.columns(3)._1)
    assertEquals(DataType.STRING, tsvSchema.columns(4)._1)

  }
}