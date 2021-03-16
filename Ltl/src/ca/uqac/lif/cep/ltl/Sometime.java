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

import java.io.IOException;
import java.io.PrintStream;

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.SMVInterface;
import ca.uqac.lif.cep.ltl.Troolean.Value;
import ca.uqac.lif.cep.tmf.SinkLast;

/**
 * Troolean implementation of the LTL <b>F</b> processor
 * @author Sylvain Hallé
 */
public class Sometime extends UnaryOperator implements SMVInterface
{
	public Sometime(Processor p)
	{
		super(p);
	}
	
	public Sometime() 
	{
		super();
	}

	@Override
	protected boolean compute(Object[] inputs, Object[] outputs) 
	{
		if (m_lastValue != Value.INCONCLUSIVE)
		{
			outputs[0] = m_lastValue;
			return true;
		}
		spawn();
		for (int i = 0; i < m_pushables.size(); i++)
		{
			Pushable p = m_pushables.get(i);
			p.push(inputs[0]);
			SinkLast sink = m_sinks.get(i);
			Troolean.Value val = (Troolean.Value) sink.getLast()[0];
			if (val == Value.TRUE)
			{
				m_lastValue = Value.TRUE;
				m_sinks.clear();
				m_processors.clear();
				m_pushables.clear();
				outputs[0] = m_lastValue;
				return true;
			}
			if (val == Value.FALSE)
			{
				m_pushables.remove(i);
				m_sinks.remove(i);
				m_processors.remove(i);
				i--;
			}
		}
		outputs[0] = Value.INCONCLUSIVE;
		return true;
	}

	@Override
	public Sometime duplicate(boolean with_state) 
	{
		Sometime st = new Sometime(m_processor.duplicate());
		super.cloneInto(st, with_state);
		return st;
	}

	@Override
	public void writingSMV(PrintStream printStream, int Id, int list, int[][] array, int arrayWidth, int maxInputArity,
			String pipeType) throws IOException {
		printStream.printf("MODULE Sometime(lastReceived, inc_1, inb_1,ouc_1, oub_1, lastPassed)\n");
		printStream.printf("	ASSIGN \n");
		printStream.printf("		init(ouc_1) := case \n");
		printStream.printf("		inc_1 = 1 : 1; \n");
		printStream.printf("		TRUE : 2; \n");
		printStream.printf("	esac; \n");
			
		printStream.printf("	init(oub_1) := case \n");
		printStream.printf("		inb_1 = TRUE : TRUE; \n");
		printStream.printf("		TRUE : FALSE; \n");
		printStream.printf("	esac; \n");
		
		printStream.printf("	next(ouc_1):= case \n");
		printStream.printf("		next(inc_1) = 1  : 1;\n");
		printStream.printf("		oub_1 : 1; \n");
		printStream.printf("		TRUE : 2; \n");
		printStream.printf("	esac; \n");

		printStream.printf("	next(oub_1) := case \n");
		printStream.printf("		next(inb_1) = TRUE  : TRUE; \n");
		printStream.printf("		(ouc_1 = 1) & next(inb_1) : TRUE; \n");
		printStream.printf("		(ouc_1 = 1) & next(!inb_1) : FALSE; \n");
		printStream.printf("		TRUE : FALSE; \n");
		printStream.printf("	esac; \n");
		
		printStream.printf("		init(lastPassed) := lastReceived; \n");
		printStream.printf("		next(lastPassed) := next(lastReceived); \n");
	}

	@Override
	public void writePipes(PrintStream printStream, int ProcId, int[][] connectionArray) throws IOException {
		printStream.printf("		--Sometime \n");
		printStream.printf("		pipe_"+ProcId+" : boolean;\n");
		printStream.printf("		b_pipe_"+ProcId+ " : boolean; \n");
		printStream.printf("		lastPassed_"+ProcId+": boolean; \n");
		
	}
}