package tests;

import org.junit.Test;
import util.TickingTimer;
import util.TickingTimerManager;
import util.Utility;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * This class tests the correct behaviour of the updateTickerTimer method of the TickingTimerManager class.
 */
public class TickingTimerManagerTest {

    TickingTimerManager timerMgr;

    public TickingTimerManagerTest() {
        TickingTimer refreshTimer = new TickingTimer("Refresh Timer", TickingTimerManager.REFRESH_PERIOD,
                (Integer stub) -> {}, () -> {}, TimeUnit.SECONDS);
        timerMgr = new TickingTimerManager(refreshTimer);
    }

    @Test
    public void updateTickerTimerWhenInitAppLaunch() {
        int result = timerMgr.updateTickerTimer(100, 10, 0);
        assertEquals(result, Utility.secsToMins(TickingTimerManager.REFRESH_PERIOD));
    }

    @Test
    public void updateTickerTimerWhenOutOfQuota() {
        int result = timerMgr.updateTickerTimer(0, 35, 0);
        assertEquals(result, 35 + TickingTimerManager.BUFFER_TIME);
    }

    @Test
    public void updateTickerTimerWhenMinimalQuota() {
        int result = timerMgr.updateTickerTimer(1, 35, 15);
        assertEquals(result, 35 + TickingTimerManager.BUFFER_TIME);
    }

    @Test
    public void updateTickerTimerWhenQuotaBelowAPIQuotaBuffer() {
        int result = timerMgr.updateTickerTimer(199,
                35, 25);
        assertEquals(result, 35 + TickingTimerManager.BUFFER_TIME);

        result = timerMgr.updateTickerTimer(1,
                35, 25);
        assertEquals(result, 35 + TickingTimerManager.BUFFER_TIME);
    }

    @Test
    public void updateTickerTimerWhenQuotaAtAPIQuotaBuffer() {
        int result = timerMgr.updateTickerTimer(200,
                35, 25);
        assertEquals(result, 35 + TickingTimerManager.BUFFER_TIME);
    }

    @Test
    public void updateTickerTimerWhenQuotaAboveAPIQuotaBuffer() {

        /*
        Case #1: when remainingQuota - buffer is less than lastApiCallsUsed,
        Don't auto refresh until next apiQuota renewal
        */
        int result = timerMgr.updateTickerTimer(201,
                35, 25);
        assertEquals(result, 35 + TickingTimerManager.BUFFER_TIME);

        result = timerMgr.updateTickerTimer(224,
                35, 25);
        assertEquals(result, 35 + TickingTimerManager.BUFFER_TIME);

        /*
        Case #2: when (remainingQuota - buffer) is more or equal than lastApiCallsUsed,
        Calculate the refreshTime as normal
        */
        result = timerMgr.updateTickerTimer(225, 35, 25);
        assertEquals(result, 35);

        result = timerMgr.updateTickerTimer(226, 35, 25);
        assertEquals(result, 35);

        result = timerMgr.updateTickerTimer(3000, 35, 25);
        assertEquals(result, 1);

        result = timerMgr.updateTickerTimer(3000, 35, 223);
        assertEquals(result, 3);

    }
}
