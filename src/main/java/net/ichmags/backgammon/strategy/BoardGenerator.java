/*
 * www.ichmags.net - Backgammon
 */
package net.ichmags.backgammon.strategy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.ichmags.backgammon.exception.InvalidMoveException;
import net.ichmags.backgammon.game.IGame;
import net.ichmags.backgammon.game.IMove;
import net.ichmags.backgammon.game.IMoves;
import net.ichmags.backgammon.game.impl.Moves;
import net.ichmags.backgammon.l10n.LocalizationManager;
import net.ichmags.backgammon.notification.INotification;
import net.ichmags.backgammon.notification.INotification.Level;
import net.ichmags.backgammon.notification.INotificationEmitter;
import net.ichmags.backgammon.notification.pojo.StringNotification;
import net.ichmags.backgammon.reflection.ClassByTypeFinder;
import net.ichmags.backgammon.setup.IAvailableDices;
import net.ichmags.backgammon.setup.IBoard;
import net.ichmags.backgammon.setup.IChecker;
import net.ichmags.backgammon.setup.IDice;
import net.ichmags.backgammon.setup.IDices;
import net.ichmags.backgammon.setup.IPlayer;
import net.ichmags.backgammon.setup.IPosition;
import net.ichmags.backgammon.setup.IPositions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code BoardGenerator} is used by a computer opponent to generate a {@link Set} of all
 * possible moves, which will be analyzed by an {@link IStrategy}. 
 *  
 * @author Anastasios Patrikis
 */
public class BoardGenerator {
	
	private static Logger LOG = LoggerFactory.getLogger(BoardGenerator.class);
	
	private INotificationEmitter notificationEmitter;
	
	/**
	 * Constructor.
	 * 
	 * @param notificationEmitter the {@link INotificationEmitter} to use for sending
	 * {@link INotification} messages.
	 */
	public BoardGenerator(INotificationEmitter notificationEmitter) {
		this.notificationEmitter = notificationEmitter;
	}
	
	/**
	 * Generates a unique {@link Set} of {@link IBoard} instances, which represent all possible outcomes
	 * for the passed parameters.
	 *  
	 * @param player the {@link IPlayer}, which will be a computer.
	 * @param game the {@link IGame} to play on the {@code Board}.
	 * @param board the original {@link IBoard}, for which all possible move will be calculated.  
	 * @param dices the {@link IDices} to use for moving the {@link IChecker}.
	 * @return The {@link Set} of all possible outcomes when the {@link IDice} combinations are applied
	 * to the original {@link IBoard}.
	 */
	public List<BoardMovesCombination> generateBoards(IPlayer player, IGame game, IBoard board, IDices dices) {
		IBoard testBoard = board.clone();
		
		IAvailableDices playableDices = getAvailableDicesInstance().initialize(dices, false);
		
		List<BoardMovesCombination> generatedBoards = new ArrayList<>(50);
		IMoves movesForBoard = new Moves();
		
		// TODO: possible performance optimization
		// This currently works fine without explicit checking the mandatory moves: they are
		// checked implicitly by the move() method.
		
		if(dices.isDoubleDices()) {
			findBoardsRecursion(player, game, testBoard, playableDices, generatedBoards, movesForBoard);
		} else if (dices.usedCount() > 0) {
			// optimized: since the dice check returns List<IDice> the second pass is only necessary when all dices can be played
			findBoardsRecursion(player, game, testBoard, playableDices, generatedBoards, movesForBoard);
		} else {
			findBoardsRecursion(player, game, testBoard, playableDices, generatedBoards, movesForBoard);
			
			playableDices = getAvailableDicesInstance().initialize(dices, true);
			findBoardsRecursion(player, game, testBoard, playableDices, generatedBoards, movesForBoard);
		}
		
		notificationEmitter.emitNotification(new StringNotification(Level.INFO,
				LocalizationManager.get().get("boardgenerator.number_of_boards", generatedBoards.size()) ));
		
		generatedBoards.forEach(item -> item.setDices(dices));
		return generatedBoards;
	}
	
	/**
	 * Recursion for calculating the possible moves.
	 * 
	 * @param player the {@link IPlayer}, which will be a computer.
	 * @param game the {@link IGame} to play on the {@code Board}.
	 * @param board the {@link IBoard}, for which all possible move will be calculated.
	 * @param dices the {@link List} of {@link IDice} values to use for moving the {@link IChecker}.
	 * @param generatedBoards the {@link Set} of found {@link BoardMovesCombination} items; this is the result
	 * of the recursion.
	 * @param movesForBoard the {@link List} of the currently played {@link IMove}s; this is needed during the
	 * recursion for the creation of {@link BoardMovesCombination} items.
	 */
	protected void findBoardsRecursion(IPlayer player, IGame game, IBoard board, IAvailableDices dices, List<BoardMovesCombination> generatedBoards, IMoves movesForBoard) {
		IPositions positions = board.createPlayerView(player);
		
		IDice dice = dices.nextElement();
		
		for(Iterator<IPosition> positionIterator = positions.get().iterator(); positionIterator.hasNext(); ) {
			IPosition position = positionIterator.next();
			
			if((position.hasCheckers() == false) || (position.readTopChecker().getOwner().equals(player.getID()) == false)) {
				continue; // minimum condition not satisfied
			}
			int currentPosition = position.getIndexIn(positions);
			
			IMove move = game.moveChecker(player, board, currentPosition, dice);
			if(move.isSuccess()) {
				movesForBoard.addLatest(move);
				if( ! dices.hasMoreElements()) {
					generatedBoards.add(new BoardMovesCombination(board.clone(), movesForBoard.clone()));
					notificationEmitter.emitNotification(new StringNotification(Level.TRACE,
							LocalizationManager.get().get("boardgenerator.found_board") ));
				} else {
					// recursion: play the next dice
					findBoardsRecursion(player, game, board, dices, generatedBoards, movesForBoard);
				}
				// undo move, so the next position can be checked with the same dice
				try {
					game.undoMoveCheker(player, board, move.clone());
					movesForBoard.removeLatest();
				} catch (InvalidMoveException e) {
					LOG.error("Error playing undo move", e);
				}
			}
		}
		
		dices.reactivateElement();
	}
	
	/**
	 * Create a instance dynamically, as an implementing class is not
	 * in this package but somewhere on the {@code classpath}.
	 * 
	 * @return a new {@link IAvailableDices} instance.
	 */
	private IAvailableDices getAvailableDicesInstance() {
		return new ClassByTypeFinder<IAvailableDices>(IAvailableDices.class, true, "net\\.ichmags\\.backgammon\\..*").getInstance();
	}
}
