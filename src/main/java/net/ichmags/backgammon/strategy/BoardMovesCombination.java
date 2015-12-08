/*
 * www.ichmags.net - Backgammon
 */
package net.ichmags.backgammon.strategy;

import net.ichmags.backgammon.game.IMove;
import net.ichmags.backgammon.game.IMoves;
import net.ichmags.backgammon.setup.IBoard;
import net.ichmags.backgammon.setup.IDices;
import net.ichmags.backgammon.setup.IPlayer;

/**
 * The {@code BoardMovesCombination} class is a container that is used during automated {@link IMove}
 * calculation for the computer {@link IPlayer}.
 * <ul>
 * <li>the stored {@link IBoard} is the result of the calculation</li>
 * <li>the {@link IMove}s were used to create the {@link IBoard} constellation</li>
 * </ul>
 * The {@link IMove}s can be used to
 * <ul>
 * <li>undo the {@link IMove}s on the {@link IBoard} to restore the original constellation</li>
 * <li>replay the moves on another {@link IBoard}</li>
 * </ul>
 * 
 * {@link #equals(Object)} returns {@code true} when the {@link IBoard} is equal - this means
 * the {@link IMoves} have no relevance.
 * 
 * @author Anastasios Patrikis
 */
public class BoardMovesCombination {

	private IBoard board;
	private IDices dices;
	private IMoves moves;
	
	/**
	 * Constructor.
	 * 
	 * @param board the {@link IBoard} which is the result of applied {@link IMove}s on another {@link IBoard}.
	 * @param moves all applied {@link IMove}s, in the order they were executed.
	 */
	public BoardMovesCombination(IBoard board, IMoves moves){
		this.board = board;
		this.moves = moves;
	}

	/**
	 * Get the calculated {@link IBoard}.
	 * 
	 * @return the {@link IBoard} after applying the {@link IMove}s.
	 */
	public IBoard getBoard() {
		return board;
	}
	
	/**
	 * Get the {@link IDices} used for moving.  
	 * @return the {@link IDices} that were applied on the {@link IBoard}.
	 */
	public IDices getDices() {
		return dices;
	}
	
	/**
	 * Set the {@link IDices} used for moving.
	 * @param dices the {@link IDices} that were applied on the {@link IBoard}.
	 */
	public void setDices(IDices dices) {
		this.dices = dices;
	}
	
	/**
	 * Get the applied {@link IMove}s that created the {@link IBoard}.
	 * @return the {@link IMove}s that were applied on the {@link IBoard}.
	 */
	public IMoves getMoves() {
		return moves;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((board == null) ? 0 : board.hashCode());
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
		BoardMovesCombination other = (BoardMovesCombination) obj;
		if (board == null) {
			if (other.board != null)
				return false;
		} else if (!board.equals(other.board))
			return false;
		return true;
	}	
}
