package InterviewQuestions.CricketMatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainClass {
	public static void main(String s[]) {
		List<Match> matchList = new ArrayList<Match>();

		List<PlayerDetails> pl1 = new ArrayList<PlayerDetails>();
		pl1.add(new PlayerDetails("Sachin", 101));
		pl1.add(new PlayerDetails("VIRAT", 50));
		pl1.add(new PlayerDetails("DHONI", 70));
		pl1.add(new PlayerDetails("RAHANE", 20));
		pl1.add(new PlayerDetails("KL RAHUL", 40));

		Match m1 = new Match("OneDay", 200, pl1);

		// PlayerDetails l2=new PlayerDetails("Sachin",101);
		List<PlayerDetails> pl2 = new ArrayList<PlayerDetails>();
		pl2.add(new PlayerDetails("Sachin", 121));
		pl2.add(new PlayerDetails("VIRAT", 20));
		pl2.add(new PlayerDetails("DHONI", 73));
		pl2.add(new PlayerDetails("RAHANE", 30));
		pl2.add(new PlayerDetails("KL RAHUL", 28));

		Match m2 = new Match("Test", 200, pl2);

		// PlayerDetails l3=new PlayerDetails("Sachin",101);
		List<PlayerDetails> pl3 = new ArrayList<PlayerDetails>();
		pl3.add(new PlayerDetails("Sachin", 11));
		pl3.add(new PlayerDetails("VIRAT", 44));
		pl3.add(new PlayerDetails("DHONI", 33));
		pl3.add(new PlayerDetails("RAHANE", 43));
		pl3.add(new PlayerDetails("KL RAHUL", 40));

		Match m3 = new Match("Twenty-Twenty", 200, pl3);

		matchList.add(m1);
		matchList.add(m2);
		matchList.add(m3);

		HashMap<String, Integer> nameScoreList = new HashMap<>();

		for (Match m : matchList) {
			List<PlayerDetails> p = m.pd;
			String matchType = m.matchType;
			int matchScore = 0;

			System.out.println(matchType + " Match");

			for (PlayerDetails pd : p) {
				if (!nameScoreList.containsKey(pd.playerName)) {
					nameScoreList.put(pd.playerName, pd.playerScore);
				} else {
					int newScore = nameScoreList.get(pd.playerName) + pd.playerScore;
					nameScoreList.put(pd.playerName, newScore);
				}
				matchScore += pd.playerScore;
				System.out.println("Player Name: " + pd.playerName + ", Player Score: " + pd.playerScore);
			}

			System.out.println("Match Total Score:" + matchScore + "\n");
		}
		System.out.println("Total Score of Player: ");
		for (Map.Entry<String, Integer> m : nameScoreList.entrySet()) {
			System.out.println(m.getKey() + ", " + m.getValue());
		}
	}
}
