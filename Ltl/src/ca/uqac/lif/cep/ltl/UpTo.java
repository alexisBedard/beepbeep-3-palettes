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
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayDeque;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.SMVInterface;
import ca.uqac.lif.cep.SynchronousProcessor;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.ltl.Troolean.Value;

/**
 * Troolean version of the LTL <b>U</b> operator
 * @author Sylvain Hallé
 */
public class UpTo extends SynchronousProcessor  implements SMVInterface
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

	@Override
	public void writingSMV(PrintStream printStream, int Id, int list, int[][] array, int arrayWidth, int maxInputArity,
			String pipeType) throws IOException {
		printStream.printf("MODULE UpTo(lastReceived_1, inc_1, inb_1, lastReceived_2, inc_2, inb_2, ouc_1, oub_1, lastPassed) \n");
		  printStream.printf("	VAR \n");
		  
		  int prec1 = array[Id][arrayWidth - maxInputArity];
		  int prec2 = array[Id][arrayWidth - maxInputArity + 1];
		  printStream.printf("		qc_1 : array 0.."+(list-1)+" of 0..2; \n");
		  printStream.printf("		qb_1 : array 0.."+(list-1)+" of boolean; \n");
		  printStream.printf("		qc_2 : array 0.."+(list-1)+" of 0..2; \n");		  
		  printStream.printf("		qb_2 : array 0.."+(list-1)+" of boolean; \n");
		  printStream.printf("\n");
		  printStream.printf("	ASSIGN \n");
		  printStream.printf("		init(lastPassed) := lastReceived_1 | lastReceived_2; \n");
		  printStream.printf("		next(lastPassed) := next(lastReceived_1) | next(lastReceived_2); \n");
		  printStream.printf("\n");

		  for(int i = 1; i <= 2; i++) {
			  for(int j = 0; j < list; j++) {
				  printStream.printf("		init(qc_"+i+"["+j+"]) := 0 \n");
			  }
		  }
		  for(int i = 1; i <= 2; i++) {
			  for(int j = 0; j < list; j++) {
				  printStream.printf("		init(qb_"+i+"["+j+"]) := FALSE \n");
			  }
		  }

		  printStream.printf("		init(ouc_1) := 0 \n");
		  printStream.printf("		init(oub_1) := FALSE \n");
		 
		  printStream.printf("		init(memory_1) := inc_1; \n");
		  printStream.printf("		init(memory_2) := inc_2; \n");
			
		  printStream.printf("		next(memory_1) := memory_1; \n");
		  printStream.printf("		next(memory_2) := memory_2; \n");
		  
		  //qb variables
		  for(int i = 1; i <= 2; i++) {
			  for(int j = 0; j < list; j++) {
				  printStream.printf("		next(qb_"+i+"["+j+"]) := case \n");
				  if(j == 0) {
					  printStream.printf("			-- inb_"+i+" is the only intput and both waiting lists are empty. \n");
					  if(i == 1) {
						  printStream.printf("			next(inb_1) & next(!inb_2) & !qb_1["+j+"] & !qb_2["+j+"]: TRUE; \n");
					  }
					  if(i == 2) {
						  printStream.printf("			next(!inb_1) & next(inb_2) & !qb_1["+j+"] & !qb_2["+j+"]: TRUE; \n");
					  }
					  if(j+1 == list) {
						  if(i == 1) {
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : FALSE; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] : TRUE; \n");
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : FALSE; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] : TRUE; \n");
						  }
					  }
					  else {
						  if(i == 1) {
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: FALSE; \n");
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: TRUE;\n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE;\n");
							 
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: FALSE; \n");
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: TRUE;\n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE;\n");
						  }
					  }
				  }
				  if(j != 0) {
					  if(j+1 == list) {
						  if(i == 1) {
							  printStream.printf("			-- Only inb_1 and there is something in the waiting list at position "+ (j-1)+" \n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"] : TRUE; \n");
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"] \n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : FALSE; \n");
							  printStream.printf("			-- Both inputs and there is something in qb_"+i+"["+(j-1)+"], something in qb_"+i+"["+j+"] \n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+(j-1)+"] & qb_"+i+"["+j+"]: TRUE; \n");
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_2 and there is something in the waiting list at position"+ (i-1)+" \n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & !qb_"+i+"["+i+"] & qb_"+i+"["+(i-1)+"] : TRUE; \n");
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"] \n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : FALSE; \n");
							  printStream.printf("			-- Both inputs and there is something in qb_"+i+"["+(j-1)+"], something in qb_"+i+"["+j+"] \n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+(j-1)+"] & qb_"+i+"["+j+"]: TRUE; \n");
						  }
					  }
					  else {
						  if(i == 1) {
							  printStream.printf("			-- Only inb_1 and there is something in the waiting list at position "+ (j-1)+" \n ");
							  printStream.printf("			next(inb_1) & next(!inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"]: TRUE; \n"); 
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: FALSE; \n");
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: TRUE;\n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE;\n");
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_2 and there is something in the waiting list at position "+ (j-1)+" \n ");
							  printStream.printf("			next(!inb_1) & next(inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"]: TRUE; \n"); 
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: FALSE; \n");
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: TRUE;\n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE;\n");
						  }
					  }
				  }
				  
				  if(j+1 == list) {
					  if(i == 1) {
						  printStream.printf("			--Waiting list is full.\n");
						  printStream.printf("			next(inb_1) & next(!inb_2) & qb_1["+j+"] & !qb_2[0]: TRUE;\n");
					  }
					  if(i == 2) {
						  printStream.printf("			--Waiting list is full.\n");
						  printStream.printf("			next(!inb_1) & next(inb_2) & qb_2["+j+"] & !qb_1[0]: TRUE;\n");
					  }
				  }
				  
				  printStream.printf("			TRUE : qb_"+i+"["+j+"]; \n");
				  printStream.printf("		esac; \n");
				  printStream.printf("\n");
			  }
		  }
		  
		//qc variables
		  for(int i = 1; i <= 2; i++) {
			  for(int j = 0; j < list; j++) {
				  printStream.printf("		next(qc_"+i+"["+j+"]) := case \n");
				  if(j == 0) {
					  printStream.printf("			-- inb_"+i+" is the only intput and both waiting lists are empty. \n");
					  if(i == 1) {
						  printStream.printf("			next(inb_1) & next(!inb_2) & !qb_1["+j+"] & !qb_2["+j+"]: next(inc_1); \n");
					  }
					  if(i == 2) {
						  printStream.printf("			next(!inb_1) & next(inb_2) & !qb_1["+j+"] & !qb_2["+j+"]: next(inc_2); \n");
					  }
					  if(j+1 == list) {
						  if(i == 1) {
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : 0; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] : next(inc_1); \n");
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : 0; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] : next(inc_2); \n");
						  }
					  }
					  else {
						  if(i == 1) {
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: 0; \n");
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"]; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: next(inc_1);\n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"];\n");
							 
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: 0; \n");
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"]; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: next(inc_2);\n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"];\n");
						  }
					  }
				  }
				  if(j != 0) {
					  if(j+1 == list) {
						  if(i == 1) {
							  printStream.printf("			-- Only inb_1 and there is something in the waiting list at position "+ (j-1)+" \n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"] : next(inc_1); \n");
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"] \n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : 0; \n");
							  printStream.printf("			-- Both inputs and there is something in qb_"+i+"["+(j-1)+"], something in qb_"+i+"["+j+"] \n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+(j-1)+"] & qb_"+i+"["+j+"]: next(inc_1); \n");
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_2 and there is something in the waiting list at position"+ (i-1)+" \n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"] : next(inc_2); \n");
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"] \n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : 0; \n");
							  printStream.printf("			-- Both inputs and there is something in qb_"+i+"["+(j-1)+"], something in qb_"+i+"["+j+"] \n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+(j-1)+"] & qb_"+i+"["+j+"]: next(inc_2); \n");
						  }
					  }
					  else {
						  if(i == 1) {
							  printStream.printf("			-- Only inb_1 and there is something in the waiting list at position "+ (j-1)+" \n ");
							  printStream.printf("			next(inb_1) & next(!inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"]: next(inc_1); \n"); 
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: 0; \n");
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"]; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: next(inc_1);\n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"];\n");
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_2 and there is something in the waiting list at position "+ (j-1)+" \n ");
							  printStream.printf("			next(!inb_1) & next(inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"]: next(inc_2); \n"); 
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: 0; \n");
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"]; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: next(inc_2);\n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"];\n");
						  }
					  }
				  }
				  
				  if(j+1 == list) {
					  if(i == 1) {
						  printStream.printf("			--Waiting list is full.\n");
						  printStream.printf("			next(inb_1) & next(!inb_2) & qb_1["+j+"] & !qb_2[0]: next(inc_1);\n");
					  }
					  if(i == 2) {
						  printStream.printf("			--Waiting list is full.\n");
						  printStream.printf("			next(!inb_1) & next(inb_2) & qb_2["+j+"] & !qb_1[0]: next(inc_2);\n");
					  }
				  }
				  
				  printStream.printf("			TRUE : qc_"+i+"["+j+"]; \n");
				  printStream.printf("		esac; \n");
				  printStream.printf("\n");
			  }
		  }
		  	printStream.printf("		next(oub_1) := case \n");
		  	printStream.printf("			--ON NE PEUT RIEN CONCLURE POUR LE MOMENT, LA CHAINE CONTINUE \n");
		  	printStream.printf("			(next(inb_1) & qb_2[0]) & (next(inc_1) = memory_1) & (qc_2[0] = memory_2) : FALSE; \n");
		  	printStream.printf("			(next(inb_2) & qb_1[0]) & (next(inc_2) = memory_2) & (qc_1[0] = memory_1)  : FALSE; \n");
		  	printStream.printf("			(next(inb_1) & next(inb_2)) & (next(inc_1) = memory_1) & (next(inc_2) = memory_2) : FALSE; \n");
			
		  	printStream.printf("			-- P CHANGE ALORS QUE Q N'A PAS ENCORE CHANGÉ \n");
		  	printStream.printf("			(next(inb_1) & qb_2[0]) & (next(inc_1) != memory_1) & (qc_2[0] = memory_2) : TRUE; \n");
			printStream.printf("			(next(inb_2) & (next(inc_2) = memory_2) & (qc_1[0] != memory_1)  : TRUE; \n");
			printStream.printf("			(next(inb_1) & next(inb_2)) & (next(inc_1) != memory_1) & (next(inc_2) = memory_2) : TRUE; \n");
			
			printStream.printf("			-- Q CHANGE, PAS BESOIN DE CONNAITRE LA VALEUR DE P NEXT \n");
			printStream.printf("			(next(inb_1) & qb_2[0]) & (qc_2[0] != memory_2) : TRUE; \n");
			printStream.printf("			(next(inb_2) & qb_1[0]) & (next(inc_2) != memory_2) : TRUE; \n");
			printStream.printf("			(next(inb_1) & next(inb_2)) & (next(inc_2) != memory_2) : TRUE; \n");
			
			printStream.printf("			TRUE : FALSE; \n");
			printStream.printf("		esac; \n");
			printStream.printf("\n");
		
			printStream.printf("		next(ouc_1) := case \n");
			printStream.printf("			oub_1 : 1; \n");
			
			printStream.printf("			--ON NE PEUT RIEN CONCLURE POUR LE MOMENT, LA CHAINE CONTINUE \n");
			printStream.printf("			(next(inb_1) & qb_2[0]) & (next(inc_1) = 1) & (qc_2[0] = 0) : 0; \n");
			printStream.printf("			(next(inb_2) & qb_1[0]) & (next(inc_2) = 0) & (qc_1[0] = 1)  : 0; \n");
			printStream.printf("			(next(inb_1) & next(inb_2)) & (next(inc_1) = 1) & (next(inc_2) = 0) : 0; \n");
			
			printStream.printf("			-- P CHANGE ALORS QUE Q N'A PAS ENCORE CHANGÉ \n");
			printStream.printf("			(next(inb_1) & qb_2[0]) & (next(inc_1) = 0) & (qc_2[0] = 0) : 0; \n");
			printStream.printf("			(next(inb_2) & qb_1[0]) & (next(inc_2) = 0) & (qc_1[0] = 0)  : 0; \n");
			printStream.printf("			(next(inb_1) & next(inb_2)) & (next(inc_1) = 0) & (next(inc_2) = 0) : 0; \n");
			
			printStream.printf("			-- Q CHANGE, PAS BESOIN DE CONNAITRE LA VALEUR DE P NEXT \n");
			printStream.printf("			(next(inb_1) & qb_2[0]) & (qc_2[0] = 1) : 1; \n");
			printStream.printf("			(next(inb_2) & qb_1[0]) & (next(inc_2) = 1) : 1; \n");
			printStream.printf("			(next(inb_1) & next(inb_2)) & (next(inc_2) = 1) : 1; \n");
			
			printStream.printf("			TRUE : 0; \n");
			printStream.printf("		esac; \n");
		   
		
	}

	@Override
	public void writePipes(PrintStream printStream, int ProcId, int[][] connectionArray) throws IOException {
		printStream.printf("		--UpTo \n");
		printStream.printf("		pipe_"+ProcId+" : boolean;\n");
		printStream.printf("		b_pipe_"+ProcId+ " : boolean; \n");
		printStream.printf("		lastPassed_"+ProcId+": boolean; \n");
		
	}
}
