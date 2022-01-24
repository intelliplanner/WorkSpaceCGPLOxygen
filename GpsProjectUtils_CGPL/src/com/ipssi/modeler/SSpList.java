package com.ipssi.modeler;

import java.sql.Connection;

import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Pair;
import com.ipssi.modeler.SGpsData;

public class SSpList extends FastList<SGpsData> {
	
	public  FastList<SGpsData> createTempSpecialList() {
		FastList<SGpsData> list = new FastList<SGpsData>();
		for (int i = 0; i < this.size(); i++) {
			list.add(new SGpsData(this.get(i)));
		}
		return list;
	}
	
	public void update(SGpsData gpsData, Pair<Integer, Integer> specialListImpactBegInclEndExcl) throws Exception { // returns the
		//copied from rule processor
		SSpList specialList = this;
		Pair<Integer, Boolean> addAt = specialList.indexOf(gpsData);
		boolean amTrue = gpsData.isTrue();
		int firstIndexOfChange = 0;
		int lastIndexOfChangeExcl = 0;
		boolean toSetSpecialListDirty = false;
		if (!addAt.second.booleanValue()) { // only time no change in speciallist if it can be merged with left and there is a non-empty next
			SGpsData prevGpsData = specialList.get(addAt.first);
			SGpsData nextGpsData = specialList.get(addAt.first + 1);

			boolean prevTrue = prevGpsData == null ? !amTrue : prevGpsData.isTrue();
			if (prevGpsData != null && nextGpsData != null && prevTrue == amTrue) { // do nothing
				prevGpsData.merge(gpsData, true);
				firstIndexOfChange = 0;
				lastIndexOfChangeExcl = 0;
			} else { // insert and then start merging the right hand part
				specialList.add(gpsData);
				toSetSpecialListDirty = true;
				SGpsData toMergeWith = prevGpsData;
				int startIndex = addAt.first;
				// prev is null. next is null then start from curr (case 1)
				// prev is null. next is not null then start from curr (case 2)
				// prev is not null, next is not null then, curr will have diff value than prev and we start from curr (otherwise we would have added in the previous one (case 3)
				// prev is not null, next is null ..prev and prevPrev may be same ... in which case we will have to start from prevPrev (case 4)
				if (prevGpsData != null && nextGpsData != null) { // case 3 start merging from curr //prevTrue != amTrue
					toMergeWith = gpsData;
					startIndex = addAt.first + 1;
					prevTrue = amTrue;
				} else if (prevGpsData == null) { // case 1 and case 2 start merging from me
					toMergeWith = gpsData;
					startIndex = addAt.first + 1;
					prevTrue = amTrue;
				} else { // case 4 check if prev of prev is also same as prev
					SGpsData prevPrev = specialList.get(addAt.first - 1);
					if (prevPrev != null && prevPrev.isTrue() == prevTrue) {
						toMergeWith = prevPrev;
						startIndex = addAt.first - 1;
					}
				}

				firstIndexOfChange = startIndex;
				lastIndexOfChangeExcl = firstIndexOfChange + (addAt.first + 1 - startIndex) + 1;
				for (int i = startIndex + 1, endIndex = specialList.size() - 1; i < endIndex; i++) { // yes endIndex = size()-1 - we dont want to touch the last entry
					SGpsData mergeMe = specialList.get(startIndex + 1);
					if (mergeMe.isTrue() == prevTrue) {
						toMergeWith.merge(mergeMe, true);
						SGpsData junkgpsData = specialList.get(startIndex + 1);
                        
						specialList.remove(startIndex + 1);
					} else {
						break;
					}
				}
			}
		}// entry was new
		else { // entry is at same time point
			SGpsData oldGpsData = specialList.get(addAt.first);
			boolean oldTrue = oldGpsData.isTrue();

			if (amTrue == oldTrue) {
				// do nothing
			} else {
				toSetSpecialListDirty = true;
				SGpsData prev = specialList.get(addAt.first - 1);
				if (oldTrue) { // amTrue = F we want F(new)/T(old)
					if (prev == null || prev.isTrue()) {// null F(new)/T(old) or T Fnew/T(Old) => shift current T in OldGpsData by 1 ms and then insert the new point
						SGpsData junkgpsData = specialList.get(addAt.first);

						specialList.remove(addAt.first);
						oldGpsData.setArtificial();
						specialList.add(oldGpsData);
						specialList.add(gpsData);
						firstIndexOfChange = addAt.first;
						lastIndexOfChangeExcl = addAt.first + 2;
					} else { // F(old) F(new)/T(old) then Fnew merges with Fold => so do nothing
						// do nothing
					}
				} else {
					if (prev == null || prev.isTrue()) {// null F(old)/T(new) or T old F(old)/T(new) => shift Tnew by 1 ms and then add and then merge from right
						gpsData.setArtificial();
						specialList.add(gpsData);
						// do merging from right common

						int startIndex = addAt.first + 1;// the point gets added at addAt.first+1
						SGpsData toMergeWith = gpsData;
						for (int i = startIndex + 1, endIndex = specialList.size() - 1; i < endIndex; i++) { // yes endIndex = size()-1 - we dont want to touch the last entry
							SGpsData mergeMe = specialList.get(startIndex + 1);
							if (mergeMe.isTrue() == amTrue) {
								toMergeWith.merge(mergeMe, true);
								SGpsData junkgpsData = specialList.get(startIndex + 1);

								specialList.remove(startIndex + 1);
							} else {
								break;
							}
						}
						firstIndexOfChange = startIndex;
						lastIndexOfChangeExcl = firstIndexOfChange + 2;
					} else {// F(old) F(old)/T(new) =>merge oldGpsData to prev, insert gpsData and no merging from right needed because FF => at end
						prev.merge(oldGpsData, true);
						specialList.add(gpsData);
						firstIndexOfChange = addAt.first - 1;
						lastIndexOfChangeExcl = addAt.first + 1;
					}
				}
			}// old value != new value
		}// existing entry
		
		mergeChangeIndex(specialListImpactBegInclEndExcl, firstIndexOfChange, lastIndexOfChangeExcl);
	}// end of func
	private static void mergeChangeIndex(Pair<Integer, Integer> currRes, int start, int endExcl) {
		if (currRes == null)
			return;
		if (start != endExcl) {
			if (currRes.first > start)
				currRes.first = start;
			if (currRes.second < endExcl)
				currRes.second = endExcl;
		}
	}
	
}
