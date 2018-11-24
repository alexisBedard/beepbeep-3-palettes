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
package ca.uqac.lif.cep.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This set of tests shows how BeepBeep can be used as a drop-in replacement
 * in any existing code that uses JDBC. The examples below are all copy-pasted
 * from JDBC examples found on the web, which were designed for use with a
 * relational database such as Oracle or MySQL.
 * 
 * @author Sylvain Hallé
 */
public class JdbcTest
{

	/**
	 * This example was found
	 * <a href="http://java2novice.com/jdbc/read-select/">here</a>. The only
	 * thing thas was changed is the query in the call to
	 * <code>executeQuery()</code>, which contains an ESQL query rather than
	 * an SQL query.
	 */
	@Test
	@Ignore
	public void testSimpleQuery() 
	{
		try 
		{
			Class.forName("ca.uqac.lif.cep.jdbc.BeepBeepDriver");
			Connection con = java.sql.DriverManager.getConnection("jdbc:beepbeep:","user","password");
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT (foo) AS name, (bar) AS salary FROM (THE TUPLES OF (FILE \"tuples.csv\"))");
			while(rs.next())
			{
				int s = rs.getInt("name");
				int sal = rs.getInt("salary");
				System.out.printf("name = %d, salary = %d\n", s, sal);
			}
			rs.close();
			con.close();
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
}