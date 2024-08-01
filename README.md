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

+ Source codes of these examples are in `app/src/main/java/com/www233/gridpagersnaphelper/MainActivity.java`

## Features

1. 支持按列滑动和按行滑动、直接设置当前页面
2. 最后一个页面无需填满组件
3. 拖动半个及以上组件的长度即可移动至下一页/上一页

\* 各个组件的**尺寸须一致**，否则SnapHelper的距离计算会出现问题

## How to use

1. Add the dependence in a file like:

    + `build.gradle`(Groovy)
      
      ```groovy
      dependencies {
        implementation 'io.github.wuww233:GridPagerSnapHelper:1.0.0'
      }
      ```

    + or `build.gradle.kts`(Kotlin)
      ```kotlin
      dependencies {
        implementation("io.github.wuww233:GridPagerSnapHelper:1.0.0")
      }
      ```


2. Use it in your code

    ```java
    // set Adapter and GridLayoutManager for your RecyclerView before setting GridPageSnapHelper
    GridPageSnapHelper snapHelper = new GridPageSnapHelper(max_size_in_each_row_or_line, max_size_of_each_page);
    snapHelper.attachToRecyclerView(your_RecyclerView_name_managed_by_GridLayoutManager);
    ```

    

## APIs

```java
// set listener
snapHelper.setOnPageChangeListener(new GridPagerSnapHelper.OnPageChangeListener() {
  @Override
  public void onChange(int pageBeforeChange, int pageAfterChange) {
      // Function will be called when current page is changed.
  }
});

// scroll
snapHelper.scrollToPage(target_page_index);
snapHelper.smoothScrollToPage(target_page_index);

// others
snapHelper.getCurrentPageIndex();
snapHelper.getPageCount();
```