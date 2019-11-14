package com.camunda.react.starter.bpm;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import com.camunda.react.starter.AppConfigProperties.GracePeriodSetting;

import java.util.logging.Logger;

public class GetRemainingDaysDelegate implements JavaDelegate {
	public static Logger log = Logger.getLogger(GetRemainingDaysDelegate.class.getName());

	@SuppressWarnings("unchecked")
	public void execute(DelegateExecution execution) {
		
		Date leaseExpirationDate = (Date)execution.getVariable("leaseExpirationDate");
		log.fine("The lease expiration date is: "
				+ leaseExpirationDate);
		
		int leaseExpirationBufferDays = (Integer)execution.getVariable("leaseExpirationBufferDays");
		log.fine("The buffer number of days for renewal deadline: "
				+ leaseExpirationBufferDays);
		
		//Getting the Renewal Dead line days. 
		//Subtracting the Lease end date by number of buffer days 
		// until final notice is sent so property manger can list property.
		LocalDate expDate = leaseExpirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate renewalDeadlineDate = expDate.minusDays(leaseExpirationBufferDays);
		log.fine("The renewal deadline date: "
				+ renewalDeadlineDate);

		int remainingDays = (int)ChronoUnit.DAYS.between(LocalDate.now(), renewalDeadlineDate);
		execution.setVariable("remainingDays", remainingDays);
		log.fine("Getting Rmaining Days: "
				+ remainingDays);

		List<GracePeriodSetting> settings = (List<GracePeriodSetting>)execution.getVariable("gracePeriodSettings");
		
		//set the default grace period  
		//then make the grace period shorter based on the days remaining on the renewal
		execution.setVariable("gracePeriod", settings.get(0).getCron());		
		for(GracePeriodSetting setting : settings){
			if (remainingDays > setting.getBufferDays())
				execution.setVariable("gracePeriod", setting.getCron());
		}	
	}
}