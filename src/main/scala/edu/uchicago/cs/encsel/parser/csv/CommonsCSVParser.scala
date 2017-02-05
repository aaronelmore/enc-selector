/**
 * *****************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Contributors:
 *     Hao Jiang - initial API and implementation
 *
 * *****************************************************************************
 */
package edu.uchicago.cs.encsel.parser.csv

import java.io.File
import java.io.FileReader
import java.io.Reader
import java.net.URI

import scala.collection.JavaConversions.asScalaIterator
import scala.collection.JavaConversions.iterableAsScalaIterable

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord

import edu.uchicago.cs.encsel.parser.Parser
import edu.uchicago.cs.encsel.parser.Record
import edu.uchicago.cs.encsel.schema.Schema

class CommonsCSVParser extends Parser {

  override def parse(inputFile: URI, schema: Schema): Iterable[Record] = {
    parse(new FileReader(new File(inputFile)), schema)
  }

  protected def parse(reader: Reader, schema: Schema): Iterable[Record] = {
    this.schema = schema

    var format = CSVFormat.EXCEL
    if (schema != null && schema.hasHeader) {
      format = format.withFirstRecordAsHeader()
    }
    var parser = format.parse(reader)

    var csvrecords = parser.iterator()

    if (schema == null) {
      // Fetch a record to guess schema name
      var firstrec = csvrecords.next()
      guessedHeader = firstrec.iterator().toArray
    }

    return new java.lang.Iterable[Record]() {
      override def iterator(): java.util.Iterator[Record] = new MyIterator(csvrecords)
    }
    //iterator.map(new CSVRecordWrapper(_)).toIterable
  }

  var guessedHeader: Array[String] = null;

  override def guessHeaderName: Array[String] = guessedHeader
}

class MyIterator(inner: Iterator[CSVRecord]) extends java.util.Iterator[Record] {
  def hasNext: Boolean = inner.hasNext
  def next(): CSVRecordWrapper = new CSVRecordWrapper(inner.next())
}

class CSVRecordWrapper(inner: CSVRecord) extends Record {
  var innerRecord = inner;

  def apply(idx: Int): String = {
    inner.get(idx)
  }
  def length(): Int = {
    inner.size()
  }
  override def toString(): String = {
    inner.toString()
  }
  def iterator(): Iterator[String] = {
    inner.iterator()
  }
}