package com.www233.gridpagersnaphelper;

import java.util.ArrayList;
import java.util.List;

public class GridPagerUtils {
    /**
     *
     * @param dataList 原数据
     * @param row 不动方向上的每行/列的最大个数
     * @param pageLimit 每一页的最大个数
     * @return 转换表格方向后的数据
     * @param <T> 数据类型
     */
    static public <T> List<T> transform(List<T> dataList, int row, int pageLimit) {
        List<T> dataListTransform = new ArrayList<>(dataList);
        for (int line_index = 0, line = pageLimit / row; line_index < line; line_index++) {
            for (int row_index = 0; row_index < row; row_index++) {
                dataListTransform.set(row_index + line_index * row, dataList.get(line_index + row_index * line));
            }
        }
        return  dataListTransform;
    }
}
