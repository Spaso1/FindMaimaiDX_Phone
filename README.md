# FindMaimaiUltra

FindMaimaiUltra 是一个 Android 应用程序，用于查询和展示 Maimai（舞萌）游戏的成绩数据。它提供了丰富的功能，包括成绩查询、歌曲成绩展示、地图功能、搜索和排序功能等。

## 特性

- **成绩查询**：从服务器获取用户的成绩数据。
- **歌曲成绩展示**：以列表形式展示用户的歌曲成绩，并提供详细信息。
- **地图功能**：展示 Maimai 机厅的位置信息。
- **搜索功能**：根据歌曲名称搜索特定歌曲的成绩。
- **排序功能**：按分数对歌曲成绩进行排序。
- **更新功能**：从服务器更新最新的成绩数据。

## 安装步骤

1. **克隆项目仓库**：
```bash 
   git clone https://github.com/Spaso1/FindMaimaiDX_Phone.git cd FindMaimaiUltra
```
2. **打开项目**：
    - 打开 Android Studio。
    - 导入项目文件夹 `FindMaimaiUltra`。

3. **配置项目依赖项**：
    - 确保项目依赖项已正确配置。主要依赖项包括：
        - Glide: 用于图片加载。
        - OkHttp: 用于网络请求。
        - Gson: 用于 JSON 解析。
        - 其他依赖项请参考 `build.gradle` 文件。

4. **构建和运行项目**：
    - 在 Android Studio 中点击 `Build` -> `Rebuild Project`。
    - 连接 Android 设备或启动模拟器。
    - 点击 `Run` 按钮运行项目。

## 使用说明

1. **绑定机器人账号**：
    - 打开应用后，首先需要绑定机器人账号以获取成绩数据。

2. **更新数据**：
    - 点击右下角的 `FloatingActionButton`，选择“更新数据”选项。

3. **分数排序**：
    - 点击右下角的 `FloatingActionButton`，选择“分数排序”选项。

4. **搜索指定歌曲**：
    - 点击右下角的 `FloatingActionButton`，选择“搜索指定歌曲”选项。

5. **查看歌曲详情**：
    - 在歌曲成绩列表中点击某首歌曲，将弹出详细信息对话框。

## 依赖项

- `com.google.android.gms:play-services-location:21.0.1`
- `androidx.appcompat:appcompat:1.6.1`
- `com.google.android.material:material:1.9.0`
- `androidx.recyclerview:recyclerview:1.3.2`
- `junit:junit:4.13.2`
- `androidx.test.ext:junit:1.1.5`
- `com.otaliastudios:zoomlayout:1.9.0`
- `androidx.work:work-runtime:2.7.1`
- `androidx.test.espresso:espresso-core:3.5.1`
- `com.squareup.okhttp3:okhttp:4.9.1`
- `com.google.code.gson:gson:2.8.8`
- `com.github.bumptech.glide:glide:4.12.0`
- `com.github.bumptech.glide:compiler:4.12.0`
- `org.nanohttpd:nanohttpd:2.2.0`
- `com.baidu.lbsyun:BaiduMapSDK_Map:7.6.3`
- `com.squareup.retrofit2:retrofit:2.9.0`
- `com.squareup.retrofit2:converter-gson:2.9.0`
- `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1`
- `com.journeyapps:zxing-android-embedded:4.3.0`
- `com.github.yalantis:ucrop:2.2.8`
- `com.github.chrisbanes:PhotoView:2.3.0`
- `androidx.appcompat:appcompat:1.6.1`
- `com.google.android.material:material:1.8.0`
- `androidx.constraintlayout:constraintlayout:2.1.4`
- `androidx.navigation:navigation-fragment:2.5.3`
- `androidx.navigation:navigation-ui:2.5.3`
- `jp.wasabeef:glide-transformations:4.3.0`

## 贡献指南

### 提交问题和功能请求

- 如果你发现任何问题或有功能建议，请在 [Issues](https://github.com/Spaso1/FindMaimaiDX_Phone/issues) 页面提交问题或功能请求。

### 贡献代码
**创建 Pull Request**：
    
- 在 GitHub 页面上，点击 `Compare & pull request` 按钮。
- 填写 Pull Request 描述并提交。

## 许可证

本项目遵循 MIT 许可证。详细信息请参考 [LICENSE](LICENSE) 文件。

## 项目地址

- [GitHub 项目地址](https://github.com/Spaso1/FindMaimaiDX_Phone)

## 开发者

- [Spaso1](https://github.com/Spaso1)
欢迎合作或者提意见！
