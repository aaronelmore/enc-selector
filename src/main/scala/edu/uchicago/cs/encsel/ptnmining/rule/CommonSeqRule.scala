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
 */

package edu.uchicago.cs.encsel.ptnmining.rule

import edu.uchicago.cs.encsel.ptnmining.parser.TWord
import edu.uchicago.cs.encsel.ptnmining._

import scala.collection.mutable.ArrayBuffer

/**
  * Look for common sequence from a union and split it into smaller pieces
  *
  * Created by harper on 3/27/17.
  */
class CommonSeqRule extends RewriteRule {

  def rewrite(ptn: Pattern): Pattern = {
    // First look for union and extract common patterns from it
    modify(ptn, p => p.isInstanceOf[PUnion], update).get
  }

  protected def update(union: Pattern): Pattern = {
    // flatten the union content
    val unionData = union.asInstanceOf[PUnion].content.map(p => {
      p match {
        case seq: PSeq => seq.content
        case _ => Array(p).toSeq
      }
    }).toSeq
    val cseq = new CommonSeq
    // Look for common sequence
    val seq = cseq.find(unionData, compare)
    if (!seq.isEmpty) {
      // Common Seq split tokens into pieces
      val commonPos = cseq.positions
      val beforeBuffer = new ArrayBuffer[Pattern]
      val afterBuffer = new ArrayBuffer[Pattern]
      //
      commonPos.zip(unionData).foreach(lp => {
        val pos = lp._1
        val data = lp._2

        beforeBuffer += (pos._1 match {
          case 0 => PEmpty
          case _ => new PSeq(data.slice(0, pos._1))
        })
        afterBuffer += (pos._1 + pos._2 match {
          case len if len == data.length => PEmpty
          case _ => new PSeq(data.slice(pos._2, data.length))
        })
      })

      // Create new pattern
      happen
      new PSeq(Array(new PUnion(beforeBuffer), new PSeq(seq), new PUnion(afterBuffer)))
    } else
      union
  }

  private def compare(a: Pattern, b: Pattern): Boolean = {
    (a, b) match {
      case (pta: PToken, ptb: PToken) => {
        if (pta.token.getClass != ptb.token.getClass) {
          false
        } else {
          !pta.token.isInstanceOf[TWord] ||
            pta.token.value.equals(ptb.token.value)
        }
      }
      case (pua: PUnion, pub: PUnion) => {
        // TODO There's no need to compare union, temporarily return false
        false
      }
      case (psa: PSeq, psb: PSeq) => {
        (psa.content.length == psb.content.length) &&
          psa.content.zip(psb.content).map(p => compare(p._1, p._2))
            .reduce((b1, b2) => b1 || b2)
      }
      case _ => {
        a == b
      }
    }
  }
}
