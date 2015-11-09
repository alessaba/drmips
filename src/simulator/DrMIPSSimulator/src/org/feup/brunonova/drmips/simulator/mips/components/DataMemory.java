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

import java.util.Stack;
import org.feup.brunonova.drmips.simulator.exceptions.InvalidCPUException;
import org.feup.brunonova.drmips.simulator.mips.Component;
import org.feup.brunonova.drmips.simulator.mips.Data;
import org.feup.brunonova.drmips.simulator.mips.IOPort;
import org.feup.brunonova.drmips.simulator.mips.Input;
import org.feup.brunonova.drmips.simulator.mips.IsSynchronous;
import org.feup.brunonova.drmips.simulator.mips.Output;
import org.feup.brunonova.drmips.simulator.util.Dimension;
import org.feup.brunonova.drmips.simulator.util.Point;

/**
 * Class that represents the data memory.
 * 
 * @author Bruno Nova
 */
public class DataMemory extends Component implements IsSynchronous {
	/** The minimum size of the memory (in ints). */
	public static final int MINIMUM_SIZE = 20;
	/** The maximum size of the memory (in ints). */
	public static final int MAXIMUM_SIZE = 500;
	
	private final Input address, writeData, memRead, memWrite;
	private final Output output;
	private int[] memory;
	private final Stack<int[]> states = new Stack<>(); // previous values
	
	/**
	 * Data memory contructor.
	 * @param id Data memory's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param size The size of the memory (number of 32 bits positions).
	 * @param addressId The identifier of the address input.
	 * @param writeDataId The identifier of the write data input.
	 * @param outId The identifier of the output.
	 * @param memReadId The identifier of the MemRead input.
	 * @param memWriteId The identifier of the MemWrite input.
	 * @throws InvalidCPUException If <tt>id</tt> is empty or duplicated.
	 */
	public DataMemory(String id, int latency, Point position, int size, String addressId, String writeDataId, String outId, String memReadId, String memWriteId) throws InvalidCPUException {
		super(id, latency, "Data\nmemory", "data_memory", "data_memory_description", position, new Dimension(80, 100));
		
		if(size < MINIMUM_SIZE || size > MAXIMUM_SIZE)
			throw new InvalidCPUException("Invalid data memory size! Must be between " + MINIMUM_SIZE + " and " + MAXIMUM_SIZE + " positions (each position has 32 bits).");
		
		memory = new int[size];
		address = addInput(addressId, new Data(), IOPort.Direction.WEST, true, true);
		writeData = addInput(writeDataId, new Data(), IOPort.Direction.WEST, false, true);
		memRead = addInput(memReadId, new Data(1), IOPort.Direction.NORTH);
		memWrite = addInput(memWriteId, new Data(1), IOPort.Direction.NORTH, false);
		output = addOutput(outId, new Data(), IOPort.Direction.EAST, true);
	}

	@Override
	public void execute() {
		boolean read = getMemRead().getValue() == 1;
		boolean write = getMemWrite().getValue() == 1;
		
		if(getMemRead().getValue() == 1)
			getOutput().setValue(getData(getAddress().getValue()));
		else
			getOutput().setValue(0);
		
		// Set inputs/outputs relevant if reading/writing
		getWriteData().setRelevant(write);
		getAddress().setRelevant(read || write);
		getOutput().setRelevant(read);
	}

	@Override
	public void executeSynchronous() {
		if(getMemWrite().getValue() == 1)
			setData(getAddress().getValue(), getWriteData().getValue(), true);
	}

	@Override
	public void pushState() {
		states.push(memory.clone());
	}

	@Override
	public void popState() {
		memory = states.pop();
	}

	@Override
	public boolean hasSavedStates() {
		return !states.empty();
	}

	@Override
	public void clearSavedStates() {
		states.clear();
	}

	@Override
	public void resetFirstState() {
		while(hasSavedStates())
			popState();
	}
	
	@Override
	public boolean isWritingState() {
		return getMemWrite().getValue() == 1;
	}
	
	/**
	 * Resets the memory to zeros.
	 */
	public final void reset() {
		for(int i = 0; i < memory.length; i++)
			memory[i] = 0;
		execute();
	}
	
	/**
	 * Returns the value in the specified address.
	 * @param address The address of the memory position.
	 * @return The desired value.
	 */
	public final int getData(int address) {
		return getDataInIndex(getIndexOfAddress(address));
	}
	
	/**
	 * Returns the value in the specified index.
	 * @param index The index of the memory position.
	 * @return The desired value, or 0 if the index is out of bounds.
	 */
	public final int getDataInIndex(int index) {
		return (index >= 0 && index < getMemorySize()) ? memory[index] : 0;
	}
	
	/**
	 * Updates the value in the specified address.
	 * <p>The new value is propagated to the rest of the circuit if it is being read.</p>
	 * @param address The address of the memory position.
	 * @param value The new value.
	 */
	public final void setData(int address, int value) {
		setData(address, value, true);
	}
	
	/**
	 * Updates the value in the specified address.
	 * @param address The address of the memory position.
	 * @param value The new value.
	 * @param propagate Whether the new value is propagated to the rest of the circuit if it is being read.
	 */
	public final void setData(int address, int value, boolean propagate) {
		setDataInIndex(getIndexOfAddress(address), value, propagate);
	}
	
	/**
	 * Updates the value in the specified index.
	 * <p>The new value is propagated to the rest of the circuit if it is being read.</p>
	 * @param index The index of the memory position.
	 * @param value The new value.
	 */
	public final void setDataInIndex(int index, int value) {
		setDataInIndex(index, value, true);
	}
	
	/**
	 * Updates the value in the specified index.
	 * @param index The index of the memory position.
	 * @param value The new value.
	 * @param propagate Whether the new value is propagated to the rest of the circuit if it is being read.
	 */
	public final void setDataInIndex(int index, int value, boolean propagate) {
		if(index >= 0 && index < getMemorySize()) {
			memory[index] = value;
			if(propagate) execute();
		}
	}
	
	/**
	 * Returns the index of the memory position in the specified address.
	 * @param address The address of the memory position.
	 * @return The index of the position, or -1 if out of bounds.
	 */
	public final int getIndexOfAddress(int address) {
		int index = address / (Data.DATA_SIZE / 8); // A lw on an address like 3 would give an error in a CPU with exceptions
		return (index >= 0 && index < getMemorySize()) ? index : -1;
	}
	
	/**
	 * Returns the size of the memory.
	 * @return The size of the memory (number of 32 bits positions).
	 */
	public final int getMemorySize() {
		return memory.length;
	}

	/**
	 * Returns the address input.
	 * @return Address input.
	 */
	public final Input getAddress() {
		return address;
	}
	
	/**
	 * Returns the write data input.
	 * @return Write data input.
	 */
	public final Input getWriteData() {
		return writeData;
	}
	
	/**
	 * Returns the MemRead input.
	 * @return MemRead input.
	 */
	public final Input getMemRead() {
		return memRead;
	}
	
	/**
	 * Returns the MemWrite input.
	 * @return MemWrite input.
	 */
	public final Input getMemWrite() {
		return memWrite;
	}
	
	/**
	 * Returns the output.
	 * @return Output.
	 */
	public final Output getOutput() {
		return output;
	}
}
