/*
 * www.ichmags.net - Backgammon
 */
package net.ichmags.backgammon.game.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.ichmags.backgammon.exception.ExitException;
import net.ichmags.backgammon.game.ExitLevel;
import net.ichmags.backgammon.game.IGame;
import net.ichmags.backgammon.game.IMove;
import net.ichmags.backgammon.game.IPlay;
import net.ichmags.backgammon.l10n.LocalizationManager;
import net.ichmags.backgammon.notification.INotification.Level;
import net.ichmags.backgammon.notification.INotificationEmitter;
import net.ichmags.backgammon.notification.pojo.BoardChangedNotification;
import net.ichmags.backgammon.notification.pojo.DicesChangedNotification;
import net.ichmags.backgammon.notification.pojo.StringNotification;
import net.ichmags.backgammon.setup.IBoard;
import net.ichmags.backgammon.setup.IDices;
import net.ichmags.backgammon.setup.IDicesChoice;
import net.ichmags.backgammon.setup.IPlayer;
import net.ichmags.backgammon.strategy.BoardGenerator;
import net.ichmags.backgammon.strategy.BoardMovesCombination;
import net.ichmags.backgammon.strategy.IStrategy;
import net.ichmags.backgammon.strategy.StrategyManager;

/**
 * Implementation of the {@link IPlay} {@code interface}.
 * This is an implementation for a <i>computer player</i>, aka a <i>KI</i>.
 * 
 * @author Anastasios Patrikis
 */
public class ComputerPlayer implements IPlay {
	
	@Override
	public boolean play(IPlayer player, IGame game, IBoard board, IDicesChoice dicesChoice, INotificationEmitter notificationEmitter) throws ExitException {
		SortedMap<Integer, List<BoardMovesCombination>> rankings = new TreeMap<>();
		
		List<IDices> dicesList = dicesChoice.getAsList();
		
		// step 1 : calculate moves
		List<BoardMovesCombination> possibleBoards = new ArrayList<BoardMovesCombination>(50);
		for(int pos = 0; pos < dicesList.size(); pos++) {
			possibleBoards.addAll(new BoardGenerator(notificationEmitter).generateBoards(player, game, board, dicesList.get(0)));
		}
		
		// if the game does not support kicking a checker (which has influence on the strategy to choose)
		// the equal boards can be removed -> we use the BoardMovesCombination#equals to do this
		if(!game.hasPosition0()) {
			Set<BoardMovesCombination> uniqueBoards = new HashSet<BoardMovesCombination>(possibleBoards);
			possibleBoards.clear(); // NOT #retainAll: BoardMovesCombination#equals does not remove identical boards
			possibleBoards.addAll(uniqueBoards);
			notificationEmitter.emitNotification(new StringNotification(Level.INFO,
					LocalizationManager.get().get("boardgenerator.number_of_boards_for_game", game.getName(), possibleBoards.size()) ));
		}
		
		Set<IStrategy> strategies = new StrategyManager().getStrategies(player.getLevel());
		for(IStrategy strategy : strategies) {
			for(BoardMovesCombination possibleBoard : possibleBoards) {
				int ranking = strategy.evaluate(player, board, possibleBoard.getBoard(), player.getPlayStyle());
				
				List<BoardMovesCombination> equalRanked = rankings.get(ranking);
				if(!rankings.containsKey(ranking)) {
					equalRanked = new ArrayList<BoardMovesCombination>(15);
					rankings.put(ranking, equalRanked);
				}
				equalRanked.add(possibleBoard);
			}
		}
		
		Integer highestRanking = rankings.lastKey();
		List<BoardMovesCombination> equalRanked = rankings.get(highestRanking);
		int equalRankedCount = equalRanked.size();
		
		notificationEmitter.emitNotification(new StringNotification(Level.INFO,
				LocalizationManager.get().get("game.highest_raking", highestRanking) ));
		notificationEmitter.emitNotification(new StringNotification(Level.INFO,
				LocalizationManager.get().get("game.highest_raking_count", equalRankedCount) ));
		
		// step 2 : get best moves
		BoardMovesCombination selection;
		if(equalRankedCount == 1) {
			selection = equalRanked.get(0);
		} else {
			// as some results seem to be equally good, return an random element
			int randomChoice = new Random().nextInt(equalRankedCount);
			selection = equalRanked.get(randomChoice);
		}
		
		// step 3 : play best moves
		for(IMove move : selection.getMoves().get()) {
			notificationEmitter.emitNotification(new DicesChangedNotification(Level.INFO, selection.getDices()));
			
			IMove controlMoveSuccess = game.moveChecker(player, board, move.getFromPosition(), move.getMoveDistance());
			if(!controlMoveSuccess.equals(move)) {
				notificationEmitter.emitNotification(new StringNotification(Level.INFO,
						LocalizationManager.get().get("game.cannot_replay_calculated_moves") ));
				throw new ExitException(ExitLevel.GAME);
			}
			
			notificationEmitter.emitNotification(new BoardChangedNotification(Level.INFO, player, game, board));
		}
		
		return game.isAllCheckersCollected(player);
	}
}
