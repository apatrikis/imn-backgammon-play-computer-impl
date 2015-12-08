/*
 * www.ichmags.net - Backgammon
 */
package net.ichmags.backgammon.strategy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.ichmags.backgammon.reflection.ClassListByTypeFinder;
import net.ichmags.backgammon.setup.IPlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code StrategyManager} keeps track of all {@link IStrategy} classes.
 * Each {@link IStrategy} has a {@link IPlayer.Level} assigned, which helps to find
 * a suitable {@link IStrategy} for the computer {@link IPlayer}.
 * 
 * @author Anastasios Patrikis
 */
public class StrategyManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(StrategyManager.class);

	private static final Set<IStrategy> STRATEGIES;
	
	static {
		STRATEGIES = new HashSet<>();
		loadStrategies();
	}
	
	public StrategyManager() {
		// noting to do
	}
	
	/**
	 * Get a List of {@link IStrategy} instances that are suitable for the computer {@link IPlayer}.
	 * 
	 * @param playerLevel The {@link IPlayer.Level} for which the {@link IStrategy} has to match.
	 * The {@link IPlayer.Level} is a maximum, so {@link IStrategy}'s below the requested {@link IPlayer.Level}
	 * will be returned.
	 * @return The {@link List} of matching {@link IStrategy} insances.
	 */
	public Set<IStrategy> getStrategies(IPlayer.Level playerLevel) {
		Set<IStrategy> matches = new HashSet<>();
		
		for(IStrategy strategy : STRATEGIES) {
			if(strategy.suitableForPlayerLevel().ordinal() <= playerLevel.ordinal()) {
				matches.add(strategy);
			}
		}
		
		return matches;
	}
	
	/**
	 * Load all {@link IStrategy} implementations which can be found on the class path.
	 * Narrow down to packages starting with {@code net.ichmags.backgammon.strategy}.
	 * 
	 * @see <a href="http://stackoverflow.com/questions/3222638/get-all-of-the-classes-in-the-classpath">stackoverflow: scanning the class path for classes</a>
	 * @see <a href="http://stackoverflow.com/questions/259140/scanning-java-annotations-at-runtime">Another stackoverflow article: scanning the class path for classes with libraries</a>
	 */
	private static void loadStrategies() {
		try {
			List<Class<IStrategy>> strategies = loadStrategyClasses();
			for(Class<IStrategy> strategy : strategies) {
				STRATEGIES.add(strategy.newInstance());
			}
		} catch (Exception e) {
			throw new RuntimeException("Cannot create IStategy instance", e);
		}
		
		LOG.info("Loaded {} strategies: {}", STRATEGIES.size(), STRATEGIES.toArray());
	}

	
	/**
	 * Create a instance dynamically, as an implementing class is not
	 * in this package but somewhere on the {@code classpath}.
	 * 
	 * @return a new {@link List} of {@link IStrategy} classes.
	 */
	private static List<Class<IStrategy>> loadStrategyClasses() {
		return new ClassListByTypeFinder<IStrategy>(IStrategy.class, true, "net\\.ichmags\\.backgammon\\..*").get();		
	}
}
