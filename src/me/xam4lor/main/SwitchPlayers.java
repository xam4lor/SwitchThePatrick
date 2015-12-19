package me.xam4lor.main;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class SwitchPlayers {
	/*Structure du tableau des switchs:
	 * premier tableau, numéro des switchs. ex: [1,2,3]
	 * second tableau, episode et temps des switchs. ex: [2,10],[3,15],[4,19]
	 */
	
	public MainClass m = null;
	@SuppressWarnings("unused")
	private int switchs[][];
	private Logger log = Logger.getLogger("Minecraft");
	
	public SwitchPlayers() {
		//initialisation des switchs
		int switchs[][] = SwitchList();
		if(switchs != null) {
			this.switchs = switchs;
			this.log.info("Configuration des switchs terminée avec succès !");
		}
		else {
			this.log.info("Erreur dans la configuration des switchs.");
		}
	}
	
	public SwitchPlayers(Integer episode, Integer minutesLeft, Integer secondsLeft) {
		//test d'un switch
	}
	
	private int[][] SwitchList() {
		try {
			VoidTbl tbl = new VoidTbl();
			int switchNumber = m.getConfig().getInt("switch.number");
			int switchs[][] = tbl.getTableau();
			
			for(int i = 0; i < switchNumber; i++) {
				switchs[i][0] = m.getConfig().getInt("switch." + (i + 1) + ".episode");
				switchs[i][1] = m.getConfig().getInt("switch." + (i + 1) + ".time");
			}
			return switchs;
		}
		catch(NullPointerException e) {
			e.printStackTrace();
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "Erreur lors du lancement de la partie, le nombre de switchs est trop élevé. Regarder la console pour plus de détails.");
			return null;
		}
	}
}
