package no.tk.behavior.activity.variables;

import no.tk.behavior.activity.values.IntegerValue;

public class IntegerVariable extends Variable<IntegerValue> {

  public IntegerVariable(String name, int initialValue) {
    super(name, new IntegerValue(initialValue));
  }
}
