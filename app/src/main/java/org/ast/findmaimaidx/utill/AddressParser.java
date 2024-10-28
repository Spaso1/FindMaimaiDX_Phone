package org.ast.findmaimaidx.utill;

import org.ast.findmaimaidx.MainLaunch;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressParser {

    public static void parseAddress(String address) {
        try {
            // 定义正则表达式
            String regex = "(.*?省|.*?市|.*?自治区|.*?自治州|.*?区|.*?县)(.*?市|.*?区|.*?县)";

            // 创建Pattern对象
            Pattern pattern = Pattern.compile(regex);

            // 创建Matcher对象
            Matcher matcher = pattern.matcher(address);

            if (matcher.find()) {
                String province = matcher.group(1);
                String city = matcher.group(2);
                List<String> zhixiashi = new ArrayList<>();
                zhixiashi.add("北京市");
                zhixiashi.add("上海市");
                zhixiashi.add("重庆市");
                zhixiashi.add("天津市");

                if(zhixiashi.contains(province)) {
                    city = province;
                }

                MainLaunch.city = city;
                MainLaunch.province = province;
            } else {
                System.out.println("无法解析地址");
            }
        }catch (Exception e) {
        }

    }
}
