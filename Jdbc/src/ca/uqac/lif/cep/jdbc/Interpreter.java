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
package ca.uqac.lif.cep.jdbc;

import ca.uqac.lif.cep.Pullable;

/**
 * Generic object that can be used to parse string "queries" and
 * return an iterable result
 */
public interface Interpreter
{
  /**
   * Resets the state of the interpreter
   */
  public void reset();
  
  /**
   * Executes a query
   * @param query A string representing the query to execute
   * @return A pullable that can be used to fetch the results
   * of the query
   * @throws ParseException An exception thrown if the query
   * cannot be parsed by the interpreter
   */
  public Pullable executeQuery(String query) throws ParseException;
  
  /**
   * An exception thrown if the query
   * cannot be parsed by the interpreter
   */
  public static class ParseException extends Exception
  {
    /**
     * Dummy UID
     */
    private static final long serialVersionUID = 1L;
  }
}
