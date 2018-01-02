package edu.uchicago.cs.encsel.query.tpch

import org.hamcrest.CoreMatchers._
import org.junit.Assert._
import org.junit.Test

class EqualIntTest {

  @Test
  def testPredicate: Unit = {

    val entryWidth = 6
    val pred = new EqualInt(35, entryWidth)
    val data = Array(3, 1, 5, 2, 35, 0, 0, 1, 7, 35, 0, 0, 1, 2, 3, 35, 0, 0, 35, 1)
    val encoded = Encoder.encode(data, entryWidth)

    val result = pred.execute(encoded, 0, data.length)

    val mask = (1 << entryWidth) - 1

    for (i <- data.indices) {
      val bitcount = (i + 1) * entryWidth - 1
      val bitIndex = bitcount / 8
      val bitOffset = bitcount % 8

      val test = result.get(bitIndex) & (1 << bitOffset)
      if (data(i) == 35) {
        assertEquals(i.toString, 0, test)
      }
      else {
        assertThat(i.toString, 0, not(test))
      }
    }
  }
}
