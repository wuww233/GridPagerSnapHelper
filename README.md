# GridPagerSnapHelper
A SnapHelper which makes RecyclerView be snapped like Viewpager

## Features

1. 支持按列滑动和按行滑动
2. 最后一个页面无需填满组件
3. 拖动半个及以上组件的长度即可移动至下一页/上一页

！各个组件的尺寸须一致，不然SnapHelper的距离计算会出现问题

## How to use

1. Add the dependence in a file like:

+ `build.gradle`(Groovy)
  ```groovy
  dependencies {
    implementation 'io.github.wuww233:GridPagerSnapHelper:0.1.1'
  }
  ```
+ or `build.gradle.kts`(Kotlin)
  ```kotlin
  dependencies {
    implementation("io.github.wuww233:GridPagerSnapHelper:0.1.1")
  }
  ```

2. Use it in your code

```java
// set Adapter and GridLayoutManager for your RecyclerView before setting GridPageSnapHelper
GridPageSnapHelper snapHelper = new GridPageSnapHelper(max_size_in_each_row_or_line, max_size_of_each_page);
snapHelper.attachToRecyclerView(your_RecyclerView_name_managed_by_GridLayoutManager);


```

## API

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