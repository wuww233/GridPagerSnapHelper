# GridPagerSnapHelper

A SnapHelper which makes RecyclerView be snapped like Viewpager

## Examples

Move (horizontal):

![move_horizontal](https://cdn.jsdelivr.net/gh/Jungezi/wwwImage@main/img/example_1.gif)

<details>
<summary>More examples</summary>
<br>Move (vertical):<br>
<img src="https://raw.githubusercontent.com/wuww233/wwwImage/main/img/example_3.gif" alt="move_vertical">
    <br><br>
Scroll (horizontal):<br>
<img src="https://cdn.jsdelivr.net/gh/Jungezi/wwwImage@main/img/example_2.gif" alt="scroll_horizontal">
    <br><br>
Scroll (vertical):<br>
<img src="https://raw.githubusercontent.com/wuww233/wwwImage/main/img/example_4.gif" alt="scroll_vertical">
</details>

+ Source codes of these examples are
  in `app/src/main/java/com/www233/gridpagersnaphelper/MainActivity.java`

## Features

1. 支持按列滑动和按行滑动、直接设置当前页面

2. 最后一个页面无需填满组件

    + 实现方式：在调用`attachToRecyclerView`方法时，会为recyclerView的最后一行/列添加装饰器以确保其大小与其他页数符合一致，从而可以正常滑动页面

        因此，当不需要用到该GridPageSnapHelper时，需要将此装饰器去掉（即调用`detachToRecyclerView`方法，在下一部分会提到）

3. 拖动半个及以上组件的长度即可移动至下一页/上一页

4. 提供工具类，可以将以List形式组织的原数据更改顺序，从而更改在表格中的显示方向
    + 例如：调整后，数据的显示格式会从左图变为右图
    
        <table style="display:initial">
            <tr style="border:0px">
            	<td style="border:0px"><img src="https://raw.githubusercontent.com/wuww233/wwwImage/main/img/Screenshot_20240820_102035.jpg" alt="图1" /></td>
            	<td style="border:0px"><img src="https://raw.githubusercontent.com/wuww233/wwwImage/main/img/Screenshot_20240820_102053.jpg" alt="图2"  /></td>
            </tr>
        </table>

5. 可自动根据recyclerView的尺寸调整子组件的宽度或高度（通过直接修改原组件的layoutParams）

    ！各个组件在移动方向上的尺寸**必须一致**，否则SnapHelper的距离计算会出现问题



## Updates

+ `1.1.0`

    + Class `GridPagerUtils` for data transforming

    + Function `setChildAutoAdjust` for size adjustment



## How to use

1. Add the dependence in a file like:

    + `build.gradle`(Groovy)

      ```groovy
      dependencies {
        implementation 'io.github.wuww233:GridPagerSnapHelper:1.1.0'
      }
      ```

    + or `build.gradle.kts`(Kotlin)
      ```kotlin
      dependencies {
        implementation("io.github.wuww233:GridPagerSnapHelper:1.1.0")
      }
      ```


2. Use it in your code

    ```java
    // optimal : transform data
    List<T> data_transform = GridPagerUtils.transform(dataList, row, pageLimit);
    
    // Set Adapter and GridLayoutManager for your RecyclerView before setting GridPageSnapHelper
    GridPageSnapHelper snapHelper = new GridPageSnapHelper(max_size_in_each_row_or_line, max_size_of_each_page);
    snapHelper.attachToRecyclerView(recyclerView);
    
    // optimal : automaticly adjust width / height
    boolean widthAdjust = true, heightAdjust = true;
    snapHelper.setChildAutoAdjust(widthAdjust, heightAdjust);
    
    // If you want to attach recyclerView to another snapHelper, detach the current one firstly.
    snapHelper.detachToRecyclerView();	// forgive my poor English :(
    
    // Then you can attach to another snapHelper
    GridPageSnapHelper snapHelper2 = new GridPageSnapHelper(row, pageLimit);
    snapHelper2.attachToRecyclerView(recyclerView);
    
    ```

## Other APIs

```java
// set listener
snapHelper.setOnPageChangeListener(new GridPagerSnapHelper.OnPageChangeListener(){
	@Override
	public void onChange(int pageBeforeChange,int pageAfterChange){
        // Function will be called when current page is changed.
	}
});

// scroll
snapHelper.scrollToPage(target_page_index);
snapHelper.smoothScrollToPage(target_page_index);

// others
snapHelper.getCurrentPageIndex();
snapHelper.getPageCount();
snapHelper.setRow(int row);
snapHelper.setPageLimit(int pageLimit);
```