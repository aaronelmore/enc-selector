/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Contributors:
 *     Hao Jiang - initial API and implementation
 */

package edu.uchicago.cs.encsel.ptnmining.compose

import org.junit.Test
import org.junit.Assert._

class PatternComposerTest {

  @Test
  def testComposeSimple: Unit = {
    val composer = new PatternComposer("(\\w+)-(\\w+):(\\d+)")
    assertEquals("ABC-MAR:KEE", composer.compose(Seq("ABC", "MAR", "KEE")))
    try {
      composer.compose(Seq("AA", "KK"))
      fail("Should not reach here")

    } catch {
      case e: IllegalArgumentException => {}
    }
  }

  @Test
  def testComposeReal: Unit = {
    var composer = new PatternComposer("^(\\d+)-(\\d+)-(\\d+)\\s+(\\d+):(\\d+):(\\d+\\.?\\d*)$")
    assertEquals(0, composer.booleanColumns.length)
    assertEquals(6, composer.numGroup)
    assertEquals("%s-%s-%s %s:%s:%s", composer.format)

    composer = new PatternComposer("^MIR-([0-9a-fA-F]+)-([0-9a-fA-F]+)-(\\d+)(-)?(\\d*)$")
    assertEquals(1, composer.booleanColumns.length)
    assertEquals(5, composer.numGroup)
    assertEquals("MIR-%s-%s-%s%s%s", composer.format)
  }
}
