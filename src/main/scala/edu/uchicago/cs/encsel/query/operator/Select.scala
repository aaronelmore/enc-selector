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

package edu.uchicago.cs.encsel.query.operator

import java.net.URI

import edu.uchicago.cs.encsel.dataset.parquet.ParquetReaderHelper
import edu.uchicago.cs.encsel.dataset.parquet.ParquetReaderHelper.ReaderProcessor
import edu.uchicago.cs.encsel.query.util.SchemaUtils
import edu.uchicago.cs.encsel.query.{RowTempTable, _}
import org.apache.parquet.VersionParser.ParsedVersion
import org.apache.parquet.column.impl.ColumnReaderImpl
import org.apache.parquet.column.page.PageReadStore
import org.apache.parquet.hadoop.Footer
import org.apache.parquet.hadoop.metadata.BlockMetaData
import org.apache.parquet.io.api.Binary
import org.apache.parquet.schema.MessageType

import scala.collection.JavaConversions._

trait Select {
  def select(input: URI, p: Predicate, schema: MessageType,
             projectIndices: Array[Int]): TempTable
}

class ColumnTempTablePipe(val table: ColumnTempTable, val index: Int) extends PredicatePipe {

  override def pipe(d: Double) = {
    table.add(index, d)
  };

  override def pipe(b: Binary) = {
    table.add(index, b)
  };

  override def pipe(i: Int) = {
    table.add(index, i)
  };

  override def pipe(l: Long) = {
    table.add(index, l)
  };

  override def pipe(bl: Boolean) = {
    table.add(index, bl)
  };

  override def pipe(f: Float) = {
    table.add(index, f)
  };
}

class RowTempTablePipe(val table: RowTempTable, val index: Int) extends PredicatePipe {

  override def pipe(d: Double) = {
    table.getConverter(index).asPrimitiveConverter().addDouble(d)
  };

  override def pipe(b: Binary) = {
    table.getConverter(index).asPrimitiveConverter().addBinary(b);
  };

  override def pipe(i: Int) = {
    table.getConverter(index).asPrimitiveConverter().addInt(i);
  };

  override def pipe(l: Long) = {
    table.getConverter(index).asPrimitiveConverter().addLong(l);
  };

  override def pipe(bl: Boolean) = {
    table.getConverter(index).asPrimitiveConverter().addBoolean(bl);
  };

  override def pipe(f: Float) = {
    table.getConverter(index).asPrimitiveConverter().addFloat(f);
  };
}

class VerticalSelect extends Select {
  override def select(input: URI, p: Predicate, schema: MessageType,
                      projectIndices: Array[Int]): TempTable = {

    val vp = p.asInstanceOf[VPredicate]
    val recorder = new ColumnTempTable(SchemaUtils.project(schema, projectIndices))

    val predictSet = vp.leaves.map(_.colIndex).toSet
    val projectMap = projectIndices.zipWithIndex.map(i => i._1 -> i._2).toMap

    val allColumnSet = predictSet.union(projectMap.keySet)

    val columnMap = allColumnSet.zipWithIndex.map(f => f._1 -> f._2).toMap

    val nonProjectIndices = columnMap.filter(e => {
      !predictSet.contains(e._1)
    }).map(_._2).toList.sorted


    ParquetReaderHelper.read(input, new ReaderProcessor {
      override def processFooter(footer: Footer): Unit = {}

      override def processRowGroup(version: ParsedVersion, meta: BlockMetaData, rowGroup: PageReadStore): Unit = {
        val columns = schema.getColumns.zipWithIndex.filter(col => {
          columnMap.containsKey(col._2)
        }).map(col => {
          val converter = projectMap.getOrElse(col._2, -1) match {
            case -1 => new NonePrimitiveConverter
            case index => recorder.getConverter(index).asPrimitiveConverter()
          }
          new ColumnReaderImpl(col._1, rowGroup.getPageReader(col._1), converter, version)
        })


        vp.leaves.foreach(leaf => {
          leaf.setColumn(columns(leaf.colIndex))
          // Install a pipe to push data that belongs to output columns
          if (projectMap.containsKey(leaf.colIndex)) {
            leaf.setPipe(new ColumnTempTablePipe(recorder, leaf.colIndex))
          }
        })

        val bitmap = vp.bitmap

        nonProjectIndices.map(columns(_)).foreach(col => {
          for (count <- 0L until rowGroup.getRowCount) {
            if (bitmap.test(count)) {
              col.writeCurrentValueToConverter()
            } else {
              col.skip()
            }
            col.consume()
          }
        })
      }
    })

    return recorder
  }
}

class HorizontalSelect extends Select {
  override def select(input: URI, p: Predicate, schema: MessageType,
                      projectIndices: Array[Int]): RowTempTable = {

    val hp = p.asInstanceOf[HPredicate]
    val projectSchema = SchemaUtils.project(schema, projectIndices)
    val recorder = new RowTempTable(projectSchema)

    val predictSet = hp.leaves.map(_.colIndex).toSet
    val projectMap = projectIndices.zipWithIndex.map(i => i._1 -> i._2).toMap

    val allColumnSet = predictSet.union(projectMap.keySet)

    val columnMap = allColumnSet.zipWithIndex.map(f => f._1 -> f._2).toMap

    val nonProjectIndices = columnMap.filter(e => {
      !predictSet.contains(e._1)
    }).map(_._2).toList.sorted

    ParquetReaderHelper.read(input, new ReaderProcessor {
      override def processFooter(footer: Footer): Unit = {}

      override def processRowGroup(version: ParsedVersion, meta: BlockMetaData, rowGroup: PageReadStore): Unit = {
        val columns = schema.getColumns.zipWithIndex
          .filter(col => {
            columnMap.containsKey(col._2)
          })
          .map(col => {
            val converter = projectMap.getOrElse(col._2, -1) match {
              case -1 => new NonePrimitiveConverter
              case index => recorder.getConverter(index).asPrimitiveConverter()
            }
            new ColumnReaderImpl(col._1, rowGroup.getPageReader(col._1), converter, version)
          })

        hp.leaves.foreach(leaf => {
          leaf.setColumn(columns(leaf.colIndex))
          if (projectMap.containsKey(leaf.colIndex)) {
            leaf.setPipe(new RowTempTablePipe(recorder, columnMap.getOrElse(leaf.colIndex, -1)));
          }
        })


        for (count <- 0L until rowGroup.getRowCount) {
          hp.value match {
            case true => {
              recorder.start()
              nonProjectIndices.map(columns(_)).foreach(col => {
                col.writeCurrentValueToConverter()
                col.consume()
              })
              recorder.end()
            }
            case _ => nonProjectIndices.map(columns(_)).foreach(col => {
              col.skip()
              col.consume()
            })
          }
        }
      }
    })
    return recorder
  }
}
