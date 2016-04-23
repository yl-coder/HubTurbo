package tests;

import org.junit.Test;
import util.ApiQuotaManager;

import static org.junit.Assert.*;

/**
 * Tests the correct behaviour of the methods in the ApiQuotaManager class.
 */
public class ApiQuotaManagerTest {

    @Test
    public void computeTickerTimer_initialAppLaunch() {
        long result = ApiQuotaManager.computeRefreshTimerPeriod(100, 10, 0, 200, 1);
        assertEquals(result, 11);

        result = ApiQuotaManager.computeRefreshTimerPeriod(100, 10, 0, 50, 1);
        assertEquals(result, 1);
    }

    @Test
    public void computeTickerTimer_outOfQuota() {
        long result = ApiQuotaManager.computeRefreshTimerPeriod(0, 35, 0, 0, 1);
        assertEquals(result, 36);

        result = ApiQuotaManager.computeRefreshTimerPeriod(0, 35, 15, 100, 1);
        assertEquals(result, 36);
    }

    @Test
    public void computeTickerTimer_minimalQuotaWithApiQuotaEqualsApiQuotaBuffer() {
        long result = ApiQuotaManager.computeRefreshTimerPeriod(1, 35, 15, 1, 1);
        assertEquals(result, 36);
    }

    @Test
    public void computeTickerTimer_minimalQuotaWithApiQuotaBufferZero() {
        long result = ApiQuotaManager.computeRefreshTimerPeriod(1, 35, 15, 0, 1);
        assertEquals(result, 36);
    }

    @Test
    public void computeTickerTimer_quotaBelowApiQuotaBuffer() {
        long result = ApiQuotaManager.computeRefreshTimerPeriod(199, 35, 25, 200, 1);
        assertEquals(result, 36);

        result = ApiQuotaManager.computeRefreshTimerPeriod(1, 35, 25, 2, 37);
        assertEquals(result, 37);

        result = ApiQuotaManager.computeRefreshTimerPeriod(1, 35, 25, 2, 2);
        assertEquals(result, 36);
    }

    @Test
    public void computeTickerTimer_quotaAtApiQuotaBuffer() {
        long result = ApiQuotaManager.computeRefreshTimerPeriod(200, 35, 25, 200, 1);
        assertEquals(result, 36);
    }

    @Test
    public void computeTickerTimer_quotaAboveApiQuotaBuffer() {
        long result;
        /*
        Case #1: when (apiQuota - apiQuotaBuffer) is less than apiCallsUsedInPreviousRefresh,
        Don't auto refresh until next apiQuota renewal
        */
        result = ApiQuotaManager.computeRefreshTimerPeriod(201, 35, 25, 200, 1);
        assertEquals(result, 36);

        result = ApiQuotaManager.computeRefreshTimerPeriod(224, 35, 25, 200, 1);
        assertEquals(result, 36);

        /*
        Case #2: when (apiQuota - apiQuotaBuffer) is more or equal than apiCallsUsedInPreviousRefresh,
        Calculate the refreshTime based on apiQuota, apiCallsUsedInPreviousRefresh, remainingTimeInMins
        and apiQuotaBuffer.
        */
        result = ApiQuotaManager.computeRefreshTimerPeriod(225, 35, 25, 200, 1);
        assertEquals(result, 36);

        result = ApiQuotaManager.computeRefreshTimerPeriod(226, 35, 25, 200, 1);
        assertEquals(result, 36);

        result = ApiQuotaManager.computeRefreshTimerPeriod(3000, 35, 25, 200, 5);
        assertEquals(result, 5);

        result = ApiQuotaManager.computeRefreshTimerPeriod(3000, 35, 223, 0, 1);
        assertEquals(result, 3);

        result = ApiQuotaManager.computeRefreshTimerPeriod(3000, 1, 223, 1, 1);
        assertEquals(result, 2);
    }

    @Test
    public void computeTickerTimer_noOfRefreshEqualOne() {
        long result = ApiQuotaManager.computeRefreshTimerPeriod(886, 9, 502, 200, 1);
        assertEquals(result, 10);
    }

    @Test
    public void computeTickerTimer_remainingTimeEqualZero() {
        long result = ApiQuotaManager.computeRefreshTimerPeriod(0, 0, 502, 1, 2);
        assertEquals(result, 2);
    }
}
