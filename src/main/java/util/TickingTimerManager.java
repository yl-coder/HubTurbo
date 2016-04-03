package util;

import ui.UI;

/**
 * This class is used to hold the TickingTimer instance and perform operations associated with it.
 */
public class TickingTimerManager {


    public static final int BUFFER_TIME = 1; //To allow some additional time until next refresh.
    private final TickingTimer timer;
    public static final int REFRESH_PERIOD = 60;
    public static final int APIQUOTA_BUFFER = 200;

    public TickingTimerManager(TickingTimer timer){
        this.timer = timer;
    }

    public void startTimer(){
        timer.start();
    }

    public void restartTimer(){
        timer.restart();
    }


    /* This function updates the TickerTimer period, which is used to periodically refresh the issues.
        * @param apiQuota : The remaining allowed api request until the next api request allowance renewal.
        * @param remainingTime : The remaining time left until the next api request allowance renewal.
        * @param lastApiCallsUsed : The amount of api used in the last api pull.
        */
   public int updateTickerTimer(int apiQuota, long remainingTime, int lastApiCallsUsed){

       assert apiQuota >=0 && remainingTime >=0 && lastApiCallsUsed >=0;

       long refreshTimeInMins;

       if (lastApiCallsUsed > 0) {
           // If less or equal than APIQUOTA_BUFFER, restricts auto refresh until next apiQuota renewal.
           // The APIQUOTA_BUFFER is reserved for Manual refresh and creation of issues, etc.
           if (apiQuota <= APIQUOTA_BUFFER){
               refreshTimeInMins = remainingTime + BUFFER_TIME;
           } else {
               if (apiQuota - APIQUOTA_BUFFER >= lastApiCallsUsed) {
                   refreshTimeInMins =  (long) Math.ceil(remainingTime /
                           Math.floor((apiQuota - APIQUOTA_BUFFER) / (double) lastApiCallsUsed));
               } else {
                   // Restricts auto refresh until next apiQuota renewal.
                   refreshTimeInMins = remainingTime + BUFFER_TIME;
               }
           }
       } else { // Enters here during application initialisation or when apiQuota = 0 and lastApiCallsUsed = 0
           if (apiQuota != 0) {
               // Set to default during initialisation
               refreshTimeInMins = Utility.secsToMins(REFRESH_PERIOD);
           } else {
               // Restricts auto refresh until next apiQuota renewal.
               refreshTimeInMins = remainingTime + BUFFER_TIME;
           }

       }

       timer.changePeriod((int) Utility.minsToSecs(refreshTimeInMins));

       return (int) refreshTimeInMins;
   }
}
