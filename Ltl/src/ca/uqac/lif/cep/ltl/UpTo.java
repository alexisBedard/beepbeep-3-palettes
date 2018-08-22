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
package ca.uqac.lif.cep.ltl;

import java.util.Queue;
import java.util.ArrayDeque;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.SynchronousProcessor;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.ltl.Troolean.Value;

/**
 * Troolean version of the LTL <b>U</b> operator
 * @author Sylvain Hallé
 */
public class UpTo extends SynchronousProcessor 
{
	protected CumulativeFunction<Value> m_left;

	protected CumulativeFunction<Value> m_right;

	protected Value m_currentValue;

	public UpTo()
	{
		super(2, 1);
		m_left = new CumulativeFunction<Value>(Troolean.AND_FUNCTION);
		m_right = new CumulativeFunction<Value>(Troolean.OR_FUNCTION);
		m_currentValue = Value.INCONCLUSIVE;
	}

	@Override
	public void reset()
	{
		super.reset();
		m_left.reset();
		m_right.reset();
		m_currentValue = Value.INCONCLUSIVE;
	}

	@Override
	protected boolean compute(Object[] input, Queue<Object[]> outputs)
	{
		Value left = Troolean.trooleanValue(input[0]);
		Value right = Troolean.trooleanValue(input[1]);
		if (m_currentValue == Value.INCONCLUSIVE)
		{
			Value v_left = m_left.getValue(left);
			Value v_right = m_right.getValue(right);
			if (v_right == Value.TRUE)
			{
				m_currentValue = Value.TRUE;
			}
			else if (v_left == Value.FALSE)
			{
				m_currentValue = Value.FALSE;
			}
		}
		outputs.add(new Object[] {m_currentValue});
		return true;
	}

	public static void build(ArrayDeque<Object> stack) 
	{
		stack.pop(); // (
		Processor right = (Processor) stack.pop();
		stack.pop(); // )
		stack.pop(); // U
		stack.pop(); // (
		Processor left = (Processor) stack.pop();
		stack.pop(); // )
		Until op = new Until();
		Connector.connect(left, 0, op, 0);
		Connector.connect(right, 0, op, 1);
		stack.push(op);
	}

	@Override
	public UpTo duplicate(boolean with_state)
	{
		UpTo u = new UpTo();
		u.m_left = m_left.duplicate(with_state);
		u.m_right = m_right.duplicate(with_state);
		if (with_state)
		{
			u.m_currentValue = m_currentValue;
		}
		return u;
	}
}
