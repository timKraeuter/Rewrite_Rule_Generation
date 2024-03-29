package no.tk.behavior.activity.expression.integer;

import no.tk.behavior.activity.expression.BinaryExpression;
import no.tk.behavior.activity.values.Value;
import no.tk.behavior.activity.variables.IntegerVariable;
import no.tk.behavior.activity.variables.Variable;

public abstract class IntegerExpression implements BinaryExpression {
  private final IntegerVariable operand1;
  private final IntegerVariable operand2;

  protected IntegerExpression(IntegerVariable operand1, IntegerVariable operand2) {
    this.operand1 = operand1;
    this.operand2 = operand2;
  }

  public abstract <V extends Value> Variable<V> getAssignee();

  @Override
  public String getNameOfOperand1() {
    return this.operand1.getName();
  }

  @Override
  public String getNameOfOperand2() {
    return this.operand2.getName();
  }

  @Override
  public String getNameOfAssignee() {
    return this.getAssignee().getName();
  }
}
