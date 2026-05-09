package all_test;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Hệ thống kiểm thử toàn diện")
@SelectPackages({"client", "server", "common", "utils"})
public class AllProjectTests {
}