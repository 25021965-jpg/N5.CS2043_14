package all_test;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Hệ thống kiểm thử toàn diện")
@SelectPackages({
        "common",
        "model",
        "client.network.response.parser",
        "client.network.response",
        "client.network",
        "client.manager",
        "client.util",
        "server.service"
})
public class AllProjectTests {}