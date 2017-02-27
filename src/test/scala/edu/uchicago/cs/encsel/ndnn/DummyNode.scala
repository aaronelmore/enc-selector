package edu.uchicago.cs.encsel.ndnn

/**
 * Created by Cathy on 2/18/2017.
 */
class DummyNode(ins: Node*) extends Node(ins: _*) {
  val first = ins(0)
  def compute = assignValue(first.value)
  def updateGrad = inputs.foreach(i => i.grad.addi(this.grad))
}
