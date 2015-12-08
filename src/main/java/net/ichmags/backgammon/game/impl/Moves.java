/*
 * www.ichmags.net - Backgammon
 */
package net.ichmags.backgammon.game.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.ichmags.backgammon.game.IMove;
import net.ichmags.backgammon.game.IMoves;

/**
 * Implementation of the {@link IMoves} {@code interface}.
 * 
 * @author Anastasios Patrikis
 */
public class Moves implements IMoves {

	private List<IMove> moves;
	private int cloneGeneration;
	
	/**
	 * Default constructor.
	 */
	public Moves() {
		moves = new ArrayList<>(4);
		cloneGeneration = 0;
	}
	
	@Override
	public void addLatest(IMove latesMove) {
		moves.add(latesMove);
	}
	
	@Override
	public IMove removeLatest() {
		return moves.remove(moves.size() - 1);
	}
	
	@Override
	public List<IMove> get() {
		return Collections.unmodifiableList(moves);
	}
	
	@Override
	public boolean isClone() {
		return cloneGeneration > 0;
	}
	
	@Override
	public Moves clone() {
		Moves clone = new Moves();
		clone.moves = new ArrayList<>(this.moves);
		clone.cloneGeneration = this.cloneGeneration + 1;
		
		return clone;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((moves == null) ? 0 : moves.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Moves other = (Moves) obj;
		if (moves == null) {
			if (other.moves != null)
				return false;
		} else if (!moves.equals(other.moves))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return ("Moves: " + moves);
	}
}
