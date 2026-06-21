# TESTCASE SUITE - REAL-WORLD TRAFFIC SCENARIOS

## Tổng Quan

Bộ testcase này mô phỏng 5 tình huống giao thông thực tế khác nhau:

## 5 Testcase Chính

### TC1: One-Way Light Traffic (Đường một chiều - Buổi sáng)
- **File**: `TestCase01_OneWayLight.java`
- **Agents**: 25 (5×5 grid)
- **Luồng**: 1 chiều, tất cả agent đi cùng hướng (+,+)
- **Hành vi**: Mix 3 loại hành vi
- **Mục đích**: Baseline test - kiểm tra hiệu suất cơ bản
- **Kỳ vọng**: Không có xung đột, luồng mượt mà

### TC2: Two-Way Conflict (Đường hai chiều - Xung đột)
- **File**: `TestCase02_TwoWayConflict.java`
- **Agents**: 49 (7×7 grid)
- **Luồng**: 2 chiều đối lập (nửa +,+ / nửa -,-)
- **Hành vi**: Hành vi 0 vs Hành vi 2
- **Mục đích**: Kiểm tra xử lý va chạm trực tiếp
- **Kỳ vọng**: Nhiều xung đột, cần tránh va chạm tốt

### TC3: Four-Way Intersection (Ngã tư giao thông)
- **File**: `TestCase03_FourWayIntersection.java`
- **Agents**: 64 (8×8 grid)
- **Luồng**: 4 chiều từ 4 hướng (giữa là giao lộ)
- **Hành vi**: Mỗi phần tư dùng hành vi khác nhau
- **Mục đích**: Kiểm tra tình huống giao lộ phức tạp
- **Kỳ vọng**: Tương tác phức tạp ở trung tâm

### TC4: Rush Hour Congestion (Giờ cao điểm - Tắc đường)
- **File**: `TestCase04_RushHourCongestion.java`
- **Agents**: 150 (15×10 grid)
- **Luồng**: 1 chiều, mật độ cao
- **Hành vi**: Mix các loại hành vi, chủ yếu hành vi 0
- **Mục đích**: Stress test - hiệu suất với mật độ cao
- **Kỳ vọng**: Kiểm tra khả năng xử lý của hệ thống

### TC5: Roundabout (Vòng xoay giao thông)
- **File**: `TestCase05_Roundabout.java`
- **Agents**: 80 (10×8 grid)
- **Luồng**: 4 hướng nhập vào, luồng tuần hoàn
- **Hành vi**: Các hướng khác nhau, tạo vòng
- **Mục đích**: Kiểm tra mô hình luồng tuần hoàn
- **Kỳ vọng**: Luồng hình tròn, không bế tắc

## Cách Chạy

### Option 1: Chạy từng testcase riêng lẻ
```bash
# Compile
javac TestCase01_OneWayLight.java

# Run
java TestCase01_OneWayLight
java TestCase02_TwoWayConflict
java TestCase03_FourWayIntersection
java TestCase04_RushHourCongestion
java TestCase05_Roundabout
```

### Option 2: Dùng TestCaseRunner
```bash
# Hiển thị menu
java TestCaseRunner

# Chạy testcase cụ thể
java TestCaseRunner 1  # TC1
java TestCaseRunner 2  # TC2
java TestCaseRunner 3  # TC3
java TestCaseRunner 4  # TC4
java TestCaseRunner 5  # TC5
```

## Các Metrics Theo Dõi

Mỗi testcase sẽ output:
- **Total Agents**: Tổng số agent
- **Completed**: Số agent đã đạt đích
- **Total Simulation Time**: Tổng thời gian mô phỏng (ms)
- **Total Steps**: Số bước mô phỏng
- **Avg Time per Agent**: Thời gian trung bình per agent (ms)
- **FPS**: Tốc độ frame (bước/giây)
- **Memory Used**: Bộ nhớ sử dụng (MB)

## Kết Cấu File

```
BaseTrafficScenario.java      <- Base class với hàm chung
TestCase01_OneWayLight.java   <- TC1
TestCase02_TwoWayConflict.java <- TC2
TestCase03_FourWayIntersection.java <- TC3
TestCase04_RushHourCongestion.java <- TC4
TestCase05_Roundabout.java    <- TC5
TestCaseRunner.java           <- Test runner
```

## Behavior Types (Hành vi Agent)

| Index | Tên | Neighbor Dist | Max Neighbors | Time Horizon | Radius | Max Speed |
|-------|-----|---|---|---|---|---|
| 0 | Normal Person (P) | 15 | 40 | 38 | 0.4 | 1.55 |
| 1 | Careful (E) | 15 | 23 | 32 | 0.4 | 1.55 |
| 2 | Elder/Large (N) | 15 | 9 | 29 | 1.6 | 1.25 |
| 3 | Aggressive (A) | 15 | 20 | 31 | 0.6 | 1.55 |
| 4 | Very Careful (AT) | 13 | 17 | 40 | 0.4 | 1.55 |
| 5 | Heavy (H) | 15 | 7 | 30 | 1.1 | 1.25 |
| 6 | Child (Default) | 15 | 5 | 5 | 2 | 1.25 |

## Directions (Hướng)

- **0**: (+,+) - Phía trên phải
- **2**: (-,-) - Phía dưới trái (ngược với 0)
- **3**: (+,-) - Phía dưới phải
- **5**: Khác

## Kích Thước So Sánh

| TC | Name | Grid | Agents | Complexity | Est. Time |
|----|------|------|--------|------------|-----------|
| 1 | One-Way Light | 5×5 | 25 | Low | ~30s |
| 2 | Two-Way | 7×7 | 49 | Medium | ~1m |
| 3 | 4-Way | 8×8 | 64 | High | ~1.5m |
| 4 | Rush Hour | 15×10 | 150 | Very High | ~3m |
| 5 | Roundabout | 10×8 | 80 | High | ~2m |

## Cách Mở Rộng

Để tạo testcase mới, kế thừa từ `BaseTrafficScenario`:

```java
public class TestCase06_Custom extends BaseTrafficScenario {
    public TestCase06_Custom() {
        scenarioName = "TC6: Custom Scenario";
        trafficType = "custom";
        totalAgents = 100;
    }

    @Override
    protected void setupScenario() {
        // Setup logic
    }

    public static void main(String[] args) {
        TestCase06_Custom testCase = new TestCase06_Custom();
        testCase.runSimulation();
    }
}
```

## Kết Quả Mong Đợi

### TC1 (One-Way Light)
- Smooth flow
- Minimal conflicts
- Time: ~500ms per agent

### TC2 (Two-Way Conflict)
- Many conflict avoidance
- Agents must navigate around each other
- Time: ~800ms per agent

### TC3 (4-Way Intersection)
- Complex interactions
- Central congestion
- Time: ~1000ms per agent

### TC4 (Rush Hour)
- Performance bottleneck
- High CPU usage
- Time: ~1200ms per agent

### TC5 (Roundabout)
- Rotational pattern
- No major congestion if algorithm works well
- Time: ~900ms per agent
