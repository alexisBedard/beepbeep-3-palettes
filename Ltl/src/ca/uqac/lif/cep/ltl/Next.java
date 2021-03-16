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

import ca.uqac.lif.cep.SMVInterface;
import ca.uqac.lif.cep.tmf.Trim;

/**
 * Boolean implementation of the LTL <b>X</b> processor
 * @author Sylvain Hallé
 */
public class Next extends Trim
{
	public Next()
	{
		super(1);
	}
		
	@Override
	public Next duplicate(boolean with_state)
	{
		Next n = new Next();
		if (with_state)
		{
			n.m_inputCount = m_inputCount;
			n.m_hasBeenNotifiedOfEndOfTrace = m_hasBeenNotifiedOfEndOfTrace;
		}
		return n;
	}
}
