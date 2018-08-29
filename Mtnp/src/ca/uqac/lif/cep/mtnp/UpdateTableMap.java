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
package ca.uqac.lif.cep.mtnp;

import java.util.Map;

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.UniformProcessor;
import ca.uqac.lif.mtnp.table.HardTable;
import ca.uqac.lif.mtnp.table.TableEntry;

public class UpdateTableMap extends UniformProcessor
{
	protected HardTable m_table;

	public UpdateTableMap(String ... col_names)
	{
		super(1, 1);
		m_table = new HardTable(col_names);
	}
	
	protected TableEntry createEntry(Object[] inputs)
	{
		String[] col_names = m_table.getColumnNames();
		TableEntry e = new TableEntry();
		for (int i = 0; i < Math.min(col_names.length, inputs.length); i++)
		{
			e.put(col_names[i], inputs[i]);
		}
		return e;
	}
	
	@Override
	public void reset()
	{
		m_table = new HardTable(m_table.getColumnNames());
	}

  @Override
  protected boolean compute(Object[] inputs, Object[] outputs)
  {
    @SuppressWarnings("unchecked")
    Map<String,Object> map = (Map<String,Object>) inputs[0];
    TableEntry te = new TableEntry();
    for (String s : m_table.getColumnNames())
    {
      te.put(s, map.get(s));
    }
    m_table.add(te);
    return true;
  }

  @Override
  public Processor duplicate(boolean with_state)
  {
    // TODO Auto-generated method stub
    return null;
  }
}
