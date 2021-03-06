/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2017 Sylvain Hallé

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
package ca.uqac.lif.cep.tuples;

import java.util.ArrayDeque;

import ca.uqac.lif.cep.functions.Function;

class NamedAttributeExpression
{
	public static void build(ArrayDeque<Object> stack)
	{
		Object o;
		Function expression;
		String name = (String) stack.pop();
		stack.pop(); // AS
		o = stack.pop(); // (
		if (o instanceof String)
		{
			expression = (Function) stack.pop();
			//stack.pop(); // )
		}
		else
		{
			expression = (Function) o;
		}
		AttributeExpression ae = new AttributeExpression(expression, name);
		stack.push(ae);
	}
}