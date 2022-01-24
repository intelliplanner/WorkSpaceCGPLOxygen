package InterviewQuestions.CricketMatch;

import java.util.List;

public class Match {
	String matchType;
	int matchScore;
	List<PlayerDetails> pd;
	public Match(String matchType,	int matchScore,List<PlayerDetails> pd){
		this.matchScore=matchScore;
		this.matchType=matchType;
		this.pd=pd;
	}
}
