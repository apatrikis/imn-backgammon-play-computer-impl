package net.ichmags.backgammon.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.ichmags.backgammon.game.IMoves;
import net.ichmags.backgammon.game.impl.Fevga;
import net.ichmags.backgammon.game.impl.Moves;
import net.ichmags.backgammon.game.impl.Portes;
import net.ichmags.backgammon.notification.INotification;
import net.ichmags.backgammon.notification.INotificationConsumer;
import net.ichmags.backgammon.notification.INotificationEmitter;
import net.ichmags.backgammon.setup.CheckerColor;
import net.ichmags.backgammon.setup.IAvailableDices;
import net.ichmags.backgammon.setup.IBoard;
import net.ichmags.backgammon.setup.IDices;
import net.ichmags.backgammon.setup.IPlayer;
import net.ichmags.backgammon.setup.impl.AvailableDices;
import net.ichmags.backgammon.setup.impl.DiceGenerator;
import net.ichmags.backgammon.setup.impl.Dices;
import net.ichmags.backgammon.setup.impl.DicesChoice;
import net.ichmags.backgammon.setup.impl.Player;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BoardGeneratorTest {

	private IPlayer player1;
	private IPlayer player2;
	
	@Before
	public void setUp() throws Exception {
		player1 = new Player().initialize("Tester 1", IPlayer.ID.ONE, IPlayer.Type.LOCAL, Player.Level.AVERAGE, CheckerColor.WHITE);
		player2 = new Player().initialize("Tester 2", IPlayer.ID.TWO, IPlayer.Type.COMPUTER, Player.Level.AVERAGE, CheckerColor.BLACK);
	}

	@Test
	public void testGenerateBoards() {
		PortesLocal testPortes = new PortesLocal();
		testPortes.initialize(player1, player2, null);
		DiceGenerator.get().load(new int[]{6, 4});
		IDices dices = new Dices().roll();
		
		IBoard before = testPortes.getBoard().clone();
		
		boolean check = testPortes.checkIfAnyMoveIsPossible(player2, dices);
		Assert.assertTrue("A move is possible", check);
		
		DicesChoice dicesList = testPortes.findPlayableDices(player2, dices);
		Assert.assertTrue("Single dices option", dicesList.isSingleOption());
		Assert.assertTrue("All dices are playable", dicesList.getOption1().usedCount() == 0);
		
		// 7 results, where 2 results are identical
		BoardGenerator generator = new BoardGenerator(new NotificationEmitterLocal());
		List<BoardMovesCombination> generatedBoards = generator.generateBoards(
				player2, testPortes, testPortes.getBoard(), dicesList.getOption1() );
		Assert.assertEquals("Generated boards count does not match", 7, generatedBoards.size());
		
		Set<BoardMovesCombination> distinctBoards = new HashSet<>(generatedBoards);
		Assert.assertEquals("Distinct boards count does not match", 5, distinctBoards.size());
		
		Assert.assertEquals("Board before and after is equal", before, testPortes.getBoard());
	}

	@Test
	public void testFindBoardsAscendingDice() {
		PortesLocal testPortes = new PortesLocal();
		testPortes.initialize(player1, player2, null);
		DiceGenerator.get().load(new int[]{4, 6});
		IDices dices = new Dices().roll();
		
		IBoard before = testPortes.getBoard().clone();
		
		boolean check = testPortes.checkIfAnyMoveIsPossible(player2, dices);
		Assert.assertTrue("A move is possible", check);
		
		DicesChoice dicesList = testPortes.findPlayableDices(player2, dices);
		Assert.assertTrue("Single dices option", dicesList.isSingleOption());
		Assert.assertTrue("All dices are playable", dicesList.getOption1().usedCount() == 0);
		
		// 4-6 -> has 4 possible moves
		List<BoardMovesCombination> generatedBoards = new ArrayList<>(5);
		IMoves movesForBoard = new Moves();
		IAvailableDices availableDices = new AvailableDices().initialize(dicesList.getOption1(), false);
		new BoardGenerator(new NotificationEmitterLocal()).findBoardsRecursion(
				player2, testPortes, testPortes.getBoard(), availableDices, generatedBoards, movesForBoard);
		Assert.assertEquals("Generated boards count does not match", 4, generatedBoards.size());
		
		Assert.assertEquals("Board before and after is equal", before, testPortes.getBoard());
	}

	@Test
	public void testFindBoardsDescendingDice() {
		PortesLocal testPortes = new PortesLocal();
		testPortes.initialize(player1, player2, null);
		DiceGenerator.get().load(new int[]{6, 4});
		IDices dices = new Dices().roll();
		
		IBoard before = testPortes.getBoard().clone();
		
		boolean check = testPortes.checkIfAnyMoveIsPossible(player2, dices);
		Assert.assertTrue("A move is possible", check);
		
		DicesChoice dicesList = testPortes.findPlayableDices(player2, dices);
		Assert.assertTrue("Single dices option", dicesList.isSingleOption());
		Assert.assertTrue("All dices are playable", dicesList.getOption1().usedCount() == 0);
		
		// 6-4 -> has 3 possible moves
		List<BoardMovesCombination> generatedBoards = new ArrayList<>(5);
		IMoves movesForBoard = new Moves();
		IAvailableDices availableDices = new AvailableDices().initialize(dicesList.getOption1(), false);
		new BoardGenerator(new NotificationEmitterLocal()).findBoardsRecursion(
				player2, testPortes, testPortes.getBoard(), availableDices, generatedBoards, movesForBoard);
		Assert.assertEquals("Generated boards count does not match", 3, generatedBoards.size());
		
		Assert.assertEquals("Board before and after is equal", before, testPortes.getBoard());
	}
	
	@Test
	public void testFindSingleMandatoryMove() {
		FevgaLocal testFevga = new FevgaLocal();
		testFevga.initialize(player1, player2, null);
		DiceGenerator.get().load(new int[]{1, 2});
		IDices dices = new Dices().roll();
		
		IBoard before = testFevga.getBoard().clone();
		
		boolean check = testFevga.checkIfAnyMoveIsPossible(player2, dices);
		Assert.assertTrue("A move is possible", check);
		
		DicesChoice dicesList = testFevga.findPlayableDices(player2, dices);
		Assert.assertTrue("Single dices option", dicesList.isSingleOption());
		Assert.assertTrue("Not all dices are playable", dicesList.getOption1().usedCount() > 0);

		// exactly 1 result
		BoardGenerator generator = new BoardGenerator(new NotificationEmitterLocal());
		List<BoardMovesCombination> generatedBoards = generator.generateBoards(
				player2, testFevga, testFevga.getBoard(), dicesList.getOption1() );
		Assert.assertEquals("Generated boards count does not match", 1, generatedBoards.size());
		
		Assert.assertEquals("Board before and after is equal", before, testFevga.getBoard());
	}
}

class PortesLocal extends Portes {
	@Override
	protected List<Integer> getCheckerPositionsPlayer1() {
		return Arrays.asList(2, 2, 5, 5, 14, 14, 18, 18);
	};
	
	@Override
	protected List<Integer> getCheckerPositionsPlayer2() {
		return Arrays.asList(3, 3, 5, 14, 14, 19);
	}
	
	public IBoard getBoard() {
		return this.board;
	}
	
	@Override
	public boolean checkIfAnyMoveIsPossible(IPlayer currentPlayer, IDices dices) {
		return super.checkIfAnyMoveIsPossible(currentPlayer, dices);
	}
	
	@Override
	public DicesChoice findPlayableDices(IPlayer currentPlayer, IDices dices) {
		return super.findPlayableDices(currentPlayer, dices);
	}
}

class FevgaLocal extends Fevga {
	@Override
	protected List<Integer> getCheckerPositionsPlayer1() {
		return Arrays.asList(1, 1, 3, 4, 5, 7, 7, 8, 9, 14, 15, 16, 17, 23, 24);
	};
	
	@Override
	protected List<Integer> getCheckerPositionsPlayer2() {
		return Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 14, 14, 14, 14, 23);
	}
	
	public IBoard getBoard() {
		return this.board;
	}
	
	@Override
	public boolean checkIfAnyMoveIsPossible(IPlayer currentPlayer, IDices dices) {
		return super.checkIfAnyMoveIsPossible(currentPlayer, dices);
	}
	
	@Override
	public DicesChoice findPlayableDices(IPlayer currentPlayer, IDices dices) {
		return super.findPlayableDices(currentPlayer, dices);
	}
}

class NotificationEmitterLocal implements INotificationEmitter {

	@Override
	public INotificationEmitter addConsumer(INotificationConsumer consumer) {
		return this;
	}

	@Override
	public INotificationEmitter removeConsumer(INotificationConsumer consumer) {
		return this;
	}

	@Override
	public INotificationEmitter emitNotification(INotification notification) {
		return this;
	}
}
