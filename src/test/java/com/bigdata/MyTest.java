package com.bigdata;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Desciption
 * Create By  li.bo
 * CreateTime 2018/2/6 13:41
 * UpdateTime 2018/2/6 13:41
 */
@Slf4j
public class MyTest {

    @Test
    public void testMonth10() {

        List<Double> list = new ArrayList<>();
        list.add(23600.00);
        list.add(58055.00);
        list.add(605030.00);
        list.add(19270.00);
        list.add(2266120.00);
        list.add(221962.69);
        list.add(949.25);
        list.add(200000.00);
        list.add(1050000.00);
        list.add(18550.14);
        list.add(1590000.00);
        list.add(600000.00);
        list.add(5000000.00);
        list.add(1100000.00);
        list.add(2350000.00);
        list.add(2890000.00);
        list.add(17528.70);
        list.add(17340.00);
        list.add(1892.10);
        list.add(22134.00);
        list.add(4760000.00);
        list.add(55893.96);
        list.add(4455.36);
        list.add(57432.12);
        list.add(9783.23);
        list.add(10200.00);
        list.add(7497.00);
        list.add(5195.88);
        list.add(6165.73);
        list.add(31835.22);
        list.add(2890000.00);
        list.add(339290.00);
        list.add(3420000.00);

        Long r = list.stream().mapToLong(value -> Math.round(value)).sum();
        log.info("r:{}", r);
    }

    @Test
    public void testMonth11() {

        List<Double> list = new ArrayList<>();

        list.add(1390000.00);
        list.add(2070000.00);
        list.add(96658.00);
        list.add(2500000.00);
        list.add(6000000.00);
        list.add(1000000.00);
        list.add(2600.00);
        list.add(1800000.00);
        list.add(753058.00);
        list.add(753057.00);

        Long r = list.stream().mapToLong(value -> Math.round(value)).sum();
        log.info("r:{}", r);
    }

    @Test
    public void testMonth12() {

        List<Double> list = new ArrayList<>();

        list.add(5050000.00);
        list.add(800000.00);
        list.add(1400000.00);
        list.add(1900000.00);
        list.add(1300000.00);
        list.add(4500000.00);
        list.add(7363.04);
        list.add(17618.46);
        list.add(69247.29);
        list.add(57788.78);
        list.add(5032.00);
        list.add(3765.84);
        list.add(13535.40);
        list.add(9655.83);
        list.add(21223.48);
        list.add(15810.00);
        list.add(4510.00);
        list.add(500000.00);
        list.add(3839.62);
        list.add(30416.91);
        list.add(4500000.00);
        list.add(5300000.00);
        list.add(3200000.00);
        list.add(2300000.00);
        list.add(500000.00);
        list.add(181.56);
        list.add(2541772.75);
        list.add(6500.00);
        list.add(918180.00);
        list.add(1500000.00);
        list.add(2000000.00);
        list.add(1325.38);
        list.add(3.79);
        list.add(1000000.00);
        list.add(12166.56);
        list.add(4998.00);
        list.add(9790.98);
        list.add(10047.00);
        list.add(3848.46);
        list.add(38288.08);
        list.add(2970.24);
        list.add(22457.00);
        list.add(9535.98);
        list.add(15324.82);
        list.add(5865.00);
        list.add(11685.80);
        list.add(3463.92);
        list.add(299.18);
        list.add(500470.00);

        Long r = list.stream().mapToLong(value -> Math.round(value)).sum();
        log.info("r:{}", r);
    }
}