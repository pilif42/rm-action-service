package uk.gov.ons.ctp.response.action.scheduled;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.distributed.DistributedInstanceManager;
import uk.gov.ons.ctp.common.distributed.DistributedLatchManager;
import uk.gov.ons.ctp.common.distributed.DistributedLockManager;
import uk.gov.ons.ctp.response.action.service.ActionReportService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * The scheduler to trigger reports creation based on a cron expression defined in application.yml
 */
@Component
@Slf4j
public class ReportScheduler {

    private static final String DISTRIBUTED_OBJECT_KEY_REPORT_LATCH = "reportlatch";
    private static final String DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT = "reportscheduler";
    private static final String DISTRIBUTED_OBJECT_KEY_REPORT = "report";

    @Autowired
    private ActionReportService actionReportService;

    @Autowired
    private DistributedLockManager reportDistributedLockManager;

    @Autowired
    private DistributedInstanceManager reportDistributedInstanceManager;

    @Autowired
    private DistributedLatchManager reportDistributedLatchManager;

    /**
     * Initialise report scheduler
     */
    @PostConstruct
    public void init() {
        reportDistributedInstanceManager.incrementInstanceCount(DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT);
        log.info("{} {} instance/s running",
                reportDistributedInstanceManager.getInstanceCount(DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT),
                DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT);
    }

    /**
     * Clean up report scheduler on bean destruction
     */
    @PreDestroy
    public void cleanUp() {
        reportDistributedInstanceManager.decrementInstanceCount(DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT);
        reportDistributedLockManager.unlockInstanceLocks();
        log.info("{} {} instance/s running",
                reportDistributedInstanceManager.getInstanceCount(DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT),
                DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT);
    }

    /**
     * The method triggering report creation.
     */
    @Scheduled(cron = "#{appConfig.reportSettings.cronExpression}")
    public void createReport() {
        log.debug("Entering createReport...");

        reportDistributedLatchManager.setCountDownLatch(DISTRIBUTED_OBJECT_KEY_REPORT_LATCH,
                reportDistributedInstanceManager.getInstanceCount(DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT));

        if (!reportDistributedLockManager.isLocked(DISTRIBUTED_OBJECT_KEY_REPORT)) {
            if (reportDistributedLockManager.lock(DISTRIBUTED_OBJECT_KEY_REPORT)) {
                actionReportService.createReport();
            }
        }

        try {
            reportDistributedLatchManager.countDown(DISTRIBUTED_OBJECT_KEY_REPORT_LATCH);
            if (!reportDistributedLatchManager.awaitCountDownLatch(DISTRIBUTED_OBJECT_KEY_REPORT_LATCH)) {
                log.error("Report run error countdownlatch timed out, should be {} instances running",
                        reportDistributedInstanceManager.getInstanceCount(DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT));
            }
        } catch (InterruptedException e) {
            log.error("Report run error waiting for countdownlatch: {}", e.getMessage());
            log.error("Stack trace: ", e);
        } finally {
            reportDistributedLockManager.unlock(DISTRIBUTED_OBJECT_KEY_REPORT);
            reportDistributedLatchManager.deleteCountDownLatch(DISTRIBUTED_OBJECT_KEY_REPORT_LATCH);
        }
    }
}
