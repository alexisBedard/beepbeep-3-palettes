/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2018 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.cep.json;

import java.util.List;

import ca.uqac.lif.cep.functions.UnaryFunction;
import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonPath;
import ca.uqac.lif.json.JsonPath.PathElement;

/**
 * Function that evaluates a JPath expression on a JsonElement
 */
public class JPathFunction extends UnaryFunction<JsonElement,JsonElement> 
{
  /**
   * The JPath expression this function evaluates
   */
  private final List<PathElement> m_path;

  /**
   * Creates a new JPath function
   * @param path The JPath expression to evaluate
   */
  public JPathFunction(String path)
  {
    this(JsonPath.getPathElements(path));
  }

  /**
   * Creates a new XPath function
   * @param path The path elements to evaluate
   */
  public JPathFunction(List<PathElement> path)
  {
    super(JsonElement.class, JsonElement.class);
    m_path = path;
  }

  @Override
  public /*@ null @*/ JsonElement getValue(/*@NonNull*/ JsonElement x)
  {
    return JsonPath.get(x,  m_path);
  }
}