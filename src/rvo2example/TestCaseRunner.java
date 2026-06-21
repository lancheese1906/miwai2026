package rvo2example;

/**
 * Test Runner - Chạy tất cả testcase theo thứ tự Giúp so sánh kết quả giữa các
 * tình huống giao thông khác nhau
 */
public class TestCaseRunner {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║     TRAFFIC SIMULATION TEST SUITE                       ║");
        System.out.println("║     Real-world Traffic Scenarios                        ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        if (args.length == 0) {
            printMenu();
        } else {
            runTestCase(args[0]);
        }
    }

    public static void printMenu() {
        System.out.println("AVAILABLE TEST CASES:");
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("1. TC1_OneWayLight");
        System.out.println("   - Description: One-way light traffic (morning)");
        System.out.println("   - Agents: 25 (5x5)");
        System.out.println("   - Flow: 1 direction only (+,+)");
        System.out.println("   - Expected: No conflicts, smooth flow\n");

        System.out.println("2. TC2_TwoWayConflict");
        System.out.println("   - Description: Two-way opposing traffic");
        System.out.println("   - Agents: 49 (7x7)");
        System.out.println("   - Flow: 2 opposite directions (+,+) vs (-,-)");
        System.out.println("   - Expected: Heavy conflicts, collision avoidance\n");

        System.out.println("3. TC3_FourWayIntersection");
        System.out.println("   - Description: Four-way intersection (rush hour)");
        System.out.println("   - Agents: 64 (8x8)");
        System.out.println("   - Flow: 4 directions from all quadrants");
        System.out.println("   - Expected: Complex interaction at center\n");

        System.out.println("4. TC4_RushHourCongestion");
        System.out.println("   - Description: Heavy congestion (peak hour)");
        System.out.println("   - Agents: 150 (15x10)");
        System.out.println("   - Flow: 1 direction, high density");
        System.out.println("   - Expected: Performance stress test\n");

        System.out.println("5. TC5_Roundabout");
        System.out.println("   - Description: Roundabout traffic pattern");
        System.out.println("   - Agents: 80 (10x8)");
        System.out.println("   - Flow: 4 directions entering, circular flow");
        System.out.println("   - Expected: Rotational pattern\n");

        System.out.println("6. TC6_TwoIntersections");
        System.out.println("   - Description: Two continuous intersections");
        System.out.println("   - Agents: 30 (5x6)");
        System.out.println("   - Flow: bi-directional, two intersections in series");
        System.out.println("   - Expected: Sequential interaction between two intersections\n");

        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("USAGE: java TestCaseRunner <test_number>");
        System.out.println("       java TestCaseRunner 1");
        System.out.println("       java TestCaseRunner 2");
        System.out.println("       (etc.)\n");

        System.out.println("DIRECT EXECUTION:");
        System.out.println("java TestCase01_OneWayLight");
        System.out.println("java TestCase02_TwoWayConflict");
        System.out.println("java TestCase03_FourWayIntersection");
        System.out.println("java TestCase04_RushHourCongestion");
        System.out.println("java TestCase05_Roundabout");
        System.out.println("java TestCase06_TwoIntersections");
    }

    public static void runTestCase(String testNumber) {
        switch (testNumber) {
            case "1":
                System.out.println("Starting: TC1 - One-Way Light Traffic\n");
                TestCase01_OneWayLight.main(new String[]{});
                break;
            case "2":
                System.out.println("Starting: TC2 - Two-Way Conflict Traffic\n");
                TestCase02_TwoWayConflict.main(new String[]{});
                break;
            case "3":
                System.out.println("Starting: TC3 - Four-Way Intersection\n");
                TestCase03_FourWayIntersection.main(new String[]{});
                break;
            case "4":
                System.out.println("Starting: TC4 - Rush Hour Congestion\n");
                TestCase04_RushHourCongestion.main(new String[]{});
                break;
            case "5":
                System.out.println("Starting: TC5 - Roundabout Traffic\n");
                TestCase05_Roundabout.main(new String[]{});
                break;
            case "6":
                System.out.println("Starting: TC6 - Two Continuous Intersections\n");
                TestCase06_TwoIntersections.main(new String[]{});
                break;
            default:
                System.out.println("Invalid test number: " + testNumber);
                printMenu();
        }
    }

    /**
     * Chạy tất cả testcase liên tiếp
     */
    public static void runAllTestCases() {
        System.out.println("Running ALL test cases...\n");

        for (int i = 1; i <= 5; i++) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("RUNNING TEST CASE " + i);
            System.out.println("=".repeat(60) + "\n");
            runTestCase(String.valueOf(i));

            try {
                Thread.sleep(2000); // Pause between tests
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("ALL TEST CASES COMPLETED");
        System.out.println("=".repeat(60));
    }
}
