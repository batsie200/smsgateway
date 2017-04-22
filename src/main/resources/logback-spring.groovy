import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy

import static ch.qos.logback.classic.Level.INFO

appender("FILE", RollingFileAppender) {
    file = "/home/cindy/data/logs/smsgateway/gateway.log"
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "/home/cindy/data/logs/smsgateway/backups/gateway.log-%d{yyyy-MM-dd}.gz"
        maxHistory = 30
    }
    layout(PatternLayout) {
        pattern = "%d - %-5level - %msg%n"
    }
}
root(INFO, ["FILE"])
