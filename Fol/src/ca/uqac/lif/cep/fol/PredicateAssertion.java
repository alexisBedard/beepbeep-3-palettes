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
package ca.uqac.lif.cep.fol;

import java.util.Set;

import ca.uqac.lif.cep.Context;
import ca.uqac.lif.cep.EventTracker;
import ca.uqac.lif.cep.functions.Function;

/**
 * An assertion on the value of a predicate, to be evaluated on an
 * {@link Interpretation}.
 * <p>
 * While a {@link PredicateTuple} is an object that <em>defines</em>
 * the value of a predicate for a given set of arguments, a
 * {@link PredicateAssertion} <em>queries</em> an interpretation to
 * fetch the corresponding truth value for the arguments.
 */
public class PredicateAssertion extends Function
{
	/**
	 * The name of the predicate to evaluate
	 */
	protected String m_predicateName;
	
	/**
	 * The arguments of this predicate
	 */
	protected Function[] m_arguments;
	
	/**
	 * Creates a new predicate assertion
	 * @param predicate_name The name of the predicate to evaluate
	 * @param arguments The arguments of this predicate
	 */
	public PredicateAssertion(String predicate_name, Function ... arguments)
	{
		super();
		m_predicateName = predicate_name;
		m_arguments = arguments;
	}

	@Override
	public void evaluate(Object[] inputs, Object[] outputs, Context context, EventTracker tracker) 
	{
		Interpretation inter = (Interpretation) inputs[0];
		if (!inter.containsPredicate(m_predicateName))
		{
			// Closed world assumption
			outputs[0] = false;
			return;
		}
		Predicate pred = inter.getPredicate(m_predicateName);
		Object[] values = new Object[m_arguments.length];
		for (int i = 0; i < m_arguments.length; i++)
		{
			Object[] val = new Object[1];
			m_arguments[i].evaluate(inputs, val, context);
			values[i] = val[0];
		}
		pred.evaluate(values, outputs, context);
	}

	@Override
	public void evaluate(Object[] inputs, Object[] outputs) 
	{
		evaluate(inputs, outputs, null);
	}

	@Override
	public int getInputArity() 
	{
		// Arity is 1, since the assertion recieves an *interpretation*
		// as its input
		return 1;
	}

	@Override
	public int getOutputArity() 
	{
		return 1;
	}

	@Override
	public void reset() 
	{
	  super.reset();
		for (Function f : m_arguments)
		{
			f.reset();
		}
	}

	@Override
	public Function duplicate(boolean with_state) 
	{
		Function[] arguments = new Function[m_arguments.length];
		for (int i = 0; i < m_arguments.length; i++)
		{
			arguments[i] = m_arguments[i].duplicate(with_state);
		}
		return new PredicateAssertion(m_predicateName, arguments);
	}

	@Override
	public void getInputTypesFor(Set<Class<?>> classes, int index)
	{
		classes.add(Interpretation.class);
	}

	@Override
	public Class<?> getOutputTypeFor(int index)
	{
		return Boolean.class;
	}
	
	@Override
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		out.append(m_predicateName).append("(");
		for (int i = 0; i < m_arguments.length; i++)
		{
			if (i > 0)
			{
				out.append(",");
			}
			out.append(m_arguments[i]);
		}
		out.append(")");
		return out.toString();
	}

}