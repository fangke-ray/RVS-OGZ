package com.osh.rvsif.phenomenon.mapper;

import com.osh.rvsif.phenomenon.bean.NewPhenomenonEntity;

public interface NewPhenomenonMapper {

	public NewPhenomenonEntity getNewPhenomenon(String alarmMessageId);

	public void setReturnStatus(NewPhenomenonEntity newPhenomenon);

	public String searchUserDefineCodes();

	public String getLastMessageGroupNumber(String last_sent_message_number);
}
