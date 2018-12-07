package com.osh.rvs.bean.inline;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class PutinBalanceBound {

	/** 平均用时 */
	private BigDecimal avgCost = null;

	/** 先前投入标准时间 */
	private Queue<Integer> recentInputs = new LinkedBlockingQueue<Integer>();

	/** 先前投入标准时间平均用时 */
	private Integer nowSum = null;
	private BigDecimal nowBalance = null;

	public PutinBalanceBound(BigDecimal cntAvgCost, List<Integer> cntRecentInputs) {
		avgCost = cntAvgCost;
		recentInputs.addAll(cntRecentInputs);
		evalNowBalance();
		if (cntAvgCost == null) {
			avgCost = nowBalance;
		}
	}

	/** 计算先前投入标准时间平均用时 */
	private void evalNowBalance(){
		int count = 0;
		if (recentInputs.size() == 0) {
			nowBalance = BigDecimal.ZERO;
			return;
		}

		Iterator<Integer> iterator = recentInputs.iterator();
		Integer recent = iterator.next();
		while (recent != null) {
			count += recent;
			if (!iterator.hasNext()) break;
			recent = iterator.next();
		}
		nowSum = count;
		nowBalance = new BigDecimal(count).divide(new BigDecimal(recentInputs.size()), 2, BigDecimal.ROUND_HALF_UP);
	}

	/** 计算投入平衡差 */
	public BigDecimal evalBalanceDiff(int newStandard) {
		if (nowBalance == null) evalNowBalance();
		BigDecimal bgCount = new BigDecimal(nowSum).add(new BigDecimal(newStandard));
		BigDecimal avg = bgCount.divide(new BigDecimal(recentInputs.size() + 1), 2, BigDecimal.ROUND_HALF_UP);
		return avg.subtract(avgCost);
	}

	public void putNowBalance(int newStandard) {
		recentInputs.offer(newStandard);
		recentInputs.poll();
		avgCost = avgCost.multiply(new BigDecimal(5)).add(new BigDecimal(newStandard)).divide(new BigDecimal(6));
		evalNowBalance();
	}

	public String toString() {
		if (avgCost == null) return null;
		return avgCost.toPlainString() + ">>>" + nowBalance.toPlainString();
	}
}
