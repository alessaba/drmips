/*
    DrMIPS - Educational MIPS simulator
    Copyright (C) 2013-2015 Bruno Nova <brunomb.nova@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.feup.brunonova.drmips.simulator.mips.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.feup.brunonova.drmips.simulator.exceptions.InvalidCPUException;
import org.feup.brunonova.drmips.simulator.util.Point;
import org.junit.Test;
import static org.junit.Assert.*;

public class MultiplexerTest {
	@Test
	public void testComponent() throws InvalidCPUException {
		tComp(11, 32, Arrays.asList(10, 11), 1);
		tComp(10, 16, Arrays.asList(10, 11), 0);
		tComp(10, 32, Arrays.asList(10, 11, 12), 0);
		tComp(11, 32, Arrays.asList(10, 11, 12), 1);
		tComp(12, 32, Arrays.asList(10, 11, 12), 2);
		tComp(13, 32, Arrays.asList(10, 11, 12, 13), 3);
		tComp(18, 32, Arrays.asList(10, 11, 12, 13, 14, 15, 16, 17, 18), 8);
		// what should happen when the selector input points to an inexistant input?
	}

	private void tComp(int expected, int size, List<Integer> in, int sel) throws InvalidCPUException {
		List<String> inIds = new ArrayList<>();
		for(int i = 0; i < in.size(); i++) {
			inIds.add(i + "");
		}
		Multiplexer c = new Multiplexer("test", 0, new Point(0, 0), size, inIds, "sel", "out");

		for(int i = 0; i < in.size(); i++) {
			c.getInput(i).setValue(in.get(i));
		}
		c.getSelector().setValue(sel);
		c.execute();
		assertEquals(expected, c.getOutput().getValue());
	}
}
