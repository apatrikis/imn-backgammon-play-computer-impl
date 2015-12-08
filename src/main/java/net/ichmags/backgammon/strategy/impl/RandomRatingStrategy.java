/*
 * www.ichmags.net - Backgammon
 */
package net.ichmags.backgammon.strategy.impl;

import java.util.Random;

import net.ichmags.backgammon.setup.IBoard;
import net.ichmags.backgammon.setup.IPlayer;
import net.ichmags.backgammon.strategy.IStrategy;

/**
 * The {@code RandomRatingStrategy} is a implementation of the {@link IStrategy} interface.
 * This is a kind of dummy implementation that allows playing a game against the computer.
 * A evaluation does not take place, instead a random value is returned.
 * 
 * @author Anastasios Patrikis
 */
public class RandomRatingStrategy implements IStrategy {

	/**
	 * The maximum value {@link #evaluate(IPlayer, IBoard, IBoard, net.ichmags.backgammon.setup.IPlayer.PlayStyle)}
	 * can return ({@value #LIMIT}).
	 */
	public static final int LIMIT = 10;

	@Override
	public IPlayer.Level suitableForPlayerLevel() {
		return IPlayer.Level.BEGINNER;
	}

	@Override
	public IPlayer.PlayStyle suitableForPlayStyle() {
		return IPlayer.PlayStyle.OFFENSIVE;
	}

	@Override
	public int getLimit() {
		return LIMIT;
	}

	@Override
	public int evaluate(IPlayer player, IBoard boardBefore, IBoard boardAfter, IPlayer.PlayStyle preferedPlayStyle) {
		return new Random().nextInt(getLimit()) + 1;
	}
}
