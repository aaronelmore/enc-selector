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
 * under the License,
 *
 * Contributors:
 *     Hao Jiang - initial API and implementation
 *
 */

package edu.uchicago.cs.encsel.ptnmining.matching

import edu.uchicago.cs.encsel.ptnmining.Pattern

object RegexMatcher extends PatternMatcher {

  val regexgen = new GenRegexVisitor

  override def matchon(pattern: Pattern, input: String): Option[Record] = {
    regexgen.reset
    pattern.visit(regexgen)
    val regexstr = regexgen.get
    val groupPatterns = regexgen.list

    val regex = regexstr.r
    val matched = regex.findFirstMatchIn(input)

    matched match {
      case Some(mc) => {
        // matched value by index
        val patternValues = (0 until mc.groupCount).map(mc.group).zip(groupPatterns).map(t => (t._2, t._1)).toMap

        val record = new Record()
        record.values ++= patternValues
        Some(record)
      }
      case None => None
    }
  }
}
