package ostb.gameapi;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import anticheat.util.DelayedTask;
import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.game.GameEndingEvent;
import ostb.customevents.game.GameStartEvent;
import ostb.customevents.game.GameStartingEvent;
import ostb.customevents.game.GameVotingEvent;
import ostb.customevents.game.GameWaitingEvent;
import ostb.customevents.game.GameWinEvent;
import ostb.customevents.game.PostGameStartEvent;
import ostb.customevents.game.PostGameStartingEvent;
import ostb.gameapi.competitive.StatDisplayer;
import ostb.player.account.AccountHandler.Ranks;
import ostb.player.scoreboard.SidebarScoreboardUtil;
import ostb.server.CommandBase;
import ostb.server.ServerLogger;
import ostb.server.util.CountDownUtil;
import ostb.server.util.FileHandler;

public abstract class MiniGame extends ProPlugin {
	private int requiredPlayers = 4;
	private int votingCounter = 60;
	private int startingCounter = 30;
	private int endingCounter = 15;
	private int killCoins = 0;
	private int winCoins = 0;
	private boolean autoJoin = true;
	private boolean canJoinWhileStarting = false;
	private boolean useSpectatorChatChannel = true;
	private boolean playersHaveOneLife = true;
	private boolean restartWithOnePlayerLeft = true;
	private boolean useCoinBoosters = true;
	private boolean end = true;
	private World lobby = null;
	private World map = null;
	public enum GameStates {
		WAITING(new GameWaitingEvent(), "Waiting"),
		VOTING(new GameVotingEvent(), "Voting"),
		STARTING(new GameStartingEvent(), "Starting"),
		STARTED(new GameStartEvent(), "In Game"),
		ENDING(new GameEndingEvent(), "Ending");
		
		private Event event = null;
		private String display = null;
		
		private GameStates(Event event, String display) {
			this.event = event;
			this.display = display;
		}
		
		public Event getEvent() {
			return this.event;
		}
		
		public String getDisplay() {
			return display;
		}
		
		public void enable() {
			Bukkit.getPluginManager().callEvent(getEvent());
		}
	}
	private GameStates gameState = GameStates.WAITING;
	private GameStates oldState = gameState;
	private TeamHandler teamHandler = null;
	
	public MiniGame(String name) {
		super(name);
		OSTB.setMiniGame(this);
		addGroup("mini-game");
		setLobby(Bukkit.getWorlds().get(0));
		lobby.setTime(12250);
		for(Entity entity : lobby.getEntities()) {
			if(entity instanceof ArmorStand) {
				entity.remove();
			}
		}
		new MiniGameEvents();
		new SpectatorHandler();
		new PerformanceLogger();
		new PostGameStartEvent(true);
		new PostGameStartingEvent(true);
		new LeaveItem();
		new MapRating();
		new VotingHandler();
		new EndingLobby();
		new CoinBoosters();
		new AutoJoinHandler();
		new TimeOfDay();
		new KillLogger();
		teamHandler = new TeamHandler();
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				new ServerLogger();
				new StatDisplayer();
			}
		});
		setGameState(GameStates.WAITING);
		new CommandBase("startGame", 0, 1) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				setGameState(GameStates.VOTING);
				if(arguments.length == 1) {
					try {
						setCounter(Integer.valueOf(arguments[0]));
						return true;
					} catch(NumberFormatException e) {
						return false;
					}
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("setTimer", 1) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				try {
					setCounter(Integer.valueOf(arguments[0]));
					return true;
				} catch(NumberFormatException e) {
					return false;
				}
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("win", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(OSTB.getMiniGame() != null) {
					Bukkit.getPluginManager().callEvent(new GameWinEvent(player));
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		setToDefaultSidebar();
	}
	
	@Override
	public void disable() {
		super.disable();
		String container = Bukkit.getWorldContainer().getPath();
		for(World world : Bukkit.getWorlds()) {
			Bukkit.unloadWorld(world, false);
			if(world.getName().equals("lobby") && !new File(container + "/../resources/maps/lobby").exists()) {
				continue;
			}
			FileHandler.delete(new File(container + "/" + world.getName()));
		}
		if(!new File(container + "/lobby/uid.dat").exists()) {
			FileHandler.copyFolder(new File(container + "/../resources/maps/lobby"), new File(container + "/lobby"));
		}
	}
	
	@Override
	public void resetFlags() {
		super.resetFlags();
		setRequiredPlayers(4);
		setVotingCounter(60);
		setStartingCounter(30);
		setEndingCounter(15);
		setAutoJoin(true);
		setCanJoinWhileStarting(true);
		setUseSpectatorChatChannel(true);
		setPlayersHaveOneLife(true);
		setRestartWithOnePlayerLeft(true);
		setUseCoinBoosters(true);
		setEnd(true);
		setCounter(0);
	}
	
	public int getVotingCounter() {
		return this.votingCounter;
	}
	
	public void setVotingCounter(int votingCounter) {
		this.votingCounter = votingCounter;
		if(getGameState() == GameStates.VOTING) {
			setCounter(votingCounter);
		}
	}
	
	public int getStartingCounter() {
		return this.startingCounter;
	}
	
	public void setStartingCounter(int startingCounter) {
		this.startingCounter = startingCounter;
		if(getGameState() == GameStates.STARTING) {
			setCounter(startingCounter);
		}
	}
	
	public int getEndingCounter() {
		return this.endingCounter;
	}
	
	public void setEndingCounter(int endingCounter) {
		this.endingCounter = endingCounter;
	}
	
	public int getRequiredPlayers() {
		return this.requiredPlayers;
	}
	
	public void setRequiredPlayers(int requiredPlayers) {
		this.requiredPlayers = requiredPlayers;
	}
	
	public int getKillCoins() {
		return this.killCoins;
	}
	
	public void setKillCoins(int killCoins) {
		this.killCoins = killCoins;
	}
	
	public int getWinCoins() {
		return this.winCoins;
	}
	
	public void setWinCoins(int winCoins) {
		this.winCoins = winCoins;
	}
	
	public boolean getAutoJoin() {
		return this.autoJoin;
	}
	
	public void setAutoJoin(boolean autoJoin) {
		this.autoJoin = autoJoin;
	}
	
	public boolean getCanJoinWhileStarting() {
		return this.canJoinWhileStarting;
	}
	
	public void setCanJoinWhileStarting(boolean canJoinWhileStarting) {
		this.canJoinWhileStarting = canJoinWhileStarting;
	}
	
	public boolean getJoiningPreGame() {
		GameStates gameState = getGameState();
		return gameState == GameStates.WAITING || gameState == GameStates.VOTING || (gameState == GameStates.STARTING && getCanJoinWhileStarting());
	}
	
	public boolean getUseSpectatorChatChannel() {
		return this.useSpectatorChatChannel;
	}
	
	public void setUseSpectatorChatChannel(boolean useSpectatorChatChannel) {
		this.useSpectatorChatChannel = useSpectatorChatChannel;
	}
	
	public boolean getPlayersHaveOneLife() {
		return this.playersHaveOneLife;
	}
	
	public void setPlayersHaveOneLife(boolean oneLife) {
		this.playersHaveOneLife = oneLife;
	}
	
	public boolean getRestartWithOnePlayerLeft() {
		return this.restartWithOnePlayerLeft;
	}
	
	public void setRestartWithOnePlayerLeft(boolean restartWithOnePlayerLeft) {
		this.restartWithOnePlayerLeft = restartWithOnePlayerLeft;
	}
	
	public boolean getUseCoinBoosters() {
		return this.useCoinBoosters;
	}
	
	public void setUseCoinBoosters(boolean useCoinBoosters) {
		this.useCoinBoosters = useCoinBoosters;
	}
	
	public boolean getEnd() {
		return this.end;
	}
	
	public void setEnd(boolean end) {
		this.end = end;
	}
	
	public World getLobby() {
		return this.lobby;
	}
	
	public void setLobby(World lobby) {
		this.lobby = lobby;
	}
	
	public World getMap() {
		return this.map;
	}
	
	public void setMap(World map) {
		this.map = map;
	}
	
	public GameStates getGameState() {
		return this.gameState;
	}
	
	public void setGameState(GameStates gameState) {
		if(gameState == GameStates.ENDING && !getEnd()) {
			return;
		}
		this.gameState = gameState;
		getGameState().enable();
	}
	
	public GameStates getOldGameState() {
		return this.oldState;
	}
	
	public void setOldGameState(GameStates oldState) {
		this.oldState = oldState;
	}
	
	public TeamHandler getTeamHandler() {
		return teamHandler;
	}
	
	public void setToDefaultSidebar() {
		OSTB.setSidebar(new SidebarScoreboardUtil(" &a&l" + getDisplayName() + " ") {
			@Override
			public void update() {
				if(ServerLogger.updatePlayerCount()) {
					removeScore(8);
					removeScore(5);
				}
				if(getGameState() != GameStates.WAITING) {
					removeScore(5);
				}
				if(getGameState() != getOldGameState()) {
					setOldGameState(getGameState());
					removeScore(6);
				}
				int size = ProPlugin.getPlayers().size();
				setText(new String [] {
					" ",
					"&e&lPlaying",
					"&b" + size + " &7/&b " + OSTB.getMaxPlayers(),
					"  ",
					"&e&l" + getGameState().getDisplay() + (getGameState() == GameStates.STARTED ? "" : " Stage"),
					getGameState() == GameStates.WAITING ? "&b" + size + " &7/&b " + getRequiredPlayers() : CountDownUtil.getCounterAsString(getCounter(), ChatColor.AQUA),
					"   ",
					"&a&lOutsideTheBlock.org",
					"&e&lServer &b&l" + OSTB.getPlugin().getServer().toUpperCase() + OSTB.getServerName().replaceAll("[^\\d.]", ""),
					"    "
				});
				super.update();
			}
		});
	}
}
